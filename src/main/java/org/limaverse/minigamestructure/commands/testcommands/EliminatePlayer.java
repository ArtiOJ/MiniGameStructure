package org.limaverse.minigamestructure.commands.testcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.utilities.EliminationUtil;

import java.util.ArrayList;
import java.util.List;

public class EliminatePlayer implements CommandExecutor, TabCompleter {

    // Constructor to initialize the command executor.
    public EliminatePlayer(MiniGameStructure plugin) {
        plugin.getCommand("eliminate").setExecutor(this);
        plugin.getCommand("eliminate").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // Check if the correct number of arguments is provided
        if (args.length < 1 || args.length > 4) {
            sender.sendMessage("Usage: /eliminate <player> [kills] [wins] [time]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return true;
        }

        // If only one argument (the player name) is provided, call the single-argument constructor
        if (args.length == 1) {
            new EliminationUtil(target);
        } else {
            // Parse additional arguments if provided
            int kills = args.length > 1 ? Integer.parseInt(args[1]) : 0;
            int wins = args.length > 2 ? Integer.parseInt(args[2]) : 0;
            String timeSurvived = args.length > 3 ? args[3] : "0:00";

            // Eliminate the target player with the provided stats
            new EliminationUtil(target, kills, wins, timeSurvived);
        }

        sender.sendMessage("You have eliminated " + target.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest online players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                completions.add(onlinePlayer.getName());
            }
        } else if (args.length == 2) {
            // Suggest kills
            completions.add("<kills>");
        } else if (args.length == 3) {
            // Suggest wins
            completions.add("<wins>");
        } else if (args.length == 4) {
            // Suggest time
            completions.add("<time>");
        }

        return completions;
    }
}
