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
        if (controller.isDisabled()){ return; }
        Collection<? extends Player> onlinePlayers = controller.getServer().getOnlinePlayers();

        Thread[] myUpdateThreads = new Thread[onlinePlayers.size()];
        int index = 0;

        for (Player player : onlinePlayers) {
            if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
//                myUpdateThreads[index] =
//                        new Thread(() -> {
//                            try {
//                                System.out.println("wtf");
                                entityProcessor.update(player, controller.getSpawnRange(player.getWorld()));
//                                System.out.println("wtf2");
//                            }
//                            catch (Exception ignored){
//                                ignored.printStackTrace();
//                            }
//                            System.out.println("Thread finished");
//                        });
//                myUpdateThreads[index].start();
//                index++;
            }
        }
//        index = 0;
//        for(Thread thread : myUpdateThreads){
//            try {
//                if(thread != null) {
//                    System.out.println("Waiting on thread "+index);
//                    thread.join(1000);
//                }
//                index++;
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}