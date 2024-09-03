package org.limaverse.minigamestructure.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.managers.customeffects.NoDamageEffect;
import org.limaverse.minigamestructure.utilities.Colorize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StartEffectsConfigManager implements Listener {
    private final MiniGameStructure plugin;
    private final File file;
    private final YamlConfiguration config;
    private final NoDamageEffect noDamageEffect;

    public StartEffectsConfigManager(MiniGameStructure plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "starting_effects.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.noDamageEffect = config.contains("custom_effects.no_damage")
                ? new NoDamageEffect(plugin, config.getInt("custom_effects.no_damage.duration", 30))
                : null;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void giveStartingEffects() {
        List<PotionEffect> startingEffects = new ArrayList<>();

        // Handle standard potion effects
        if (config.contains("starting_effects")) {
            for (String key : config.getConfigurationSection("starting_effects").getKeys(false)) {
                String effectName = config.getString("starting_effects." + key + ".effect");
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType == null) {
                    logError("Invalid effect: " + effectName, key);
                    continue; // Skip invalid effect
                }

                int durationSeconds = config.getInt("starting_effects." + key + ".duration", 30); // default 30 seconds
                int amplifier = config.getInt("starting_effects." + key + ".amplifier", 0); // default amplifier level 0

                PotionEffect effect = new PotionEffect(effectType, durationSeconds * 20, amplifier); // Convert seconds to ticks
                startingEffects.add(effect);
            }
        }

        // Apply potion effects to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (PotionEffect effect : startingEffects) {
                player.addPotionEffect(effect);
            }
            // Apply no damage effect if it exists
            if (noDamageEffect != null) {
                noDamageEffect.applyEffect(player);
            }
        }
    }

    private void removeEffectsFromPlayer(Player player) {
        // Removing standard potion effects
        if (config.contains("starting_effects")) {
            for (String key : config.getConfigurationSection("starting_effects").getKeys(false)) {
                String effectName = config.getString("starting_effects." + key + ".effect");
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    player.removePotionEffect(effectType);
                } else {
                    logError("Invalid effect: " + effectName, key);
                }
            }
        }

        // Removing no damage effect if it exists
        if (noDamageEffect != null) {
            noDamageEffect.removeEffect(player);
        }

        // Send a message to the player
        player.sendMessage(Colorize.format("&cYour effects have expired!"));
    }

    @EventHandler
    public void onPlayerHurt(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = entityDamageByEntityEvent.getDamager();
            Entity entity = entityDamageByEntityEvent.getEntity();

            if (damager instanceof Player && entity instanceof Player) {
                Player attacker = (Player) damager;
                Player target = (Player) entity;

                boolean attackerHasEffects = attacker.getActivePotionEffects().size() > 0 ||
                        (noDamageEffect != null && noDamageEffect.hasEffect(attacker));

                boolean targetHasEffects = target.getActivePotionEffects().size() > 0 ||
                        (noDamageEffect != null && noDamageEffect.hasEffect(target));

                // If attacker has effects and target doesn't, remove effects from attacker
                if (attackerHasEffects && !targetHasEffects) {
                    removeEffectsFromPlayer(attacker);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof org.bukkit.block.data.type.Chest || holder instanceof org.bukkit.block.Barrel || holder instanceof org.bukkit.block.DoubleChest) {
            Player player = (Player) event.getPlayer();
            removeEffectsFromPlayer(player);
        }
    }

    private void logError(String message, String configKey) {
        Bukkit.getLogger().severe("[StartEffectsConfigManager] Encountered an error on entry: " + configKey + " of the config: " + message);
    }
}
