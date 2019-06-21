package me.kickash32.distributedmobspawns;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class Util {
    private static Set<EntityType> animalTypes = generateAnimalTypes();
    private static Set<EntityType> monsterTypes = generateMonsterTypes();
    private static Set<EntityType> ambientTypes = generateAmbientTypes();
    private static Set<EntityType> watermobTypes = generateWatermobTypes();

    private static Set<EntityType> generateAnimalTypes() {
        Set<EntityType> result = new HashSet<>();
        for (EntityType type : EntityType.values()) {
            Class c = type.getEntityClass();
            if (c != null &&
                    Animals.class.isAssignableFrom(c)) {
                result.add(type);
            }
        }
        return result;
    }

    private static Set<EntityType> generateMonsterTypes() {
        Set<EntityType> result = new HashSet<>();
        for (EntityType type : EntityType.values()) {
            Class c = type.getEntityClass();
            if (c != null && (
                    Monster.class.isAssignableFrom(c) ||
                            Slime.class.isAssignableFrom(c) ||
                            Ghast.class.isAssignableFrom(c))) {
                result.add(type);
            }
        }
        return result;
    }

    private static Set<EntityType> generateAmbientTypes() {
        Set<EntityType> result = new HashSet<>();
        for (EntityType type : EntityType.values()) {
            Class c = type.getEntityClass();
            if (c != null &&
                    Ambient.class.isAssignableFrom(c)) {
                result.add(type);
            }
        }
        return result;
    }

    private static Set<EntityType> generateWatermobTypes() {
        Set<EntityType> result = new HashSet<>();
        for (EntityType type : EntityType.values()) {
            Class c = type.getEntityClass();
            if (c != null &&
                    WaterMob.class.isAssignableFrom(c)) {
                result.add(type);
            }
        }
        return result;
    }

    public static List<Player> getNearbyPlayers(Location loc, int distance) {
        int distanceSquared = distance * distance;

        List<Player> list = new ArrayList<>();
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                continue;
            }
            if (player.getLocation().distanceSquared(loc) < distanceSquared) {
                list.add(player);
            }
        }
        return list;
    }

    public static Collection<Player> getPlayersInSquareRange(Location location, int rangeChunks) {
        int range = rangeChunks * 17;

        Collection<Player> playerCollection = getNearbyPlayers(location, rangeChunks * 23);

        Iterator<Player> iterator = playerCollection.iterator();
        Player player;
        Location playerLocation;
        while (iterator.hasNext()) {
            player = iterator.next();
            playerLocation = player.getLocation();

            if (Math.abs(playerLocation.getBlockX() - location.getBlockX()) < range &&
                    Math.abs(playerLocation.getBlockZ() - location.getBlockZ()) < range) {
            } else {
                iterator.remove();
            }
        }
        return playerCollection;
    }

    public static int chunksInRadius(int radius) {
        return ((radius * 2) + 1) * ((radius * 2) + 1);
    }

    public static boolean isNaturallySpawningAnimal(Entity entity) {
        if (entity == null) {
            return false;
        }
        return animalTypes.contains(entity.getType()) && wasNaturallySpawned(entity);
    }

    public static boolean isNaturallySpawningMonster(Entity entity) {
        if (entity == null) {
            return false;
        }
        return monsterTypes.contains(entity.getType()) && wasNaturallySpawned(entity);
    }

    public static boolean isNaturallySpawningAmbient(Entity entity) {
        if (entity == null) {
            return false;
        }
        return ambientTypes.contains(entity.getType()) && wasNaturallySpawned(entity);
    }

    public static boolean isNaturallySpawningWatermob(Entity entity) {
        if (entity == null) {
            return false;
        }
        return watermobTypes.contains(entity.getType()) && wasNaturallySpawned(entity);
    }

    public static boolean isNaturallySpawningAnimal(EntityType type) {
        return animalTypes.contains(type);
    }

    public static boolean isNaturallySpawningMonster(EntityType type) {
        return monsterTypes.contains(type);
    }

    public static boolean isNaturallySpawningAmbient(EntityType type) {
        return ambientTypes.contains(type);
    }

    public static boolean isNaturallySpawningWatermob(EntityType type) {
        return watermobTypes.contains(type);
    }

    public static boolean wasNaturallySpawned(Entity entity) {
        DistributedMobSpawns controller = DistributedMobSpawns.getController();
        if (controller.getCountOnlyNaturalSpawned(entity.getWorld())) {
            return entity.getEntitySpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL);
        } else {
            return true;
        }
    }
}
