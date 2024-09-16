package org.limaverse.minigamestructure.handlers.game.listeners;


import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.chesthandler.ChestSpawningHandler;
import org.limaverse.minigamestructure.handlers.game.events.RoundEndEvent;
import org.limaverse.minigamestructure.utilities.ChestGlowUtility;


public class RoundEndListener implements Listener {

    private static RoundEndListener instance;
    private RoundEndListener() {
        Bukkit.getPluginManager().registerEvents(this, MiniGameStructure.getInstance());
    }


    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        ChestSpawningHandler.getInstance().deleteChestGroups();
        ChestGlowUtility.remove();
    }

    public static synchronized RoundEndListener getInstance() {
        if (instance == null) {
            instance = new RoundEndListener();
        }
        return instance;
    }
}
