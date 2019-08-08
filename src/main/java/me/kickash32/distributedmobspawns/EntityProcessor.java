package me.kickash32.distributedmobspawns;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class EntityProcessor {

    private DistributedMobSpawns controller;
    private Map<UUID, Integer> proximityAnimals;
    private Map<UUID, Integer> proximityMonsters;
    private Map<UUID, Integer> proximityAmbients;
    private Map<UUID, Integer> proximityWatermobs;

    EntityProcessor(DistributedMobSpawns controller) {
        this.controller = controller;
        this.proximityAnimals = new HashMap<>();
        this.proximityMonsters = new HashMap<>();
        this.proximityAmbients = new HashMap<>();
        this.proximityWatermobs = new HashMap<>();
    }

    void update(Player player) {
        UUID playerID = player.getUniqueId();
        int radius = controller.getSpawnRange(player.getWorld()) * 16;

        int animalCount = 0;
        int monsterCount = 0;
        int ambientCount = 0;
        int watermobCount = 0;
        for(Entity entity: player.getWorld().getNearbyEntities(player.getLocation(), radius, 256, radius)){
            if (Util.isNaturallySpawningAnimal(entity)) { animalCount++; }
            else if (Util.isNaturallySpawningMonster(entity)) { monsterCount++; }
            else if (Util.isNaturallySpawningAmbient(entity)) { ambientCount++; }
            else if (Util.isNaturallySpawningWatermob(entity)) { watermobCount++; }
        }

        this.proximityAnimals.put(playerID, animalCount);
        this.proximityMonsters.put(playerID, monsterCount);
        this.proximityAmbients.put(playerID, ambientCount);
        this.proximityWatermobs.put(playerID, watermobCount);
    }

    boolean isSpawnAllowed(Location location, EntityType type) {
        World world = location.getWorld();

        int mobCap;
        Map<UUID, Integer> proximityMap;

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
        } else if (Util.isIgnored(type)) {
            return true;
        } else {
            System.out.println("[DMS] Error: unknown mob: " + type);
            return false; // TODO better error handling
        }

        Collection<Player> nearbyPlayers = Util.getPlayersInSquareRange(location, controller.getSpawnRange(location.getWorld()));
        for (Player player : nearbyPlayers) {
            if (proximityMap.getOrDefault(player.getUniqueId(), mobCap) >= mobCap) { return false; }
        }

        // Spawning is allowed
        for (Player player : nearbyPlayers) {
            proximityMap.put(player.getUniqueId(), proximityMap.getOrDefault(player.getUniqueId(), mobCap) + 1);
        }
        return true;
    }
}
