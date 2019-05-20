package me.kickash32.distributedmobspawns;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;


public class MobListener implements Listener {
    private DistributedMobSpawns controller;

    private Map<Player, Integer> proximityAnimals;
    private Map<Player, Integer> proximityMonsters;
    private Map<Player, Integer> proximityAmbients;
    private Map<Player, Integer> proximityWatermobs;

    private Map<World, Queue<Entity>> spawnQueueAnimals;
    private Map<World, Queue<Entity>> spawnQueueMonsters;
    private Map<World, Queue<Entity>> spawnQueueAmbients;
    private Map<World, Queue<Entity>> spawnQueueWatermobs;

    MobListener(DistributedMobSpawns controller){
        this.controller = controller;
        controller.getServer().getPluginManager().registerEvents(this, controller);

        spawnQueueAnimals = new HashMap<>();
        spawnQueueMonsters = new HashMap<>();
        spawnQueueAmbients = new HashMap<>();
        spawnQueueWatermobs = new HashMap<>();

        for(World world : controller.getServer().getWorlds()){
            proximityAnimals = new ConcurrentHashMap<>();
            proximityMonsters = new ConcurrentHashMap<>();
            proximityAmbients = new ConcurrentHashMap<>();
            proximityWatermobs = new ConcurrentHashMap<>();

            spawnQueueAnimals.put(world, new ConcurrentLinkedDeque<>());
            spawnQueueMonsters.put(world, new ConcurrentLinkedDeque<>());
            spawnQueueAmbients.put(world, new ConcurrentLinkedDeque<>());
            spawnQueueWatermobs.put(world, new ConcurrentLinkedDeque<>());
        }
    }

    void update(Player player, int radius){
        World world = player.getWorld();
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
        for(int i = -radius; i <= radius; i++){
            for(int k = -radius; k <= radius; k++) {
                int chunkX = i + ii;
                int chunkZ = k + kk;
                if (!world.isChunkLoaded(chunkX, chunkZ)) { continue; }

                chunk = world.getChunkAt(chunkX, chunkZ);

                for (Entity entity : chunk.getEntities()) {
                    if (Util.isNaturallySpawningAnimal(entity)) {
                        animalCount++;
                    }
                    else if (Util.isNaturallySpawningMonster(entity)) {
                        monsterCount++;
                    }
                    else if (Util.isNaturallySpawningAmbient(entity)) {
                        ambientCount++;
                    }
                    else if (Util.isNaturallySpawningWatermob(entity)) {
                        watermobCount++;
                    }
                }
            }
        }

        proximityAnimals.put(player, animalCount);
        proximityMonsters.put(player, monsterCount);
        proximityAmbients.put(player, ambientCount);
        proximityWatermobs.put(player, watermobCount);
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (controller.isDisabled()){ return; }
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL){ return; }

        World world = event.getLocation().getWorld();

        if(Util.isNaturallySpawningAnimal(event.getEntity())) {
             spawnQueueAnimals.get(world).add(event.getEntity());
        }
        else if(Util.isNaturallySpawningMonster(event.getEntity())) {
            spawnQueueMonsters.get(world).add(event.getEntity());
        }
        else if(Util.isNaturallySpawningAmbient(event.getEntity())) {
            spawnQueueAmbients.get(world).add(event.getEntity());
        }
        else if(Util.isNaturallySpawningWatermob(event.getEntity())) {
            spawnQueueWatermobs.get(world).add(event.getEntity());
        }
    }

    void processQueues(){
        if(controller.isDisabled()){ return; }

        Queue<Entity> queue;
        int mobCap;
        for(World world : controller.getServer().getWorlds()){
            queue = spawnQueueAnimals.get(world);
            mobCap = controller.getMobCapAnimals(world);
            while(!queue.isEmpty()){
                processEntity(queue.remove(), mobCap, proximityAnimals);
            }

            queue = spawnQueueMonsters.get(world);
            mobCap = controller.getMobCapMonsters(world);
            while(!queue.isEmpty()){
                processEntity(queue.remove(), mobCap, proximityMonsters);
            }

            queue = spawnQueueAmbients.get(world);
            mobCap = controller.getMobCapAmbient(world);
            while(!queue.isEmpty()){
                processEntity(queue.remove(), mobCap, proximityAmbients);
            }

            queue = spawnQueueWatermobs.get(world);
            mobCap = controller.getMobCapWatermobs(world);
            while(!queue.isEmpty()){
                processEntity(queue.remove(), mobCap, proximityWatermobs);
            }
        }
    }

    private void processEntity(Entity entity, int mobCap, Map<Player, Integer>proximityMap){
        boolean anyFull = false;
        Collection<Player> players = Util.getPlayersInSquareRange(entity.getLocation(), controller.getSpawnRange());
        for(Player player: players){
            if(proximityMap.get(player) >= mobCap){
                anyFull = true;
            }
        }

        if(anyFull){
            entity.remove();
        }else{
            for(Player player: players){
                proximityMap.put(player, proximityMap.get(player)+1);
            }
        }
    }
}
