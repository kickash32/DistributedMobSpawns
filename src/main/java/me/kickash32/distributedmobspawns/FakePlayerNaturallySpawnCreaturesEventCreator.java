package me.kickash32.distributedmobspawns;

import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.World;
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
            Server server = controller.getServer();

            for (World world : server.getWorlds()) {
                for (Player player : world.getPlayers()){
                    if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
                        msl.update(player, controller.getSpawnRange());
                    }
                }
            }
        }
        catch(Exception ex){
            System.out.println("error running thread " + ex.getMessage());
        }
    }
}