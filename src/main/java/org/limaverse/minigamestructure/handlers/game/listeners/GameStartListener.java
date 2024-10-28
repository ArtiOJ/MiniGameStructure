package org.limaverse.minigamestructure.handlers.game.listeners;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.GameStats;
import org.limaverse.minigamestructure.utilities.CountDownUtility;
import org.limaverse.minigamestructure.utilities.FreezePlayersUtility;
import org.limaverse.minigamestructure.handlers.game.chesthandler.ChestHandler;
import org.limaverse.minigamestructure.handlers.game.chesthandler.ChestSpawningHandler;
import org.limaverse.minigamestructure.handlers.game.events.GameStartEvent;
import org.limaverse.minigamestructure.handlers.game.playerhandlers.XPTimerHandler;
import org.limaverse.minigamestructure.managers.StartEffectsConfigManager;
import org.limaverse.minigamestructure.managers.StartItemsConfigManager;
import org.limaverse.minigamestructure.utilities.WorldChanger;

import java.io.File;
import java.util.*;

public class GameStartListener implements Listener {
    private static GameStartListener instance;
    private MiniGameStructure plugin;
    private BukkitScheduler scheduler;
    private YamlConfiguration config;
    private XPTimerHandler xpTimerHandler;
    private final World world;
    private final ChestHandler chestHandler;
    private ChestSpawningHandler chestSpawningHandler;
    private final String SPAWNS_FILE_NAME = "player_spawns.yml";
    private boolean registered = false;
    private GameStartListener() {
        plugin = MiniGameStructure.getInstance();
        scheduler = Bukkit.getScheduler();
        chestSpawningHandler = ChestSpawningHandler.getInstance();
        xpTimerHandler = XPTimerHandler.getInstance();
        chestHandler = ChestHandler.getInstance();

        world = plugin.getServer().getWorld("game_map");

        File dataFolder = new File(plugin.getServer().getWorld("game_map").getWorldFolder(), "hunger_games_data");
        File configFile = new File(dataFolder, SPAWNS_FILE_NAME);
        this.config = YamlConfiguration.loadConfiguration(configFile);

        if (!configFile.exists()) {
            // Handle file not existing
            plugin.getLogger().warning("player_spawns.yml file does not exist.");
        }

        if (config == null) {
            Bukkit.getLogger().warning("playerSpawnsConfig at: " + config + " is null.");
            return;
        }
    }

    public void startListener() {
        if (registered) return;
        registered = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void stopListener() {
        if (!registered) return;
        registered = false;
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        WorldChanger worldChanger = new WorldChanger(plugin);
        worldChanger.setWorldAllPlayers("game_map");

        scheduler.runTaskLater(plugin, () -> {
            StartItemsConfigManager configManager = new StartItemsConfigManager(plugin);
            for (Player player : GameStats.getInstance().getAlivePlayers()) {
                player.getInventory().clear();
                player.clearActivePotionEffects();
                player.setGameMode(GameMode.SURVIVAL);
                player.setLevel(0);
                player.setExp(1);
            }

            for (Player player : GameStats.getInstance().getGameSpectators()) {
                player.setGameMode(GameMode.SPECTATOR);
            }

            List<Entity> entList = world.getEntities();
            for (Entity current : entList) {
                if (current.getType() == EntityType.DROPPED_ITEM) {
                    current.remove();
                }
            }

            teleportPlayers();
            chestSpawningHandler.deleteChestGroups();
            chestSpawningHandler.spawnChestPlatforms();

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                new FreezePlayersUtility(plugin, 5, false);
                new CountDownUtility(plugin, 5, Sound.BLOCK_NOTE_BLOCK_PLING);
                chestHandler.resetChests();
                configManager.giveStartingItems();
                xpTimerHandler.resetTimerAllTimers();
            });

            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, xpTimerHandler::startTimersForAllPlayers, 20 * 20);

            // Schedule start game effects after 5 seconds
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                StartEffectsConfigManager manager = new StartEffectsConfigManager(plugin);
                manager.giveStartingEffects();
            }, 5 * 20);

        }, 4 * 20);
    }

    private void teleportPlayers() {
        Set<Location> spawnLocations = new HashSet<>();

        // Collect all player spawn locations
        if (config.contains("player_spawns")) {
            for (String key : config.getConfigurationSection("player_spawns").getKeys(false)) {
                Location location = getSpawnLocation(key);
                if (location != null) {
                    spawnLocations.add(location);
                }
            }

            // Shuffle and teleport players
            List<Location> shuffledLocations = new ArrayList<>(spawnLocations);
            Collections.shuffle(shuffledLocations);
            Iterator<Location> iterator = shuffledLocations.iterator();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (iterator.hasNext()) {
                    Location spawnLocation = iterator.next();
                    spawnLocation.setWorld(world);
                    player.teleport(spawnLocation);
                }
            }
        } else {
            plugin.getLogger().warning("No player spawns configuration found.");
        }
    }

    private Location getSpawnLocation(String key) {
        if (config.contains("player_spawns." + key)) {
            String data = config.getString("player_spawns." + key);
            String[] parts = data.split(",");
            if (parts.length == 5) {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                float pitch = Float.parseFloat(parts[3]);
                float yaw = Float.parseFloat(parts[4]);
                return new Location(Bukkit.getWorlds().get(0), x, y, z, yaw, pitch);
            }
        }
        return null;
    }

    public static synchronized GameStartListener getInstance() {
        if (instance == null) {
            instance = new GameStartListener();
        }
        return instance;
    }
}
