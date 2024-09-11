package org.limaverse.minigamestructure.handlers.game;

import org.limaverse.minigamestructure.handlers.game.events.RoundEndEvent;
import org.limaverse.minigamestructure.handlers.game.listeners.GameStartListener;
import org.limaverse.minigamestructure.handlers.game.playerhandlers.PreGameHandler;
import org.limaverse.minigamestructure.handlers.game.events.GameStartEvent;
import org.limaverse.minigamestructure.handlers.game.events.GameEndEvent;
import org.limaverse.minigamestructure.handlers.game.events.RoundStartEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GameStateHandler implements Listener {

    private GameStates currentState;
    private final PreGameHandler preGameHandler;
    private final GameStartListener gameStartListener;
    private static GameStateHandler instance;

    // Initialize the game state to PREGAME (or any other state you want)
    private GameStateHandler() {
        setGameState(GameStates.PREGAME);
        this.preGameHandler = PreGameHandler.getInstance();
        this.gameStartListener = GameStartListener.getInstance();
    }

    public static synchronized GameStateHandler getInstance() {
        if (instance == null) {
            instance = new GameStateHandler();
        }
        return instance;
    }

    // Get the current game state
    public GameStates getCurrentState() {
        return currentState;
    }

    // Set a new game state
    public void setGameState(GameStates newState) {
        this.currentState = newState;

        switch (newState) {
            case PREGAME:
                gameStartListener.startListener();
                preGameHandler.startPreGame();
                break;
        }
    }

    // Check if the game is in a specific state
    public boolean isState(GameStates state) {
        return this.currentState == state;
    }

    // Handle GameStartEvent and move to the STARTED state
    @EventHandler
    public void onGameStart(GameStartEvent event) {
        if (isState(GameStates.PREGAME)) {
            preGameHandler.unregister();
            gameStartListener.stopListener();
            setGameState(GameStates.STARTED);
            // Handle any additional game start logic
        }
    }

    // Handle GameEndEvent and move to the ENDED state
    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        if (isState(GameStates.STARTED)) {
            setGameState(GameStates.ENDED);
            // Handle any additional game end logic
        }
    }

    // Handle RoundStartEvent and move to the next round
    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        if (isState(GameStates.WAITING)) {
            setGameState(GameStates.STARTED);
        }
    }

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        if (isState(GameStates.STARTED)) {
            setGameState(GameStates.WAITING);
        }
    }
}
