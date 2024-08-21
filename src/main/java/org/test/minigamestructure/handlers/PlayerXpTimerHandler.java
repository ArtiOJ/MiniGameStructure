package org.test.minigamestructure.handlers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerXpTimerHandler implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, Integer> playerTimers = new HashMap<>();
    private final File configFile;
    private final YamlConfiguration config;

    // Constants for the formula
    private final double multiplier;
    private final double constant;
    private final int waterScail;
    private BukkitTask runnable;

    public PlayerXpTimerHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        File dataFolder = new File(plugin.getServer().getWorld("world").getWorldFolder(), "hunger_games_data");
        configFile = new File(dataFolder, "timer.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        // Error: Check if config is missing
        if (!configFile.exists()) {
            plugin.getLogger().severe("Config file not found! Ensure the config.yml file is present in the correct directory.");
        }

        // Load formula constants from config
        multiplier = config.getDouble("xp_increase.formula.multiplier", 25);
        constant = config.getDouble("xp_increase.formula.constant", 50);
        waterScail = config.getInt("xp_increase.formula.water_scale", 1);
    }

    public void startTimersForAllPlayers() {
        int initialDuration = config.getInt("timer.duration", -1);

        // Error: Check if the timer duration is valid
        if (initialDuration == -1) {
            plugin.getLogger().severe("Timer duration is missing or invalid in the config. Please check 'timer.duration'.");
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            playerTimers.put(playerId, initialDuration);

            // Set the initial XP level to the total duration
            player.setLevel(0);
        }

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (playerTimers.isEmpty()) {
                    cancel();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    if (!playerTimers.containsKey(playerId)) continue;

                    int timeLeft = playerTimers.get(playerId);
                    if (timeLeft <= 0) {
                        applyEndAction(player);
                        continue;
                    }


                    applyEffects(player, timeLeft);

                    // Update the player's XP level to show remaining time
                    player.setLevel(0);

                    // Update the XP bar to show stage progression
                    updateXpBar(player, timeLeft, initialDuration);

                    if (!player.isInWater()) {
                        playerTimers.put(playerId, timeLeft - 1);
                    } else {
                        playerTimers.put(playerId, timeLeft - waterScail);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Run every second
    }

    private void applyEffects(Player player, int timeLeft) {
        for (String effectKey : config.getConfigurationSection("timer.effects").getKeys(false)) {
            String path = "timer.effects." + effectKey;
            int startTime = config.getInt(path + ".start_time", -1);

            // Error: Check if effect configuration is valid
            if (startTime == -1) {
                plugin.getLogger().severe("Start time is missing or invalid for effect " + effectKey + ". Please check 'timer.effects." + effectKey + ".start_time'.");
                continue;
            }

            if (timeLeft <= startTime) {
                String type = config.getString(path + ".type");
                int maxLevel = config.getInt(path + ".max_level", -1);
                boolean scaling = config.getBoolean(path + ".scaling", false);

                // Error: Validate effect type and level
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

                if (potionEffectType != null) {
                    player.addPotionEffect(new PotionEffect(potionEffectType, 40, effectLevel, true, true));
                } else {
                    plugin.getLogger().severe("Invalid effect type: " + type + " for effect " + effectKey);
                }
            }
        }
    }

    private void applyEndAction(Player player) {
        String type = config.getString("timer.end_action.type");
        int level = config.getInt("timer.end_action.level", -1);
        int duration = config.getInt("timer.end_action.duration", -1);

        // Error: Validate end action configuration
        if (type == null) {
            plugin.getLogger().severe("End action type is missing. Please check 'timer.end_action.type'.");
            return;
        }

        if (level == -1) {
            plugin.getLogger().severe("End action level is missing or invalid. Please check 'timer.end_action.level'.");
            return;
        }

        if (duration == -1) {
            plugin.getLogger().severe("End action duration is missing or invalid. Please check 'timer.end_action.duration'.");
            return;
        }

        PotionEffectType potionEffectType = PotionEffectType.getByName(type.toUpperCase());
        if (potionEffectType == null) {
            plugin.getLogger().severe("Invalid end action effect type: " + type);
            return;
        }

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getDuration() / 20 <= 1) {
                player.addPotionEffect(new PotionEffect(potionEffectType, duration * 20, level - 1, true, true));
            }
        }
    }

    public void increaseTimer(Player player, double distance) {
        UUID playerId = player.getUniqueId();
        if (playerTimers.containsKey(playerId)) {
            double increaseAmount = -Math.sqrt(multiplier * distance) + constant;
            int newTime = playerTimers.get(playerId) + (int) increaseAmount;

            // Ensure the timer doesn't exceed the initial duration
            int initialDuration = config.getInt("timer.duration", -1);
            if (newTime > initialDuration) {
                newTime = initialDuration;
            }

            playerTimers.put(playerId, newTime);

            // Update the player's XP level to show the increased time
            player.setLevel(0);

            // Update the XP bar to reflect the new timer value
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

        // Set the XP bar progress based on time left
        player.setExp((float) progress);
    }

    public void stopAllTimers() {
        playerTimers.clear();
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player target = (Player) event.getEntity();

            // Calculate the distance between the damager and the target
            double distance = damager.getLocation().distance(target.getLocation());

            // Increase the timer for the damager based on the distance
            increaseTimer(damager, distance);
        }
    }
}

