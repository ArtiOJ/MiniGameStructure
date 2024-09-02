package org.limaverse.minigamestructure.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.managers.customeffects.NoDamageEffect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StartEffectsConfigManager {
    private final MiniGameStructure plugin;
    private File file;
    private YamlConfiguration config;

    public StartEffectsConfigManager(MiniGameStructure plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "starting_effects.yml");
        plugin.saveResource("starting_effects.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
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

        // Handle custom effects
        if (config.contains("custom_effects")) {
            for (String key : config.getConfigurationSection("custom_effects").getKeys(false)) {
                if (key.equalsIgnoreCase("no_damage")) {
                    int durationSeconds = config.getInt("custom_effects.no_damage.duration", 30); // default 30 seconds
                    applyNoDamageEffectToPlayers(durationSeconds);
                }
            }
        }

        // Apply potion effects to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (PotionEffect effect : startingEffects) {
                player.addPotionEffect(effect);
            }
        }
    }

    private void applyNoDamageEffectToPlayers(int durationSeconds) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            NoDamageEffect noDamageEffect = new NoDamageEffect(plugin, durationSeconds);
            noDamageEffect.applyEffect(player);
        }
    }

    private void logError(String message, String configKey) {
        Bukkit.getLogger().severe("[StartEffectsConfigManager] Encountered an error on entry: " + configKey + " of the config: " + message);
    }
}
