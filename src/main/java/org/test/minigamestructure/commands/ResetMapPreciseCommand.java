package org.test.minigamestructure.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.test.minigamestructure.MiniGameStructure;
import org.test.minigamestructure.handlers.ResetHandler;
import org.test.minigamestructure.handlers.chesthandler.ChestHandler;
import org.test.minigamestructure.utilities.Colorize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResetMapPreciseCommand implements CommandExecutor, TabCompleter {

    private final ResetHandler resetHandler;
    private final MiniGameStructure plugin;

    /**
     * Constructor to initialize the ResetHandler and set the command executor and tab completer.
     *
     * @param plugin       The main plugin instance.
     * @param chestHandler The ChestHandler instance.
     */
    public ResetMapPreciseCommand(MiniGameStructure plugin, ChestHandler chestHandler) {
        this.resetHandler = new ResetHandler(plugin, chestHandler);
        this.plugin = plugin;
        plugin.getCommand("resetmapprecise").setExecutor(this);
        plugin.getCommand("resetmapprecise").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        // Check if the correct number of arguments is provided.
        if (args.length != 2) {
            sender.sendMessage("Usage: /resetmapprecise <map_name> <variation_name>");
            return true;
        }

        String mapName = args[0];
        String variationName = args[1];

        // Check if the specified map and variation exist.
        File mapsFolder = new File(plugin.getDataFolder(), "maps");
        File mapFolder = new File(mapsFolder, mapName);
        File[] variations = mapFolder.listFiles(File::isDirectory);

        boolean foundVariation = false;

        if (mapFolder.exists() && mapFolder.isDirectory() && variations != null) {
            for (File variationFolder : variations) {
                if (variationFolder.isDirectory() && variationFolder.getName().equalsIgnoreCase(variationName)) {
                    foundVariation = true;
                    break;
                }
            }
        }

        if (!foundVariation) {
            sender.sendMessage(Colorize.format("&cVariation " + variationName + " not found in map " + mapName + "."));
            return true;
        }

        // Reset the map to the specified variation.
        resetHandler.resetMapPrecise(mapName, variationName);

        sender.sendMessage(Colorize.format("&aMap " + mapName + " has been reset to variation: " + variationName));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            File mapsFolder = new File(plugin.getDataFolder(), "maps");
            File[] mapFolders = mapsFolder.listFiles(File::isDirectory);

            if (mapFolders != null) {
                for (File mapFolder : mapFolders) {
                    if (mapFolder.isDirectory()) {
                        suggestions.add(mapFolder.getName());
                    }
                }
            }
        } else if (args.length == 2) {
            File mapsFolder = new File(plugin.getDataFolder(), "maps");
            File mapFolder = new File(mapsFolder, args[0]);
            File[] variations = mapFolder.listFiles(File::isDirectory);

            if (mapFolder.exists() && mapFolder.isDirectory() && variations != null) {
                for (File variationFolder : variations) {
                    if (variationFolder.isDirectory()) {
                        suggestions.add(variationFolder.getName());
                    }
                }
            }
        }

        return suggestions;
    }
}

