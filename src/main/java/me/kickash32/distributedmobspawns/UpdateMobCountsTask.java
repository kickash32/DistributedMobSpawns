package me.kickash32.distributedmobspawns;

import io.papermc.lib.PaperLib;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class UpdateMobCountsTask implements Runnable {
    private DistributedMobSpawns controller;
    private EntityProcessor entityProcessor;
    private boolean isPaper;


    UpdateMobCountsTask(DistributedMobSpawns controller, EntityProcessor entityProcessor) {
        this.controller = controller;
        this.entityProcessor = entityProcessor;
        isPaper = PaperLib.isPaper();
    }

    public void run() {
        if (controller.isDisabled()) { return; }

        for (Player player : controller.getServer().getOnlinePlayers()) {
            if (player.getGameMode() != GameMode.SPECTATOR &&
                    ( !isPaper || player.getAffectsSpawning())) {
                entityProcessor.update(player);
            }
        }
    }
}