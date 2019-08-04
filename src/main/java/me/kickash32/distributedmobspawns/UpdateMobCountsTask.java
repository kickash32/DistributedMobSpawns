package me.kickash32.distributedmobspawns;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class UpdateMobCountsTask implements Runnable {
    private DistributedMobSpawns controller;
    private EntityProcessor entityProcessor;


    UpdateMobCountsTask(DistributedMobSpawns controller, EntityProcessor entityProcessor) {
        this.controller = controller;
        this.entityProcessor = entityProcessor;
    }

    public void run() {
        if (controller.isDisabled()) { return; }

        for (Player player : controller.getServer().getOnlinePlayers()) {
            if (player.getGameMode() != GameMode.SPECTATOR && player.getAffectsSpawning()) {
                entityProcessor.update(player);
            }
        }
    }
}