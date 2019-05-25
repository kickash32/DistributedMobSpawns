package me.kickash32.distributedmobspawns;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityProcessor {

    private Map<Player, Integer> proximityAnimals;
    private Map<Player, Integer> proximityMonsters;
    private Map<Player, Integer> proximityAmbients;
    private Map<Player, Integer> proximityWatermobs;

//    private Map<World, Set<Entity>> spawnQueueAnimals;
//    private Map<World, Set<Entity>> spawnQueueMonsters;
//    private Map<World, Set<Entity>> spawnQueueAmbients;
//    private Map<World, Set<Entity>> spawnQueueWatermobs;

    DistributedMobSpawns controller;

    public EntityProcessor(DistributedMobSpawns controller){
        this.controller = controller;
        this.proximityAnimals = new ConcurrentHashMap<>();
        this.proximityMonsters = new ConcurrentHashMap<>();
        this.proximityAmbients = new ConcurrentHashMap<>();
        this.proximityWatermobs = new ConcurrentHashMap<>();

//        this.spawnQueueAnimals = new HashMap<>();
//        this.spawnQueueMonsters = new HashMap<>();
//        this.spawnQueueAmbients = new HashMap<>();
//        this.spawnQueueWatermobs = new HashMap<>();
//
//        for(World world : controller.getServer().getWorlds()){
//            this.spawnQueueAnimals.put(world, new HashSet<>());
//            this.spawnQueueMonsters.put(world, new HashSet<>());
//            this.spawnQueueAmbients.put(world, new HashSet<>());
//            this.spawnQueueWatermobs.put(world, new HashSet<>());
//        }
    }

    void update(Player player){
        World world = player.getWorld();
        int radius = controller.getSpawnRange(world);
        Location location = player.getLocation();
        //get player's chunk co-ordinates
        int ii = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
        int kk = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);

        int animalCount = 0;
        int monsterCount = 0;
        int ambientCount = 0;
        int watermobCount = 0;

        //get Chunk info
        Chunk chunk;
        int index;
        Entity[] chunkEntities;
        Entity entity;

        int chunkX;
        int chunkZ;
        for(int i = -radius; i <= radius; i++){
            for(int k = -radius; k <= radius; k++) {
                chunkX = i + ii;
                chunkZ = k + kk;
                if (!world.isChunkLoaded(chunkX, chunkZ)) { continue; }

                chunk = world.getChunkAt(chunkX, chunkZ);
                chunkEntities = chunk.getEntities();
                for (index = 0; index < chunkEntities.length; index++) {
                    entity = chunkEntities[index];

                for (Entity entity : chunk.getEntities()) {
                    if (Util.isNaturallySpawningAnimal(entity) ){//&& !this.spawnQueueAnimals.get(world).contains(entity)) {
                        animalCount++;
                    }
                    else if (Util.isNaturallySpawningMonster(entity) ){//&& !this.spawnQueueMonsters.get(world).contains(entity)) {
                        monsterCount++;
                    }
                    else if (Util.isNaturallySpawningAmbient(entity) ){//&& !this.spawnQueueAmbients.get(world).contains(entity)) {
                        ambientCount++;
                    }
                    else if (Util.isNaturallySpawningWatermob(entity) ){//&& !this.spawnQueueWatermobs.get(world).contains(entity)) {
                        watermobCount++;
                    }
                }
            }
        }
        this.proximityAnimals.put(player, animalCount);
        this.proximityMonsters.put(player, monsterCount);
        this.proximityAmbients.put(player, ambientCount);
        this.proximityWatermobs.put(player, watermobCount);
    }

    void enqueue(Entity entity){
        World world = entity.getLocation().getWorld();

        if(Util.isNaturallySpawningAnimal(entity)) {
            processEntity(entity, controller.getMobCapAnimals(world), proximityAnimals);
//            this.spawnQueueAnimals.get(world).add(entity);
        }
        else if(Util.isNaturallySpawningMonster(entity)) {
            processEntity(entity, controller.getMobCapMonsters(world), proximityMonsters);
//            this.spawnQueueMonsters.get(world).add(entity);
        }
        else if(Util.isNaturallySpawningAmbient(entity)) {
            processEntity(entity, controller.getMobCapAmbient(world), proximityAmbients);
//            this.spawnQueueAmbients.get(world).add(entity);
        }
        else if(Util.isNaturallySpawningWatermob(entity)) {
            processEntity(entity, controller.getMobCapWatermobs(world), proximityWatermobs);
//            this.spawnQueueWatermobs.get(world).add(entity);
        }
    }

//    void processQueues(){
//        Set<Entity> queue;
//        int mobCap;
//        Map<Player, Integer>proximityMap;
//        for(World world : controller.getServer().getWorlds()){
//
//            queue = this.spawnQueueAnimals.get(world);
//            mobCap = this.controller.getMobCapAnimals(world);
//            proximityMap = this.proximityAnimals;
//            for(Entity entity : queue){
//                processEntity(entity, mobCap, proximityMap);
//            }
//            queue.clear();
//
//            queue = this.spawnQueueMonsters.get(world);
//            mobCap = this.controller.getMobCapMonsters(world);
//            proximityMap = this.proximityMonsters;
//            for(Entity entity : queue){
//                processEntity(entity, mobCap, proximityMap);
//            }
//            queue.clear();
//
//            queue = this.spawnQueueAmbients.get(world);
//            mobCap = this.controller.getMobCapAmbient(world);
//            proximityMap = this.proximityAmbients;
//            for(Entity entity : queue){
//                processEntity(entity, mobCap, proximityMap);
//            }
//            queue.clear();
//
//            queue = this.spawnQueueWatermobs.get(world);
//            mobCap = this.controller.getMobCapWatermobs(world);
//            proximityMap = this.proximityWatermobs;
//            for(Entity entity : queue){
//                processEntity(entity, mobCap, proximityMap);
//            }
//            queue.clear();
//        }
//    }

    boolean processEntity(Entity entity, int mobCap, Map<Player, Integer>proximityMap){
        boolean anyFull = false;
        Collection<Player> nearbyPlayers = Util.getPlayersInSquareRange(entity.getLocation(), controller.getSpawnRange(entity.getWorld()));
        for (Player player : nearbyPlayers) {
            if (proximityMap.getOrDefault(player, mobCap) > mobCap) {
                anyFull = true;
            }
        }

        if(anyFull){
            entity.remove();
        }else{
            for(Player player: nearbyPlayers){
                proximityMap.put(player, proximityMap.getOrDefault(player, mobCap)+1);
            }
        }
        return !anyFull;
    }
}
