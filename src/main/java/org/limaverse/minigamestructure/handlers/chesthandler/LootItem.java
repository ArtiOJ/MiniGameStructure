package org.limaverse.minigamestructure.handlers.chesthandler;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LootItem {
    private final Material material;
    private final String customName;
    private final Map<Enchantment, Integer> enchantmentToLevelMap = new HashMap<>();
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
