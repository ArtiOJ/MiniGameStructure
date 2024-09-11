//package org.limaverse.minigamestructure.commands;
//
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.jetbrains.annotations.NotNull;
//import org.limaverse.minigamestructure.MiniGameStructure;
//import org.limaverse.minigamestructure.gameevents.StartGameEvent;
//import org.limaverse.minigamestructure.handlers.GameStartHandler;
//
//import java.util.List;
//
//public class StartGameCommand implements CommandExecutor {
//    private final GameStartHandler gameStartHandler;
//
//    public StartGameCommand(MiniGameStructure plugin, GameStartHandler gameStartHandler) {
//        this.gameStartHandler = gameStartHandler;
//        plugin.getCommand("startgame").setExecutor(this);
//    }
//
//    @Override
//    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//        // Run player-related tasks on the main thread as they interact with Bukkit API.
//        gameStartHandler.forceStart();
//        return true;
//    }
//}
