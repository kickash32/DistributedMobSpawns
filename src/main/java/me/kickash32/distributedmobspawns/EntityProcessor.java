package me.kickash32.distributedmobspawns;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntityProcessor {

    DistributedMobSpawns controller;
    private Map<Player, Integer> proximityAnimals;
    private Map<Player, Integer> proximityMonsters;
    private Map<Player, Integer> proximityAmbients;
    private Map<Player, Integer> proximityWatermobs;

    public EntityProcessor(DistributedMobSpawns controller) {
        this.controller = controller;
        this.proximityAnimals = new HashMap<>();
        this.proximityMonsters = new HashMap<>();
        this.proximityAmbients = new HashMap<>();
        this.proximityWatermobs = new HashMap<>();
    }

    void update(Player player) {
        World world = player.getWorld();
        int radius = controller.getSpawnRange(world);
        Location location = player.getLocation();
        //get player's chunk co-ordinates
        int ii = (int) Math.floor(0.0 + location.getBlockX() / 16.0D);
        int kk = (int) Math.floor(0.0 + location.getBlockZ() / 16.0D);

        int animalCount = 0;
        int monsterCount = 0;
        int ambientCount = 0;
        int watermobCount = 0;

        Chunk chunk;
        int index;
        Entity[] chunkEntities;
        Entity entity;

        int chunkX;
        int chunkZ;
        for (int i = -radius; i <= radius; i++) {
            for (int k = -radius; k <= radius; k++) {
                chunkX = i + ii;
                chunkZ = k + kk;
                if (!world.isChunkLoaded(chunkX, chunkZ)) { continue; }

                chunk = world.getChunkAt(chunkX, chunkZ);
                chunkEntities = chunk.getEntities();
                for (index = 0; index < chunkEntities.length; index++) {
                    entity = chunkEntities[index];

                    if (Util.isNaturallySpawningAnimal(entity)) { animalCount++; }
                    else if (Util.isNaturallySpawningMonster(entity)) { monsterCount++; }
                    else if (Util.isNaturallySpawningAmbient(entity)) { ambientCount++; }
                    else if (Util.isNaturallySpawningWatermob(entity)) { watermobCount++; }
                }
            }
        }
        this.proximityAnimals.put(player, animalCount);
        this.proximityMonsters.put(player, monsterCount);
        this.proximityAmbients.put(player, ambientCount);
        this.proximityWatermobs.put(player, watermobCount);
    }

    boolean allFull(Player player) {
        World world = player.getWorld();
        return
                this.proximityAnimals.getOrDefault(player, 0) >= controller.getMobCapAnimals(world) &&
                        this.proximityMonsters.getOrDefault(player, 0) >= controller.getMobCapMonsters(world) &&
                        this.proximityAmbients.getOrDefault(player, 0) >= controller.getMobCapAmbient(world) &&
                        this.proximityWatermobs.getOrDefault(player, 0) >= controller.getMobCapWatermobs(world);
    }

    boolean isSpawnAllowed(Location location, EntityType type) {
        //new Exception().printStackTrace();
        World world = location.getWorld();

        int mobCap = -1;
        Map<Player, Integer> proximityMap = null;

        if (Util.isNaturallySpawningAnimal(type)) {
            mobCap = controller.getMobCapAnimals(world);
            proximityMap = proximityAnimals;
        } else if (Util.isNaturallySpawningMonster(type)) {
            mobCap = controller.getMobCapMonsters(world);
            proximityMap = proximityMonsters;
        } else if (Util.isNaturallySpawningAmbient(type)) {
            mobCap = controller.getMobCapAmbient(world);
            proximityMap = proximityAmbients;
        } else if (Util.isNaturallySpawningWatermob(type)) {
            mobCap = controller.getMobCapAmbient(world);
            proximityMap = proximityWatermobs;
        }

        boolean anyFull = false;
        Collection<Player> nearbyPlayers = Util.getPlayersInSquareRange(location, controller.getSpawnRange(location.getWorld()));
        for (Player player : nearbyPlayers) {
            if (proximityMap.getOrDefault(player, mobCap) >= mobCap) { anyFull = true; }
        }

        if (anyFull) { return false; }
        else {
            for (Player player : nearbyPlayers) {
                proximityMap.put(player, proximityMap.getOrDefault(player, mobCap) + 1);
            }
            return true;
        }
    }
}
