package me.kickash32.distributedmobspawns;

import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import java.util.TimerTask;

public class FakePlayerNaturallySpawnCreaturesEventCreator extends TimerTask {
    private DistributedMobSpawns controller;


    FakePlayerNaturallySpawnCreaturesEventCreator(DistributedMobSpawns controller) {
        this.controller = controller;
    }

    public void run() {
        try {
            MobSpawnListener msl = controller.getListener();
            Server worldserver = controller.getServer();

            for (Player entityhuman : worldserver.getOnlinePlayers()) {
                if (!entityhuman.getGameMode().equals(GameMode.SPECTATOR)) {
                    msl.update(entityhuman, controller.getSpawnRange());
                }
            }
        }
        catch(Exception ex){
            System.out.println("error running thread " + ex.getMessage());
        }
    }
}