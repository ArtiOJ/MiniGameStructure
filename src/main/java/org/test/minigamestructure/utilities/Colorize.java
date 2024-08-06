package org.test.minigamestructure.utilities;

import org.bukkit.ChatColor;

public class Colorize {
    public Colorize() {
    }

    public static String format(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
