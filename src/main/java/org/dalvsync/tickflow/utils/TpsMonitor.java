package org.dalvsync.tickflow.utils;

import org.bukkit.Bukkit;

public class TpsMonitor {

    public static boolean isLagging(double threshold) {
        try {
            double currentTps = Bukkit.getServer().getTPS()[0];
            return currentTps < threshold;
        } catch (Exception | NoSuchMethodError e) {
            return false;
        }
    }
}