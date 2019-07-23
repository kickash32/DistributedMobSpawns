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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Util {
    private static Set<EntityType> animalTypes = generateAnimalTypes();
    private static Set<EntityType> monsterTypes = generateMonsterTypes();
    private static Set<EntityType> ambientTypes = generateAmbientTypes();
    private static Set<EntityType> watermobTypes = generateWatermobTypes();
    private static Set<EntityType> ignoredMobTypes = generateIgnoredMobTypes();

    private static Set<EntityType> generateAnimalTypes() {
        Set<EntityType> result = EnumSet.noneOf(EntityType.class);
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
        Set<EntityType> result = EnumSet.noneOf(EntityType.class);
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
        Set<EntityType> result = EnumSet.noneOf(EntityType.class);
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
        Set<EntityType> result = EnumSet.noneOf(EntityType.class);
        for (EntityType type : EntityType.values()) {
            Class c = type.getEntityClass();
            if (c != null &&
                    WaterMob.class.isAssignableFrom(c)) {
                result.add(type);
            }
        }
        return result;
    }

    private static Set<EntityType> generateIgnoredMobTypes() {
        Set<EntityType> result = EnumSet.noneOf(EntityType.class);
        try{
            result.add(EntityType.valueOf("PHANTOM"));
        } catch(IllegalArgumentException ex){
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public static List<Player> getPlayersInSquareRange(Location location, int rangeChunks) {
        int range = rangeChunks * 16;

        List<Player> players = location.getWorld().getPlayers();
        List<Player> filteredPlayers = new ArrayList<>();

        Location playerLocation;
        for(Player player : players){
            playerLocation = player.getLocation();

        if (player.getGameMode() == GameMode.SPECTATOR ||
                    !(Math.abs(playerLocation.getBlockX() - location.getBlockX()) < range &&
                      Math.abs(playerLocation.getBlockZ() - location.getBlockZ()) < range)) {
            filteredPlayers.add(player);
            }
        }
        return filteredPlayers;
    }

    public static int chunksInRadius(int radius) {
        return ((radius * 2) + 1) * ((radius * 2) + 1);
    }

    public static boolean isNaturallySpawningAnimal(Entity entity) {
        return isNaturallySpawningAnimal(entity.getType()) && wasNaturallySpawned(entity);
    }

    public static boolean isNaturallySpawningMonster(Entity entity) {
        return isNaturallySpawningMonster(entity.getType()) && wasNaturallySpawned(entity);
    }

    public static boolean isNaturallySpawningAmbient(Entity entity) {
        return isNaturallySpawningAmbient(entity.getType()) && wasNaturallySpawned(entity);
    }

    public static boolean isNaturallySpawningWatermob(Entity entity) {
        return isNaturallySpawningWatermob(entity.getType()) && wasNaturallySpawned(entity);
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

    public static boolean isIgnored(Entity entity) {
        return isIgnored(entity.getType());
    }

    public static boolean isIgnored(EntityType type) {
        return ignoredMobTypes.contains(type);
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
