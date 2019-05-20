package me.kickash32.distributedmobspawns;

import org.bukkit.Location;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Util {

    static List<Player> getNearbyPlayers(Location loc, int distance)
    {
        int distanceSquared = distance*distance;

        List<Player> list = new ArrayList<>();
        for(Player player: loc.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) < distanceSquared) {
                list.add(player);
            }
        }
        return list;
    }

    static Collection<Player> getPlayersInSquareRange(Location location, int rangeChunks){
        int range = rangeChunks * 16;

        Collection<Player> playerCollection = getNearbyPlayers(location,rangeChunks*23);

        Iterator<Player> iterator = playerCollection.iterator();
        Player player;
        Location playerLocation;
        while(iterator.hasNext()){
            player = iterator.next();
            playerLocation = player.getLocation();

            if(Math.abs(playerLocation.getBlockX() - location.getBlockX()) < range &&
                    Math.abs(playerLocation.getBlockZ() - location.getBlockZ()) < range){
            }
            else{
                iterator.remove();
            }
        }
        return playerCollection;
    }

    static int chunksInRadius(int radius){
        return ((radius*2)+1)*((radius*2)+1);
    }

    static boolean isNaturallySpawningAnimal(Entity entity){
        if (entity == null) { return false; }
        return isAnimal(entity.getType());
    }
    static boolean isAnimal(EntityType type){
        if (type == EntityType.UNKNOWN || type == null) { return false; }
        Class c = type.getEntityClass();
        return Animals.class.isAssignableFrom(c);
    }

    static boolean isNaturallySpawningMonster(Entity entity){
        if (entity == null) { return false; }
        return isMonster(entity.getType());
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
        return isAmbient(entity.getType());
    }
    static boolean isAmbient(EntityType type){
        if (type == EntityType.UNKNOWN || type == null) { return false; }
        Class c = type.getEntityClass();
        return Ambient.class.isAssignableFrom(c);
    }

    static boolean isNaturallySpawningWatermob(Entity entity){
        if (entity == null) { return false; }
        return isWatermob(entity.getType());
    }
    static boolean isWatermob(EntityType type){
        if (type == EntityType.UNKNOWN || type == null) { return false; }
        Class c = type.getEntityClass();
        return WaterMob.class.isAssignableFrom(c);
    }
}
