package org.limaverse.minigamestructure.handlers.game.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class RoundEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Player> playersInGame;
    private final List<Player> playersSpectating;
    private final int roundNumber;
    private final Player winningPlayer;

    public RoundEndEvent(List<Player> playersInGame, List<Player> playersSpectating, int roundNumber, Player winningPlayer) {
        this.playersInGame = playersInGame;
        this.playersSpectating = playersSpectating;
        this.roundNumber = roundNumber;
        this.winningPlayer = winningPlayer;
    }

    public List<Player> getPlayersInGame() {
        return playersInGame;
    }

    public List<Player> getPlayersSpectating() {
        return playersSpectating;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public Player getWinningPlayer() {
        return winningPlayer;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
