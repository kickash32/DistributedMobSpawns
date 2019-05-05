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

import java.util.*;


public class MobSpawnListener implements Listener {
    private DistributedMobSpawns controller;
    private HashMap<World, LongHashSet> blackListsMonsters;//Each world has its own blacklist of chunks(stored as a set of location hashes)

    MobSpawnListener(DistributedMobSpawns controller){
        this.controller = controller;
        controller.getServer().getPluginManager().registerEvents(this, controller);

        blackListsMonsters = new HashMap<>();
        for(World world : controller.getServer().getWorlds()){
            blackListsMonsters.put(world, new LongHashSet());
        }
    }

    private LongHashSet getBlackListMonsters(World world){
        return blackListsMonsters.get(world);
    }


    @EventHandler
    public void onPlayerNaturallySpawnCreaturesEvent(PlayerNaturallySpawnCreaturesEvent event){
        controller.serverPaperDetected();
        update(event.getPlayer(), event.getSpawnRadius());
    }

    void update(Player player, int radius){
        World world = player.getWorld();
        radius = 8;

        LongHashSet chunksFull = getBlackListMonsters(world);
        LongHashSet playerChunks = new LongHashSet();

        Location location = player.getLocation();
        //get player's chunk co-ordinates
        int ii = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
        int kk = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);
        int monsterCount = 0;
        //the maximum density for each player is defined as the mobcap distributed over 17x17 chunks (refer to mojang's code)
        double densityLimit = (double) (controller.getMobCapMonsters(world) + controller.getBuffer()) / (17*17);

        //get Chunk info
        Chunk chunk;
        for(int i = -radius; i <= radius; i++){
            for(int k = -radius; k <= radius; k++) {
                //ignore chunks that are more than 128 blocks away
                if (i * i + k * k >= (8 + 1) * (8 + 1)) { continue; }
                int chunkX = i + ii;
                int chunkZ = k + kk;
                if (!world.isChunkLoaded(chunkX, chunkZ)) { continue; }

                chunk = world.getChunkAt(chunkX, chunkZ);
                playerChunks.add(chunkX, chunkZ);
                for (Entity entity : chunk.getEntities()) {
                    if (isNaturallySpawningMonster(entity)) {
                        monsterCount++;
                    }
                }
            }
        }

        //add or remove chunks from blacklist accordingly
        Iterator<Long> iteration = playerChunks.iterator();
        Long cnk;
        boolean tmp = (double)(monsterCount)/playerChunks.size() >= densityLimit;
        while (iteration.hasNext()) {
            cnk = iteration.next();
            if (tmp) {
                chunksFull.add(cnk);
            }
            else{
                chunksFull.remove(cnk);

                //if(world.getentities)//dynamic cap
            }
        }
    }

    //event broken in paper 1.13.1
//    @EventHandler
//    public void onPreCreatureSpawnEvent(PreCreatureSpawnEvent event) {
//        controller.serverPaperDetected();
//        if (controller.isDisabled()){ return; }
//        if (!event.getReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)){ return; }
//        if(!isNaturallySpawningMonster(event.getType())){ return; }
//
//        System.out.println(isNaturallySpawningMonster(event.getType())==isNaturallySpawningMonster(event.getType()));
//
//        Location location = event.getSpawnLocation();
//        LongHashSet chunksFull = blackListsMonsters.get(location.getWorld());
//        int chunkX = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
//        int chunkZ = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);
//
//        if(chunksFull.contains(chunkX, chunkZ)) {
//            event.setShouldAbortSpawn(true);
//        }
//    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        //if(controller.runningOnPaper()){ return; }//disabled due to paper onPreCreatureSpawnEvent broken
        if (controller.isDisabled()){ return; }
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)){ return; }
        if(!isNaturallySpawningMonster(event.getEntityType())){ return; }

        Location location = event.getLocation();
        LongHashSet chunksFull = getBlackListMonsters(location.getWorld());
        int chunkX = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
        int chunkZ = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);

        if(chunksFull.contains(chunkX, chunkZ)) {
            event.setCancelled(true);
        }
    }

    //get a copy of the blacklist
    HashMap<World, HashSet<Long>> getBlacklist(){
        HashMap<World, HashSet<Long>> result = new HashMap<>();
        for(World world: blackListsMonsters.keySet()){
            result.put(world, new HashSet<>(blackListsMonsters.get(world).toArrayList()));
        }
        return result;
    }

    static boolean isNaturallySpawningMonster(Entity entity){
        if (entity == null) { return false; }
        return isNaturallySpawningMonster(entity.getType());
    }

    static boolean isNaturallySpawningMonster(EntityType type){
        if (type == EntityType.UNKNOWN || type == null) { return false; }
        Class c = type.getEntityClass();
        return (Monster.class.isAssignableFrom(c)||
                Slime.class.isAssignableFrom(c)||
                Ghast.class.isAssignableFrom(c)) &&
                !ElderGuardian.class.isAssignableFrom(c);//guardian does not spawn naturally
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
