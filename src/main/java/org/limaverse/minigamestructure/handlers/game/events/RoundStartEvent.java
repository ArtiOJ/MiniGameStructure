package org.limaverse.minigamestructure.handlers.game.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class RoundStartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Player> playersInGame;
    private final List<Player> playersSpectating;
    private final int roundNumber;

    public RoundStartEvent(List<Player> playersInGame, List<Player> playersSpectating, int roundNumber) {
        this.playersInGame = playersInGame;
        this.playersSpectating = playersSpectating;
        this.roundNumber = roundNumber;
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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
