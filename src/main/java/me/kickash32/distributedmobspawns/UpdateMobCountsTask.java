package me.kickash32.distributedmobspawns;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Collection;

public class UpdateMobCountsTask implements Runnable {
    private DistributedMobSpawns controller;
    private EntityProcessor entityProcessor;


    UpdateMobCountsTask(DistributedMobSpawns controller, EntityProcessor entityProcessor) {
        this.controller = controller;
        this.entityProcessor = entityProcessor;
    }

    public void run() {
        if (controller.isDisabled()) { return; }

        Collection<? extends Player> onlinePlayers = controller.getServer().getOnlinePlayers();
        for (Player player : onlinePlayers) {
            if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
                entityProcessor.update(player);
            }
        }
    }
}