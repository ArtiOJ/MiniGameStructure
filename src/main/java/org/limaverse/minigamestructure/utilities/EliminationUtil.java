package org.limaverse.minigamestructure.utilities;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.limaverse.minigamestructure.MiniGameStructure;

public class EliminationUtil {
    private final Player player;
    private final WorldChanger worldChanger;

    public EliminationUtil(Player player) {
        this.player = player;
        this.worldChanger = new WorldChanger(MiniGameStructure.getInstance());
        eliminatePlayer();
    }

    // Constructor with additional stats
    public EliminationUtil(Player player, int kills, int wins, String timeSurvived) {
        this.player = player;
        this.worldChanger = new WorldChanger(MiniGameStructure.getInstance());
        eliminatePlayer(kills, wins, timeSurvived);
    }

    private void eliminatePlayer() {
        // Clear player's potion effects and inventory
        clearPlayerEffectsAndInventory();

        // Send title to player
        player.sendTitle(
                Colorize.format("&cYou have been killed!"),
                Colorize.format("&7You can join back next round."),
                10, 70, 20
        );

        // Set player to spectator mode
        player.setGameMode(GameMode.SPECTATOR);

        // Change player's world to 'game_map'
        worldChanger.setWorld("game_map", player);

        // Send formatted elimination message
        player.sendMessage(Colorize.format("&e--------&cYou were eliminated!&e--------"));
    }

    private void eliminatePlayer(int kills, int wins, String timeSurvived) {
        // Clear player's potion effects and inventory
        clearPlayerEffectsAndInventory();

        // Send title to player
        player.sendTitle(
                Colorize.format("&cYou have been killed!"),
                Colorize.format("&7You can join back next round."),
                10, 70, 20
        );

        // Set player to spectator mode
        player.setGameMode(GameMode.SPECTATOR);

        // Change player's world to 'game_map'
        worldChanger.setWorld("game_map", player);

        // Send formatted elimination message with stats
        player.sendMessage(Colorize.format("&e--------&cYou were eliminated!&e--------"));
        player.sendMessage(Colorize.format("&6Kills: &f" + kills));
        player.sendMessage(Colorize.format("&6Wins: &f" + wins));
        player.sendMessage(Colorize.format("&6Time Survived: &f" + timeSurvived));
        player.sendMessage(Colorize.format("&e-----------------------------------"));
    }

    // Helper method to clear effects and inventory
    private void clearPlayerEffectsAndInventory() {
        // Clear potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Clear inventory
        player.getInventory().clear();
    }
}
