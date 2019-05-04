package me.kickash32.distributedmobspawns;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import util.LongHashSet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class MobSpawnListener implements Listener {
    private DistributedMobSpawns controller;
    private HashMap<World, LongHashSet> blackLists;
    //public int buffer;
    //public HashMap<World, Integer> mobCaps;

    MobSpawnListener(DistributedMobSpawns controller){
        this.controller = controller;
        controller.getServer().getPluginManager().registerEvents(this, controller);

        blackLists = new HashMap<>();
        for(World world : controller.getServer().getWorlds()){
            blackLists.put(world, new LongHashSet());
        }
    }

    @EventHandler
    public void onPlayerNaturallySpawnCreaturesEvent(PlayerNaturallySpawnCreaturesEvent event){
        controller.serverPaperDetected();
        update(event.getPlayer(), event.getSpawnRadius());
    }

    void update(Player player, int radius){
        World world = player.getWorld();
        radius = 8;

        LongHashSet chunksFull = blackLists.get(world);
        LongHashSet playerChunks = new LongHashSet();

        Location location = player.getLocation();
        int ii = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
        int kk = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);
        int monsterCount = 0;
        double densityLimit = (double) (controller.getMobCaps().get(world) + controller.getBuffer()) / (17*17);

        //get Chunk info
        Chunk chunk;
        for(int i = -radius; i <= radius; i++){
            for(int k = -radius; k <= radius; k++){
                int chunkX = i + ii;
                int chunkZ = k + kk;

                if(!world.isChunkLoaded(chunkX, chunkZ)){ continue; }

                chunk = world.getChunkAt(chunkX, chunkZ);
                playerChunks.add(chunkX, chunkZ);
                for(Entity entity : chunk.getEntities()){
                    if(isMonster(entity)) {
                        monsterCount++;
                    }
                }
            }
        }

        //add or remove chunks from blacklist accordingly
        Iterator<Long> iteration = playerChunks.iterator();
        Long cnk;
        boolean tmp = (double)monsterCount/playerChunks.size() >= densityLimit;
        while (iteration.hasNext()) {
            cnk = iteration.next();
            //System
            if (tmp) {
                chunksFull.add(cnk);
            }
            else{
                chunksFull.remove(cnk);

                //if(world.getentities)//dynamic cap
            }
        }
    }

//    @EventHandler
//    public void onPreCreatureSpawnEvent(PreCreatureSpawnEvent event) {
//        controller.serverPaperDetected();
//        if (controller.isDisabled()){ return; }
//        if (!event.getReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)){ return; }
//        if(!isMonster(event.getType())){ return; }
//
//        System.out.println(isMonster(event.getType())==isMonster(event.getType()));
//
//        Location location = event.getSpawnLocation();
//        LongHashSet chunksFull = blackLists.get(location.getWorld());
//        int chunkX = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
//        int chunkZ = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);
//
//        if(chunksFull.contains(chunkX, chunkZ)) {
//            event.setShouldAbortSpawn(true);
//        }
//    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        //if(controller.runningOnPaper()){ return; }
        if (controller.isDisabled()){ return; }
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)){ return; }
        if(!isMonster(event.getEntityType())){ return; }

        Location location = event.getLocation();
        LongHashSet chunksFull = blackLists.get(location.getWorld());
        int chunkX = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
        int chunkZ = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);

        if(chunksFull.contains(chunkX, chunkZ)) {
            event.setCancelled(true);
        }
    }

    HashMap<World, HashSet<Long>> getBlacklist(){
        HashMap<World, HashSet<Long>> result = new HashMap<>();
        for(World world: blackLists.keySet()){
            result.put(world, new HashSet<>(blackLists.get(world).toArrayList()));
        }
        return result;
    }

    public static boolean isMonster(Entity entity){
        return (entity instanceof Monster && !(entity instanceof ElderGuardian)) ||
                        entity instanceof Slime ||
                        entity instanceof Ghast;
    }

    public static boolean isMonster(EntityType type){
        Class c = type.getEntityClass();
        return Monster.class.isAssignableFrom(c)||
                Slime.class.isAssignableFrom(c)||
                Ghast.class.isAssignableFrom(c);
    }

//    public static boolean isAnimal(EntityType type){//TO DO
//
//        return false;
//    }
//
//    public static boolean isWaterAnimal(EntityType type){//TO DO
//
//        return false;
//    }
//
//    public static boolean isAmbient(EntityType type){//TO DO
//
//        return false;
//    }
}
