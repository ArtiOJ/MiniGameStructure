package org.limaverse.minigamestructure.gameevents;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.ChestSpawningHandler;
import org.limaverse.minigamestructure.handlers.PlayerXpTimerHandler;
import org.limaverse.minigamestructure.handlers.chesthandler.ChestHandler;
import org.limaverse.minigamestructure.handlers.PlayerHandler;
import org.limaverse.minigamestructure.managers.StartItemsConfigManager;

import java.util.List;

public class StartGameEvent {
    private final MiniGameStructure plugin;
    private final PlayerHandler playerHandler;
    private final ChestHandler chestHandler;
    private final ChestSpawningHandler chestSpawningHandler;
    private final PlayerXpTimerHandler playerXpTimerHandler;

    public StartGameEvent(MiniGameStructure plugin, PlayerHandler playerHandler, ChestHandler chestHandler, ChestSpawningHandler chestSpawningHandler, PlayerXpTimerHandler playerXpTimerHandler) {
        this.plugin = plugin;
        this.playerHandler = playerHandler;
        this.chestHandler = chestHandler;
        this.chestSpawningHandler = chestSpawningHandler;
        this.playerXpTimerHandler = playerXpTimerHandler;
    }

    public void startGame() {
        // Clear inventories, reset effects, and set game mode
        StartItemsConfigManager configManager = new StartItemsConfigManager(plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
            player.clearActivePotionEffects();
            player.setGameMode(GameMode.SURVIVAL);
            player.setLevel(0);
            player.setExp(1);
        }

        // Remove dropped items from the world
        World world = plugin.getServer().getWorld("world");
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
            new GiveStartGameEffectsEvent(plugin, 20);
        }, 5 * 20);
    }
}
