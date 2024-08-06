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

public class ResetMapCommand implements CommandExecutor, TabCompleter {

    private final ResetHandler resetHandler;
    private final MiniGameStructure plugin;

    // Constructor to initialize the ResetHandler and set the command executor and tab completer.
    public ResetMapCommand(MiniGameStructure plugin, ChestHandler chestHandler) {
        this.resetHandler = new ResetHandler(plugin, chestHandler);
        this.plugin = plugin;
        plugin.getCommand("resetmap").setExecutor(this);
        plugin.getCommand("resetmap").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        // Check if the correct number of arguments is provided.
        if (args.length != 1) {
            sender.sendMessage("Usage: /resetmap <map_name>");
            return true;
        }

        String mapName = args[0];

        // Check if the specified map exists.
        File mapFolder = new File(plugin.getDataFolder(), "maps");
        File[] maps = mapFolder.listFiles();
        boolean found = false;
        if (maps != null) {
            for (File map : maps) {
                if (map.isDirectory() && map.getName().equalsIgnoreCase(mapName)) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            sender.sendMessage(Colorize.format("&cMap " + mapName + " not found."));
            return true;
        }

        // Reset the map to the specified map.
        resetHandler.resetMap(mapName);

        sender.sendMessage(Colorize.format("&aMap has been reset to: " + mapName));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest existing maps for the first argument.
            File mapFolder = new File(plugin.getDataFolder(), "maps");
            File[] maps = mapFolder.listFiles();
            if (maps != null) {
                for (File map : maps) {
                    if (map.isDirectory()) {
                        suggestions.add(map.getName());
                    }
                }
            }
        }

        return suggestions;
    }
}
