package org.limaverse.minigamestructure.utilities;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;

public class ScoreboardUtility {
    private static final Scoreboard scoreboard; // Keep the scoreboard static to share it globally

    static {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("gameStart", "dummy", "Game Status");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    // Return the same scoreboard instance everywhere
    public static Scoreboard getScoreboard() {
        return scoreboard;
    }

    public static Objective getObjective() {
        return scoreboard.getObjective("gameStart");
    }
}
