package org.test.minigamestructure.gameevents;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GiveStartingItemsEvent {

    public GiveStartingItemsEvent(Player player) {
        giveItems(player);
    }

    private void giveItems(Player player) {
        // Stone Shovel
        ItemStack stoneShovel = new ItemStack(Material.STONE_SHOVEL);

        // Efficiency 2 Wooden Pickaxe
        ItemStack woodenPickaxe = new ItemStack(Material.WOODEN_PICKAXE);
        ItemMeta pickaxeMeta = woodenPickaxe.getItemMeta();
        pickaxeMeta.addEnchant(Enchantment.DIG_SPEED, 2, true);
        woodenPickaxe.setItemMeta(pickaxeMeta);

        // 8 blocks (assuming cobblestone for this example)
        ItemStack blocks = new ItemStack(Material.COBBLESTONE, 8);

        // Give items to player
        player.getInventory().addItem(stoneShovel);
        player.getInventory().addItem(woodenPickaxe);
        player.getInventory().addItem(blocks);
    }
}
