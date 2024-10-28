package org.limaverse.minigamestructure.commands.testcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.GameStats;

public class GetPoints implements CommandExecutor {

    private MiniGameStructure plugin;

    public GetPoints(MiniGameStructure plugin) {
        // Register the command directly in the constructor
        plugin.getCommand("getpoints").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameStats gameStats = GameStats.getInstance();

        // Check if a player name is provided as an argument
        if (args.length > 0) {
            // Look for a player with the specified name
            Player targetPlayer = plugin.getServer().getPlayer(args[0]);

            if (targetPlayer == null) {
                sender.sendMessage("Player not found.");
                return false;
            }

            // Get the target player's points
            int points = gameStats.getPlayerPoints(targetPlayer);

            if (points != 0) {
                sender.sendMessage(targetPlayer.getName() + " has " + points + " points.");
            } else {
                sender.sendMessage(targetPlayer.getName() + " has no recorded points.");
            }
        }
        return true;
    }
}
