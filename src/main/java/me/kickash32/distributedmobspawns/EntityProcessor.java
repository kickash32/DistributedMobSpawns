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

    DistributedMobSpawns controller;

    public EntityProcessor(DistributedMobSpawns controller){
        this.controller = controller;
        this.proximityAnimals = new ConcurrentHashMap<>();
        this.proximityMonsters = new ConcurrentHashMap<>();
        this.proximityAmbients = new ConcurrentHashMap<>();
        this.proximityWatermobs = new ConcurrentHashMap<>();
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

                    if (Util.isNaturallySpawningAnimal(entity) ){
                        animalCount++;
                    }
                    else if (Util.isNaturallySpawningMonster(entity) ){
                        monsterCount++;
                    }
                    else if (Util.isNaturallySpawningAmbient(entity) ){
                        ambientCount++;
                    }
                    else if (Util.isNaturallySpawningWatermob(entity) ){
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
        }
        else if(Util.isNaturallySpawningMonster(entity)) {
            processEntity(entity, controller.getMobCapMonsters(world), proximityMonsters);
        }
        else if(Util.isNaturallySpawningAmbient(entity)) {
            processEntity(entity, controller.getMobCapAmbient(world), proximityAmbients);
        }
        else if(Util.isNaturallySpawningWatermob(entity)) {
            processEntity(entity, controller.getMobCapWatermobs(world), proximityWatermobs);
        }
    }

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
