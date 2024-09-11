package org.limaverse.minigamestructure.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.PlayerHandler;

public class SetPlayerSpawnCommand implements CommandExecutor {
    private final PlayerHandler playerHandler;

    // Constructor to initialize the PlayerHandler.
    public SetPlayerSpawnCommand(MiniGameStructure plugin) {
        this.playerHandler = PlayerHandler.getInstance();

        plugin.getCommand("setplayerspawn").setExecutor(this);
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
        if (args.length != 1) {
            player.sendMessage("Usage: /setplayerspawn <key>");
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
        float pitch = player.getLocation().getPitch();
        float yaw = player.getLocation().getYaw();

        // Set the spawn location in the PlayerHandler.
        playerHandler.setSpawnLocation(String.valueOf(key), x, y, z, pitch, yaw);
        player.sendMessage("Spawn location set for key: " + key);

        return true;
    }
}
