package org.limaverse.minigamestructure.handlers.game.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.*;

public class GameEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final HashMap<Player, Integer> playerPoints;
    private final List<Player> spectators;
    private final List<Map.Entry<Player, Integer>> sortedPlayers;

    public GameEndEvent(HashMap<Player, Integer> playerPoints, List<Player> spectators) {
        this.playerPoints = playerPoints;
        this.spectators = spectators;

        // Sort players by points in descending order
        this.sortedPlayers = new ArrayList<>(playerPoints.entrySet());
        this.sortedPlayers.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    }

    public HashMap<Player, Integer> getPlayerPoints() {
        return playerPoints;
    }

    public List<Player> getSpectators() {
        return spectators;
    }

    // Get the winner (first place)
    public Player getWinner() {
        return sortedPlayers.isEmpty() ? null : sortedPlayers.get(0).getKey();
    }

    // Get the second place player
    public Player getSecond() {
        return sortedPlayers.size() < 2 ? null : sortedPlayers.get(1).getKey();
    }

    // Get the third place player
    public Player getThird() {
        return sortedPlayers.size() < 3 ? null : sortedPlayers.get(2).getKey();
    }

    // Get player at a specific place (1st, 2nd, 3rd, etc.)
    public Player getPlayer(int place) {
        if (place <= 0 || place > sortedPlayers.size()) {
            return null; // Return null if the place is out of bounds
        }
        return sortedPlayers.get(place - 1).getKey(); // place is 1-based index
    }

    // Get the place (rank) of a specific player
    public int getPlace(Player player) {
        for (int i = 0; i < sortedPlayers.size(); i++) {
            if (sortedPlayers.get(i).getKey().equals(player)) {
                return i + 1; // return 1-based index
            }
        }
        return -1; // Return -1 if player is not found
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
