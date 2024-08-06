package org.test.minigamestructure.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.test.minigamestructure.MiniGameStructure;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerHandler {
    private final MiniGameStructure plugin;
    private final FileConfiguration playerSpawnsConfig;

    public PlayerHandler(MiniGameStructure plugin) {
        this.plugin = plugin;
        File playerSpawnsFile = new File(Bukkit.getServer().getWorld("world").getWorldFolder(), "hunger_games_data/player_spawns.yml");
        if (!playerSpawnsFile.exists()) {
            // Handle file not existing
            plugin.getLogger().warning("player_spawns.yml file does not exist.");
        }
        this.playerSpawnsConfig = YamlConfiguration.loadConfiguration(playerSpawnsFile);
        if (playerSpawnsConfig == null) {
            Bukkit.getLogger().warning("playerSpawnsConfig at: " + playerSpawnsFile + " is null.");
            return;
        }
        if (playerSpawnsConfig.get("max_players") != null) {
            Bukkit.setMaxPlayers(playerSpawnsConfig.getInt("max_players"));
        }
    }

    /**
     * Retrieves the spawn location for a specific player.
     *
     * @param key The key of the player.
     * @return The spawn location, or null if not found.
     */
    public Location getSpawnLocation(String key) {
        FileConfiguration config = playerSpawnsConfig;
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

    /**
     * Sets the spawn location for a specific player.
     *
     * @param key      The key used in the entry
     * @param x        The x-coordinate.
     * @param y        The y-coordinate.
     * @param z        The z-coordinate.
     * @param pitch    The pitch.
     * @param yaw      The yaw.
     */
    public void setSpawnLocation(String key, double x, double y, double z, float pitch, float yaw) {
        FileConfiguration config = playerSpawnsConfig;
        String path = "player_spawns." + key;
        String data = x + "," + y + "," + z + "," + pitch + "," + yaw;
        config.set(path, data);
        saveConfig();
    }

    /**
     * Teleports all players to their spawn locations.
     */
    public void teleportPlayers() {
        Set<Location> spawnLocations = new HashSet<>();

        // Collect all player spawn locations
        if (playerSpawnsConfig.contains("player_spawns")) {
            for (String key : playerSpawnsConfig.getConfigurationSection("player_spawns").getKeys(false)) {
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
                    player.teleport(spawnLocation);
                }
            }
        } else {
            plugin.getLogger().warning("No player spawns configuration found.");
        }
    }

    /**
     * Saves the player spawns configuration file.
     */
    private void saveConfig() {
        File playerSpawnsFile = new File(Bukkit.getWorldContainer(), "hunger_games_data/player_spawns.yml");
        try {
            playerSpawnsConfig.save(playerSpawnsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

