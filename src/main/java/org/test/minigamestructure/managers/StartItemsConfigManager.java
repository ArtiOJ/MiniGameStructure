package org.test.minigamestructure.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.test.minigamestructure.MiniGameStructure;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class StartItemsConfigManager {
    private final MiniGameStructure plugin;
    private File file;
    private YamlConfiguration config;

    public StartItemsConfigManager(MiniGameStructure plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "starting_items.yml");

        plugin.saveResource("starting_items.yml", false);

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void giveStartingItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String key : config.getKeys(false)) {
                // Get item material
                String materialName = config.getString(key + ".material");
                Material material = Material.getMaterial(materialName);
                if (material == null) {
                    logError("Invalid material: " + materialName, key);
                    continue; // Skip invalid material
                }

                // Create the item stack
                int amount = config.getInt(key + ".amount", 1);
                ItemStack item = new ItemStack(material, amount);

                // Set item meta
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    // Set item name
                    if (config.contains(key + ".name")) {
                        meta.setDisplayName(config.getString(key + ".name"));
                    }

                    // Set item enchantments
                    if (config.contains(key + ".enchantments")) {
                        for (String enchantmentKey : config.getConfigurationSection(key + ".enchantments").getKeys(false)) {
                            Enchantment enchantment = Enchantment.getByKey(
                                    NamespacedKey.minecraft(
                                            enchantmentKey.toLowerCase()
                                    )
                            );
                            int level = config.getInt(key + ".enchantments." + enchantmentKey, 1);
                            if (enchantment != null) {
                                meta.addEnchant(enchantment, level, true);
                            } else {
                                logError("Invalid enchantment: " + enchantmentKey, key);
                            }
                        }
                    }

                    // Set item unbreakable
                    if (config.getBoolean(key + ".unbreakable", false)) {
                        meta.setUnbreakable(true);
                    }

                    // Set item flags
                    if (config.contains(key + ".flags")) {
                        List<String> flags = config.getStringList(key + ".flags");
                        for (String flag : flags) {
                            try {
                                ItemFlag itemFlag = ItemFlag.valueOf(flag);
                                meta.addItemFlags(itemFlag);
                            } catch (IllegalArgumentException e) {
                                logError("Invalid item flag: " + flag, key);
                            }
                        }
                    }

                    // Set item attributes
                    if (config.contains(key + ".attributes")) {
                        for (String attributeKey : config.getConfigurationSection(key + ".attributes").getKeys(false)) {
                            try {
                                Attribute attribute = Attribute.valueOf(attributeKey);
                                double amountModifier = config.getDouble(key + ".attributes." + attributeKey + ".amount");
                                String operation = config.getString(key + ".attributes." + attributeKey + ".operation", "ADD_NUMBER").toUpperCase();
                                UUID uuid = UUID.randomUUID();

                                AttributeModifier modifier = new AttributeModifier(uuid, attributeKey, amountModifier, AttributeModifier.Operation.valueOf(operation));
                                meta.addAttributeModifier(attribute, modifier);
                            } catch (IllegalArgumentException e) {
                                logError("Invalid attribute or operation: " + attributeKey, key);
                            }
                        }
                    }

                    item.setItemMeta(meta);
                }

                // Give the item to the player
                player.getInventory().addItem(item);
            }
        }
    }

    private void logError(String message, String configKey) {
        int lineNumber = config.getConfigurationSection(configKey).getCurrentPath().split("\\.").length + 1;
        Bukkit.getLogger().severe("[StartItemsConfigManager] Encountered an error on line: " + lineNumber + " of the config: " + message);
    }
}
