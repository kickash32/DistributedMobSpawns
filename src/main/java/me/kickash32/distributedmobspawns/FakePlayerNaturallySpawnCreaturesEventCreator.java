package me.kickash32.distributedmobspawns;

import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import java.util.Iterator;
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
            Iterator iterator = worldserver.getOnlinePlayers().iterator();

            while (iterator.hasNext()) {
                Player entityhuman = (Player) iterator.next();
                if (!entityhuman.getGameMode().equals(GameMode.SPECTATOR) ){
                    msl.update(entityhuman, 8);
                }
            }
        }
        catch(Exception ex){
            System.out.println("error running thread " + ex.getMessage());
        }
    }
}