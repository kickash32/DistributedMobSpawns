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
    private HashMap<World, LongHashSet> whiteListsAnimals;//Each world has its own whitelist of chunks(stored as a set of location hashes) where monsters can spawn
    private HashMap<World, LongHashSet> whiteListsMonsters;
    private HashMap<World, LongHashSet> whiteListsAmbient;
    private HashMap<World, LongHashSet> whiteListsWatermobs;

    MobSpawnListener(DistributedMobSpawns controller){
        this.controller = controller;
        controller.getServer().getPluginManager().registerEvents(this, controller);

        whiteListsAnimals = new HashMap<>();
        whiteListsMonsters = new HashMap<>();
        whiteListsAmbient = new HashMap<>();
        whiteListsWatermobs = new HashMap<>();
        reset();
    }

    void reset(){
        for(World world : controller.getServer().getWorlds()){
            whiteListsAnimals.put(world, new LongHashSet());
            whiteListsMonsters.put(world, new LongHashSet());
            whiteListsAmbient.put(world, new LongHashSet());
            whiteListsWatermobs.put(world, new LongHashSet());
        }
    }

    private LongHashSet getWhiteListAnimals(World world){
        return whiteListsAnimals.get(world);
    }
    private LongHashSet getWhiteListMonsters(World world){
        return whiteListsMonsters.get(world);
    }
    private LongHashSet getWhiteListAmbient(World world){
        return whiteListsAmbient.get(world);
    }
    private LongHashSet getWhiteListWatermobs(World world){
        return whiteListsWatermobs.get(world);
    }

    HashSet<Long> getWhitelistAnimalsImmutable(World world){
        return new HashSet<>(getWhiteListMonsters(world).toArrayList());
    }
    HashSet<Long> getWhitelistMonstersImmutable(World world){
        return new HashSet<>(getWhiteListMonsters(world).toArrayList());
    }
    HashSet<Long> getWhitelistAmbientImmutable(World world){
        return new HashSet<>(getWhiteListMonsters(world).toArrayList());
    }
    HashSet<Long> getWhitelistWatermobsImmutable(World world){
        return new HashSet<>(getWhiteListMonsters(world).toArrayList());
    }
//    @EventHandler //removed until a better way is found for supporting spigot and paper optimizations at the same time
//    public void onPlayerNaturallySpawnCreaturesEvent(PlayerNaturallySpawnCreaturesEvent event){
//        controller.serverPaperDetected();
//        update(event.getPlayer(), event.getSpawnRadius());
//    }

    void update(Player player, int radius){
        radius = 8;
        World world = player.getWorld();

        LongHashSet whiteListChunks = getWhiteListMonsters(world);
        LongHashSet playerChunks = new LongHashSet();

        Location location = player.getLocation();
        //get player's chunk co-ordinates
        int ii = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
        int kk = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);
        int monsterCount = 0;
        //the maximum density for each player is defined as the mobcap distributed over 17x17 chunks (refer to mojang's code)
        double densityLimit = (double) (controller.getMobCapMonsters(world) + controller.getBuffer()) / controller.chunksInRadius(radius);

        //get Chunk info
        Chunk chunk;
        for(int i = -radius; i <= radius; i++){
            for(int k = -radius; k <= radius; k++) {
                //ignore chunks that are more than 128 blocks away
                if (i * i + k * k >= (radius + 1) * (radius + 1)) { continue; }
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

        //add or remove chunks from whitelist accordingly
        Iterator<Long> iteration = playerChunks.iterator();
        Long cnk;
        boolean tmp = (double)(monsterCount)/controller.chunksInRadius(radius) > densityLimit;
        while (iteration.hasNext()) {
            cnk = iteration.next();
            if (tmp) {
                whiteListChunks.remove(cnk);
            }
            else{
                whiteListChunks.add(cnk);
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
        if(!isNaturallySpawningMonster(event.getEntity())){ return; }

        Location location = event.getLocation();
        LongHashSet whitelist = getWhiteListMonsters(location.getWorld());
        int chunkX = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
        int chunkZ = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);

        if(!whitelist.contains(chunkX, chunkZ)) {
            event.setCancelled(true);
        }
    }

    static boolean isNaturallySpawningAnimal(Entity entity){
        if (entity == null) { return false; }
        return isAnimal(entity.getType()) &&
                entity.getEntitySpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL);
    }
    static boolean isAnimal(EntityType type){
        if (type == EntityType.UNKNOWN || type == null) { return false; }
        Class c = type.getEntityClass();
        return Animals.class.isAssignableFrom(c);
    }

    static boolean isNaturallySpawningMonster(Entity entity){
        if (entity == null) { return false; }
        return isMonster(entity.getType()) &&
                entity.getEntitySpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL);
    }
    static boolean isMonster(EntityType type){
        if (type == EntityType.UNKNOWN || type == null) { return false; }
        Class c = type.getEntityClass();
        boolean result = (Monster.class.isAssignableFrom(c)||
                Slime.class.isAssignableFrom(c)||
                Ghast.class.isAssignableFrom(c));
        return result &&
                !Phantom.class.isAssignableFrom(c); //phantoms do not count towards monsters mobcap
    }

    static boolean isNaturallySpawningAmbient(Entity entity){
        if (entity == null) { return false; }
        return isAmbient(entity.getType()) &&
                entity.getEntitySpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL);
    }
    static boolean isAmbient(EntityType type){
        if (type == EntityType.UNKNOWN || type == null) { return false; }
        Class c = type.getEntityClass();
        return Ambient.class.isAssignableFrom(c);
    }

    static boolean isNaturallySpawningWatermob(Entity entity){
        if (entity == null) { return false; }
        return isWatermob(entity.getType()) &&
                entity.getEntitySpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL);
    }
    static boolean isWatermob(EntityType type){
        if (type == EntityType.UNKNOWN || type == null) { return false; }
        Class c = type.getEntityClass();
        return WaterMob.class.isAssignableFrom(c);
    }
}
