package org.test.minigamestructure.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.test.minigamestructure.MiniGameStructure;
import org.test.minigamestructure.handlers.ChestSpawningHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetChestSpawnCommand implements CommandExecutor, TabCompleter {
    private final ChestSpawningHandler chestSpawningHandler;

    // Constructor to initialize the ChestSpawningHandler and set the command executor and tab completer.
    public SetChestSpawnCommand(MiniGameStructure plugin, ChestSpawningHandler handler) {
        this.chestSpawningHandler = handler;
        plugin.getCommand("setchestspawn").setExecutor(this);
        plugin.getCommand("setchestspawn").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Check if the correct number of arguments is provided.
        if (args.length != 2) {
            player.sendMessage("Usage: /setchestspawn <key> <block_type>");
            return true;
        }

        // Attempt to parse the key as an integer.
        int key;
        try {
            key = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("The key must be an integer.");
            return true;
        }

        // Get player's current location.
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();

        // Validate the block type.
        String blockTypeStr = args[1].toUpperCase();
        Material blockType;
        try {
            blockType = Material.valueOf(blockTypeStr);
            if (!blockType.isBlock()) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid block type.");
            return true;
        }

        // Set the chest spawn location in the ChestSpawningHandler.
        chestSpawningHandler.setSpawnLocation(key, x, y, z, blockType);
        player.sendMessage("Chest spawn location set for key: " + key + " with block type: " + blockTypeStr);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest "<key>" for the first argument.
            return Arrays.asList("<key>");
        } else if (args.length == 2) {
            // Suggest block types for the second argument.
            String partialBlockType = args[1].toUpperCase();
            return Arrays.stream(Material.values())
                    .filter(material -> material.isBlock() && material.name().startsWith(partialBlockType))
                    .map(Enum::name)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
