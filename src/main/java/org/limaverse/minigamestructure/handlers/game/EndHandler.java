package org.limaverse.minigamestructure.handlers.game;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.limaverse.minigamestructure.MiniGameStructure;
import org.limaverse.minigamestructure.handlers.game.events.GameStartEvent;
import org.limaverse.minigamestructure.handlers.game.events.RoundEndEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndHandler implements Listener {
    private List<Player> playersInGame;
    private List<Player> alivePlayers;
    private final Map<Player, Integer> playerWins;
    private MiniGameStructure plugin;
    private final int WINS = 2;
    private GameStateHandler gameStateHandler;
    private GameDataHandler gameDataHandler;

    public EndHandler() {
        this.playerWins = new HashMap<>();
        plugin = MiniGameStructure.getInstance();
        gameStateHandler = GameStateHandler.getInstance();
        gameDataHandler = GameDataHandler.getInstance();
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        playersInGame = event.getPlayers();
        alivePlayers = event.getPlayers();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (gameStateHandler.isState(GameStates.STARTED)) {
            if (alivePlayers.contains(event.getPlayer())) alivePlayers.remove(event.getPlayer());

            if (alivePlayers.size() <= 1) {

                if (playerWins.containsKey(event.getEntity())) {
                    playerWins.put(event.getEntity(), playerWins.get(event.getEntity()) + 1);
                } else {
                    playerWins.put(event.getEntity(), 1);
                }
                if (playerWins.get(event.getEntity()) >= WINS) {
                    gameDataHandler.endGame();
                    return;
                }
                gameDataHandler.endRound(event.getEntity());
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (gameStateHandler.isState(GameStates.STARTED)) {
            if (alivePlayers.contains(event.getPlayer())) alivePlayers.remove(event.getPlayer());

            if (alivePlayers.size() <= 1) {
                playerWins.put(alivePlayers.getFirst(), playerWins.get(alivePlayers.getFirst()) + 1);

                if (playerWins.get(alivePlayers.getFirst()) >= WINS) {
                    gameDataHandler.endGame();
                    return;
                }
                gameDataHandler.endRound(alivePlayers.getFirst());
            }
        }
    }
}
