package org.limaverse.minigamestructure.handlers.game.playerhandlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.GameStats;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XPTimerHandler implements Listener {
    private static XPTimerHandler instance;

    private final MiniGameStructure plugin;
    private final Map<UUID, Integer> playerTimers = new HashMap<>();
    private final File configFile;
    private final YamlConfiguration config;

    private final double multiplier;
    private final double constant;
    private final int waterScale;
    private BukkitTask runnable;
    private boolean allowNaturalRegen;
    private GameStats gameStats;

    private XPTimerHandler() {
        this.plugin = MiniGameStructure.getInstance();
        this.gameStats = GameStats.getInstance();
        File dataFolder = new File(plugin.getServer().getWorld("game_map").getWorldFolder(), "hunger_games_data");
        configFile = new File(dataFolder, "timer.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        allowNaturalRegen = config.getBoolean("timer.end_action.allow_natural_regen", true);

        if (!configFile.exists()) {
            plugin.getLogger().severe("Config file not found! Ensure the config.yml file is present in the correct directory.");
        }

        multiplier = config.getDouble("xp_increase.formula.multiplier", 25);
        constant = config.getDouble("xp_increase.formula.constant", 50);
        waterScale = config.getInt("xp_increase.formula.water_scale", 1);
    }

    public static synchronized XPTimerHandler getInstance() {
        if (instance == null) {
            instance = new XPTimerHandler();
        }
        return instance;
    }

    public static void unregister() {
        if (instance != null) {
            // Unregister the event listeners
            HandlerList.unregisterAll(instance);

            // Cancel any running tasks
            if (instance.runnable != null) {
                instance.runnable.cancel();
            }

            // Clear player timers
            instance.playerTimers.clear();

            // Set the instance to null to de-instance the singleton
            instance = null;
        }
    }

    public void startTimersForAllPlayers() {
        int initialDuration = config.getInt("timer.duration", -1);

        if (initialDuration == -1) {
            plugin.getLogger().severe("Timer duration is missing or invalid in the config. Please check 'timer.duration'.");
            return;
        }
        startSpawnIncreaseTimer(1);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (Player player : gameStats.getActiveGamePlayers()) {
            UUID playerId = player.getUniqueId();
            playerTimers.put(playerId, initialDuration);

            player.setLevel(0);
        }

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (playerTimers.isEmpty()) {
                    cancel();
                    return;
                }

                for (Player player : gameStats.getAlivePlayers()) {
                    UUID playerId = player.getUniqueId();
                    if (!playerTimers.containsKey(playerId)) continue;
                    int timeLeft = playerTimers.get(playerId);
                    if (timeLeft <= 0) {
                        applyEndAction(player);
                        continue;
                    }

                    applyEffects(player, timeLeft);

                    player.setLevel(0);

                    updateXpBar(player, timeLeft, initialDuration);

                    if (!player.isInWater()) {
                        playerTimers.put(playerId, timeLeft - 1);
                    } else {
                        playerTimers.put(playerId, timeLeft - waterScale);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Run every second
    }

    private void applyEffects(Player player, int timeLeft) {
        for (String effectKey : config.getConfigurationSection("timer.effects").getKeys(false)) {
            String path = "timer.effects." + effectKey;
            int startTime = config.getInt(path + ".start_time", -1);

            if (startTime == -1) {
                plugin.getLogger().severe("Start time is missing or invalid for effect " + effectKey + ". Please check 'timer.effects." + effectKey + ".start_time'.");
                continue;
            }

            if (timeLeft <= startTime) {
                String type = config.getString(path + ".type");
                int maxLevel = config.getInt(path + ".max_level", -1);
                boolean scaling = config.getBoolean(path + ".scaling", false);

                if (type == null) {
                    plugin.getLogger().severe("Effect type is missing for " + effectKey + ". Please check 'timer.effects." + effectKey + ".type'.");
                    continue;
                }

                if (maxLevel == -1) {
                    plugin.getLogger().severe("Max level is missing or invalid for effect " + effectKey + ". Please check 'timer.effects." + effectKey + ".max_level'.");
                    continue;
                }

                int effectLevel = scaling ? Math.min(maxLevel, maxLevel * (startTime - timeLeft) / startTime) : maxLevel;
                PotionEffectType potionEffectType = PotionEffectType.getByName(type.toUpperCase());

                if (potionEffectType == null) {
                    plugin.getLogger().severe("Invalid effect type: " + type + " for effect " + effectKey);
                    return;
                }

                if (player.getPotionEffect(potionEffectType) == null) {
                    player.addPotionEffect(new PotionEffect(potionEffectType, 5 * 20, effectLevel, true, true));
                }

                for (PotionEffect effect : player.getActivePotionEffects()) {
                    if (!effect.getType().equals(potionEffectType)) continue;
                    if (effect.getDuration() / 20 <= 1) {
                        player.addPotionEffect(new PotionEffect(potionEffectType, 5 * 20, effect.getAmplifier() - 1, true, true));
                    }
                }
            }
        }
    }

    private void applyEndAction(Player player) {
        String type = config.getString("timer.end_action.type");
        int level = config.getInt("timer.end_action.level", 1) - 1;
        int duration = config.getInt("timer.end_action.duration", 5) * 20;

        if (type == null) {
            plugin.getLogger().severe("End action type is missing. Please check 'timer.end_action.type'.");
            return;
        }

        PotionEffectType potionEffectType = PotionEffectType.getByName(type.toUpperCase());
        if (potionEffectType == null) {
            plugin.getLogger().severe("Invalid end action effect type: " + type);
            return;
        }

        PotionEffect currentEffect = player.getPotionEffect(potionEffectType);

        if (currentEffect != null) {
            if (currentEffect.getDuration() > 20) {
                return;
            }
        }

        player.addPotionEffect(new PotionEffect(potionEffectType, duration, level, true, true));
    }

    private void startSpawnIncreaseTimer(int timePerSecond) {
        World world = Bukkit.getWorld("game_map");
        Location center = new Location(world, 0, 0, 0);
        int radius = 10;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : gameStats.getAlivePlayers()) {
                if (center.distance(player.getLocation()) <= radius) {
                    int time = playerTimers.get(player.getUniqueId());
                    playerTimers.put(player.getUniqueId(), time + timePerSecond + 1);
                }
            }
        }, 0, 20);
    }

    public void increaseTimer(Player player, double distance) {
        UUID playerId = player.getUniqueId();
        if (playerTimers.containsKey(playerId)) {
            double increaseAmount = -Math.sqrt(multiplier * distance) + constant;
            int newTime = playerTimers.get(playerId) + (int) increaseAmount;

            int initialDuration = config.getInt("timer.duration", -1);
            if (newTime > initialDuration) {
                newTime = initialDuration;
            }

            playerTimers.put(playerId, newTime);

            player.setLevel(0);

            updateXpBar(player, newTime, initialDuration);
        }
    }

    public void resetTimerAllTimers() {
        if (runnable != null) {
            runnable.cancel();
        }
        playerTimers.clear();
    }

    private void updateXpBar(Player player, int timeLeft, int initialDuration) {
        double progress = (double) timeLeft / initialDuration;
        player.setExp((float) progress);
    }

    public void stopAllTimers() {
        playerTimers.clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player target) {
            double distance = damager.getLocation().distance(target.getLocation());
            increaseTimer(damager, distance);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof Player shooter && event.getHitEntity() instanceof Player target) {
            if (shooter == target) return;
            double distance = shooter.getLocation().distance(target.getLocation());
            increaseTimer(shooter, distance);
        }
    }

    @EventHandler
    public void onPlayerRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (allowNaturalRegen) return;
        if (!playerTimers.containsKey(event.getEntity().getUniqueId())) return;
        if (playerTimers.get(event.getEntity().getUniqueId()) <= 0) event.setCancelled(true);
    }
}
