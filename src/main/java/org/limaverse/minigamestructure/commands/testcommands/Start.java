package org.limaverse.minigamestructure.commands.testcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.playerhandlers.PreGameHandler;

public class Start implements CommandExecutor {


    // Constructor to initialize the ChestHandler and set the command executor.
    public Start(MiniGameStructure plugin) {

        plugin.getCommand("startgame").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PreGameHandler.getInstance().forceStart();
        return true;
    }
}
