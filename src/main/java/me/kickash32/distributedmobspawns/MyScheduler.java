package me.kickash32.distributedmobspawns;

import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.TimerTask;

public class MyScheduler extends TimerTask {
    private DistributedMobSpawns controller;


    MyScheduler(DistributedMobSpawns controller) {
        this.controller = controller;
    }

    public void run() {
        try {
            MobListener msl = controller.getListener();
            Server server = controller.getServer();

            msl.processQueues();
            for (World world : server.getWorlds()) {
                for (Player player : world.getPlayers()){
                    if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
                        msl.update(player, controller.getSpawnRange());
                    }
                }
            }
        }
        catch(Exception ex){
            System.out.println("[DMS] error running thread ");
            ex.printStackTrace();
        }
    }
}