package org.limaverse.minigamestructure.handlers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.chesthandler.ChestHandler;
import org.limaverse.minigamestructure.utilities.Colorize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;

/**
 * Handles the resetting of maps, including specific variations and chest states.
 */
public class ResetHandler {
    private final MiniGameStructure plugin;


    public ResetHandler(MiniGameStructure plugin) {
        this.plugin = plugin;
    }

    /**
     * Resets the map to a specific variation.
     *
     * @param mapName        The name of the map to reset.
     * @param variationName  The name of the variation to reset to.
     */
    public void resetMapPrecise(String mapName, String variationName) {
        File mapsFolder = new File(plugin.getDataFolder(), "maps");
        File mapFolder = new File(mapsFolder, mapName);
        File variationFolder = new File(mapFolder, variationName);

        // Check if the specified map and variation exist
        if (!mapFolder.exists() || !mapFolder.isDirectory()) {
            Bukkit.broadcastMessage(Colorize.format("&cMap not found: " + mapName));
            return;
        }

        if (!variationFolder.exists() || !variationFolder.isDirectory()) {
            Bukkit.broadcastMessage(Colorize.format("&cMap variation not found: " + variationName));
            return;
        }

        // Reset the map with the specified variation
        resetWorld(variationFolder);

        // Notify about the restart requirement with countdown
        Bukkit.broadcastMessage(Colorize.format("&cThe map has been reset. Server will restart in 3 seconds..."));

        // Schedule a restart after 3 seconds (60 ticks)
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(plugin, () -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart"); // Use "stop" if you don't have a restart command
        }, 60L); // 60 ticks = 3 seconds
    }

    /**
     * Resets the map to a default state.
     *
     * @param mapName The name of the map to reset.
     */
    public void resetMap(String mapName) {
        File mapsFolder = new File(plugin.getDataFolder(), "maps");
        File mapFolder = new File(mapsFolder, mapName);

        if (!mapFolder.exists() || !mapFolder.isDirectory()) {
            Bukkit.broadcastMessage(Colorize.format("&cMap not found: " + mapName));
            return;
        }

        File[] variations = mapFolder.listFiles(File::isDirectory);

        if (variations == null || variations.length == 0) {
            Bukkit.broadcastMessage(Colorize.format("&cNo variations found for map: " + mapName));
            return;
        }

        File selectedVariation = variations[new Random().nextInt(variations.length)];

        resetWorld(selectedVariation);

        Bukkit.broadcastMessage(Colorize.format("&cThe map has been reset to a random variation. Server will restart in 3 seconds..."));

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(plugin, () -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
        }, 60L);
    }


    /**
     * Resets the world to the state defined in the given map folder.
     *
     * @param mapFolder The folder containing the map data.
     */
    private void resetWorld(File mapFolder) {
        File worldFolder = new File(Bukkit.getWorldContainer(), plugin.getServer().getWorlds().get(0).getName());

        // Notify the player about the reset
        Bukkit.broadcastMessage(Colorize.format("&aResetting the map..."));

        try {
            // Copy the contents of the map folder to the world folder
            copyFolder(mapFolder.toPath(), worldFolder.toPath());
            Bukkit.broadcastMessage(Colorize.format("&aMap reset successfully!"));
        } catch (IOException e) {
            Bukkit.broadcastMessage(Colorize.format("&cError resetting the map."));
            e.printStackTrace();
        }
    }

    /**
     * Copies the contents of one folder to another, overwriting existing files.
     *
     * @param sourcePath The source folder path.
     * @param targetPath The target folder path.
     * @throws IOException If an I/O error occurs.
     */
    private void copyFolder(Path sourcePath, Path targetPath) throws IOException {
        Files.walk(sourcePath).forEach(source -> {
            Path target = targetPath.resolve(sourcePath.relativize(source));
            try {
                if (Files.isDirectory(source)) {
                    if (!Files.exists(target)) {
                        Files.createDirectory(target);
                    }
                } else {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error copying folder", e);
            }
        });
    }
}
