package org.limaverse.minigamestructure.handlers.game.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class GameStartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Player> players;
    private final List<Player> spectators;

    public GameStartEvent(List<Player> players, List<Player> spectators) {
        this.players = players;
        this.spectators = spectators;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getSpectators() {
        return spectators;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
