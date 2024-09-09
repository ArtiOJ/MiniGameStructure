package org.limaverse.minigamestructure.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.limaverse.minigamestructure.MiniGameStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class WorldChanger {

    private World bukkitWorld;
    private File worldFolder;
    private final MiniGameStructure plugin;

    public WorldChanger(MiniGameStructure plugin) {
        this.plugin = plugin;
    }

    public void setWorldAllPlayers(String worldName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setWorld(worldName, player);
        }
    }
    public void setWorld(String worldName, Player player) {
        unload();
        this.worldFolder = plugin.getServer().getWorldContainer();
        this.worldFolder = new File(worldFolder, worldName);
        this.bukkitWorld = Bukkit.createWorld(new WorldCreator(this.worldFolder.getName()));
        player.teleport(new Location(bukkitWorld, 0, 0, 0));
    }
    public void unload() {
        if (this.bukkitWorld != null) {
            Bukkit.unloadWorld(this.bukkitWorld, true);
            this.bukkitWorld = null;
        }
    }
    public List<String> getWorlds() {
        List<String> worlds = new ArrayList<>();
        try {
            File worldContainer = plugin.getServer().getWorldContainer();
            if (worldContainer == null) {
                plugin.getLogger().log(Level.SEVERE, "World container is null");
                return worlds;
            }

            File[] files = worldContainer.listFiles();
            if (files == null) {
                plugin.getLogger().log(Level.SEVERE, "No files found in world container");
                return worlds;
            }

            for (File file : files) {
                if (file.isDirectory() && new File(file, "level.dat").exists()) {
                    worlds.add(file.getName());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Exception occurred while getting world list", e);
        }

        return worlds;
    }
}
