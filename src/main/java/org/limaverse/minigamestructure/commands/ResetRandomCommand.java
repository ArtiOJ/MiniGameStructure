package org.limaverse.minigamestructure.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.ResetHandler;
import org.limaverse.minigamestructure.utilities.Colorize;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ResetRandomCommand implements CommandExecutor {

    private final ResetHandler resetHandler;
    private final MiniGameStructure plugin;

    // Constructor to initialize the ResetHandler and set the command executor and tab completer.
    public ResetRandomCommand(MiniGameStructure plugin) {
        this.resetHandler = new ResetHandler(plugin);
        this.plugin = plugin;
        plugin.getCommand("resetrandom").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the specified map exists.
        File mapFolder = new File(plugin.getDataFolder(), "maps");
        File[] mapsList = mapFolder.listFiles();
        List<File> mapArray = Arrays.stream(mapsList).toList();

        if (mapArray.isEmpty()) {
            sender.sendMessage(Colorize.format("&cThere are no maps in the folder maps folder."));
            return false;
        }

        Random random = new Random();
        File randomDirectiory = mapArray.get(random.nextInt(mapArray.size()));

        resetHandler.resetMap(randomDirectiory.getName());

        sender.sendMessage(Colorize.format("&aMap has been reset to: " + randomDirectiory.getName()));
        return true;
    }
}
