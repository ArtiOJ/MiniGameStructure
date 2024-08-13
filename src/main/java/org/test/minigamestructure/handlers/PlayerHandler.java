package org.test.minigamestructure.handlers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.test.minigamestructure.MiniGameStructure;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerHandler {
    private final MiniGameStructure plugin;
    private final World world;
    private File configFile;
    private FileConfiguration config;
    private static final String SPAWNS_FILE_NAME = "player_spawns.yml";

    public PlayerHandler(MiniGameStructure plugin) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld("world");
        File dataFolder = new File(plugin.getServer().getWorld("world").getWorldFolder(), "hunger_games_data");
        configFile = new File(dataFolder, SPAWNS_FILE_NAME);
        config = YamlConfiguration.loadConfiguration(configFile);

        if (!configFile.exists()) {
            // Handle file not existing
            plugin.getLogger().warning("player_spawns.yml file does not exist.");
        }

        if (config == null) {
            Bukkit.getLogger().warning("playerSpawnsConfig at: " + config + " is null.");
            return;
        }
        if (config.get("max_players") != null) {
            Bukkit.setMaxPlayers(config.getInt("max_players"));
        }
    }

    /**
     * Retrieves the spawn location for a specific player.
     *
     * @param key The key of the player.
     * @return The spawn location, or null if not found.
     */
    public Location getSpawnLocation(String key) {
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
        x = Math.floor(x) + 0.5;
        z = Math.floor(z) + 0.5;

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
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

