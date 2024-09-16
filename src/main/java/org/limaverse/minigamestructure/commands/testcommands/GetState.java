package org.limaverse.minigamestructure.commands.testcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.GameStateHandler;
import org.limaverse.minigamestructure.handlers.game.GameStates;
import org.limaverse.minigamestructure.utilities.Colorize;

public class GetState implements CommandExecutor {


    // Constructor to initialize the ChestHandler and set the command executor.
    public GetState(MiniGameStructure plugin) {

        plugin.getCommand("getstate").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameStates state = GameStateHandler.getInstance().getCurrentState();
        sender.sendMessage(Colorize.format("&eThe current game state is: &4" + state.toString()));
        return true;
    }
}
