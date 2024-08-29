package org.limaverse.minigamestructure.utilities;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;

public class ScoreboardUtility {
    private static Scoreboard scoreboard;

    static {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("gameStart", "dummy", "Game Status");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static Scoreboard getScoreboard() {
        return scoreboard;
    }
}
