package org.limaverse.minigamestructure.gameevents;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.ChestSpawningHandler;
import org.limaverse.minigamestructure.handlers.PlayerXpTimerHandler;
import org.limaverse.minigamestructure.handlers.chesthandler.ChestHandler;
import org.limaverse.minigamestructure.handlers.PlayerHandler;
import org.limaverse.minigamestructure.managers.StartEffectsConfigManager;
import org.limaverse.minigamestructure.managers.StartItemsConfigManager;
import org.limaverse.minigamestructure.utilities.WorldChanger;

import java.util.List;

public class StartGameEvent implements Listener {
    private final MiniGameStructure plugin;
    private final PlayerHandler playerHandler;
    private final ChestHandler chestHandler;
    private final ChestSpawningHandler chestSpawningHandler;
    private final PlayerXpTimerHandler playerXpTimerHandler;
    boolean allowChests = false;

    public StartGameEvent(MiniGameStructure plugin, PlayerHandler playerHandler, ChestHandler chestHandler, ChestSpawningHandler chestSpawningHandler, PlayerXpTimerHandler playerXpTimerHandler) {
        this.plugin = plugin;
        this.playerHandler = playerHandler;
        this.chestHandler = chestHandler;
        this.chestSpawningHandler = chestSpawningHandler;
        this.playerXpTimerHandler = playerXpTimerHandler;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void startGame() {
        // Clear inventories, reset effects, and set game mode
        World world = plugin.getServer().getWorld("game_map");
        WorldChanger worldChanger = new WorldChanger(plugin);
        worldChanger.setWorldAllPlayers("game_map");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            allowChests = true;
            StartItemsConfigManager configManager = new StartItemsConfigManager(plugin);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getInventory().clear();
                player.clearActivePotionEffects();
                player.setGameMode(GameMode.SURVIVAL);
                player.setLevel(0);
                player.setExp(1);
            }

            // Remove dropped items from the world
            List<Entity> entList = world.getEntities();
            for (Entity current : entList) {
                if (current.getType() == EntityType.DROPPED_ITEM) {
                    current.remove();
                }
            }

            // Teleport players and spawn chests
            playerHandler.teleportPlayers();
            chestSpawningHandler.deleteChestGroups();
            chestSpawningHandler.spawnChestPlatforms();

            // Run additional events asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                new FreezePlayersEvent(plugin, 5, false);
                new CountDownEvent(plugin, 5, Sound.BLOCK_NOTE_BLOCK_PLING);
                chestHandler.resetChests();
                configManager.giveStartingItems();
                playerXpTimerHandler.resetTimerAllTimers();
            });

            // Start player XP timers 20 seconds later
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, playerXpTimerHandler::startTimersForAllPlayers, 20 * 20);

            // Schedule start game effects after 5 seconds
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                StartEffectsConfigManager manager = new StartEffectsConfigManager(plugin);
                manager.giveStartingEffects();
            }, 5 * 20);
        }, 4 * 20);
    }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if ((holder instanceof Chest || holder instanceof Barrel || holder instanceof DoubleChest) && !allowChests) {
            event.setCancelled(true);
        }
    }
}
