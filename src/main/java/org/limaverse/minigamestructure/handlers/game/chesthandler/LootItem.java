package org.limaverse.minigamestructure.handlers.game.chesthandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LootItem {
    private final Material material;
    private final String customName;
    private final Map<Enchantment, Integer> enchantmentToLevelMap = new HashMap<>();
    private final Map<Attribute, AttributeModifier> attributeToAttributeModifierMap = new HashMap<>();
    private final List<ItemFlag> itemFlagsList = new ArrayList<>();
    private boolean setUnbreakable;
    private final double chance;
    private final int minAmount;
    private final int maxAmount;
    private final int weight;
    private final String type;

    public LootItem(ConfigurationSection section) {
        Material material;

        try {
            material = Material.valueOf(section.getString("material"));
        } catch (Exception e) {
            material = Material.AIR;
        }

        this.material = material;
        this.customName = section.getString("name");

        ConfigurationSection enchantmentsSection = section.getConfigurationSection("enchantments");
        if (enchantmentsSection != null) {
            for (String enchantmentKey : enchantmentsSection.getKeys(false)){
                Enchantment enchantment = Enchantment.getByKey(
                        NamespacedKey.minecraft(
                                enchantmentKey.toLowerCase()
                        )
                );

                if (enchantment != null) {
                    int level = enchantmentsSection.getInt(enchantmentKey);
                    enchantmentToLevelMap.put(enchantment, level);
                }
            }
        }

        if (section.getBoolean("unbreakable")) {
            setUnbreakable = true;
        }

        ConfigurationSection flagsSection = section.getConfigurationSection("flags");
        if (flagsSection != null) {
            List<String> flags = section.getStringList("flags");
            for (String flag : flags) {
                try {
                    ItemFlag itemFlag = ItemFlag.valueOf(flag);
                    itemFlagsList.add(itemFlag);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().severe("Invalid item flag: " + flag);
                }
            }
        }

        ConfigurationSection attributesSection = section.getConfigurationSection("attributes");
        if (attributesSection != null) {
            for (String attributeKey : attributesSection.getKeys(false)) {
                try {
                    Attribute attribute = Attribute.valueOf(attributeKey);
                    double amountModifier = attributesSection.getDouble("amount");
                    String operation = attributesSection.getString("operation", "ADD_NUMBER").toUpperCase();
                    UUID uuid = UUID.randomUUID();

                    AttributeModifier modifier = new AttributeModifier(uuid, attributeKey, amountModifier, AttributeModifier.Operation.valueOf(operation));
                    attributeToAttributeModifierMap.put(attribute, modifier);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().severe("Invalid attribute or operation: " + attributeKey);
                }
            }
        }

        this.chance = section.getDouble("chance");
        this.minAmount = section.getInt("minAmount");
        this.maxAmount = section.getInt("maxAmount");
        this.weight = section.getInt("weight");
        this.type = section.getString("type");
    }

    public boolean shouldFill(Random random) {
        return random.nextDouble() < chance;
    }

    public ItemStack make(ThreadLocalRandom random) {
        int amount = random.nextInt(minAmount, maxAmount + 1);
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (customName != null) {
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
        }

        if (!enchantmentToLevelMap.isEmpty()) {
            for (Map.Entry<Enchantment, Integer> enchantEntry : enchantmentToLevelMap.entrySet()) {
                itemMeta.addEnchant(
                        enchantEntry.getKey(),
                        enchantEntry.getValue(),
                        true
                );
            }
        }

        if (setUnbreakable) {
            itemMeta.setUnbreakable(true);
        }

        if (!itemFlagsList.isEmpty()) {
            for (ItemFlag flag : itemFlagsList) {
                itemMeta.addItemFlags(flag);
            }
        }

        if (!attributeToAttributeModifierMap.isEmpty()) {
            for (Map.Entry<Attribute, AttributeModifier> attributeEntry : attributeToAttributeModifierMap.entrySet()) {
                itemMeta.addAttributeModifier(
                        attributeEntry.getKey(),
                        attributeEntry.getValue()
                );
            }
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public int getWeight() {
        return weight;
    }

    public String getType() {
        return type;
    }
}
