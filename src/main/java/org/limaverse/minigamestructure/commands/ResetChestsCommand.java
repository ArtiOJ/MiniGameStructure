package org.limaverse.minigamestructure.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.chesthandler.ChestHandler;

public class ResetChestsCommand implements CommandExecutor {

    private final ChestHandler chestHandler;

    // Constructor to initialize the ChestHandler and set the command executor.
    public ResetChestsCommand(MiniGameStructure plugin, ChestHandler chestHandler) {
        this.chestHandler = chestHandler;
        plugin.getCommand("resetchests").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        // Reset all chests using the ChestHandler.
        chestHandler.resetChests();
        sender.sendMessage("Chests have been reset.");

        return true;
    }
}
