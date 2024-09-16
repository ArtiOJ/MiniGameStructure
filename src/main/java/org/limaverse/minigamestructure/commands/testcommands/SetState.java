package org.limaverse.minigamestructure.commands.testcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.GameStateHandler;
import org.limaverse.minigamestructure.handlers.game.GameStates;
import org.limaverse.minigamestructure.utilities.Colorize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetState implements CommandExecutor, TabCompleter {

    public SetState(MiniGameStructure plugin) {
        // Register both the executor and the tab completer for the setstate command
        plugin.getCommand("setstate").setExecutor(this);
        plugin.getCommand("setstate").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player and has provided the correct argument.
        if (args.length != 1) {
            sender.sendMessage(Colorize.format("&cUsage: /setstate <state>"));
            return false;
        }

        // Try to parse the provided argument into a valid game state.
        try {
            GameStates newState = GameStates.valueOf(args[0].toUpperCase());
            GameStateHandler.getInstance().setGameState(newState);
            sender.sendMessage(Colorize.format("&eGame state has been set to: &4" + newState.toString()));
        } catch (IllegalArgumentException e) {
            // If the argument doesn't match any valid game state.
            sender.sendMessage(Colorize.format("&cInvalid game state. Available states: PREGAME, STARTED, WAITING, ENDED"));
            return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Provide a list of valid GameStates for tab completion when the player starts typing the state name.
        if (args.length == 1) {
            // Convert GameStates enum values to a list of strings
            List<String> states = Arrays.stream(GameStates.values())
                    .map(GameStates::name)
                    .collect(Collectors.toList());

            // If the user has started typing, return matching states, otherwise return all.
            return states.stream()
                    .filter(state -> state.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
