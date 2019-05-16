package me.kickash32.distributedmobspawns;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.*;


public class MobSpawnListener implements Listener {
    private DistributedMobSpawns controller;

    private HashMap<Player, Integer> proximityAnimals;
    private HashMap<Player, Integer> proximityMonsters;
    private HashMap<Player, Integer> proximityAmbients;
    private HashMap<Player, Integer> proximityWatermobs;

    private HashMap<World, List<Entity>> spawnQueueAnimals;
    private HashMap<World, List<Entity>> spawnQueueMonsters;
    private HashMap<World, List<Entity>> spawnQueueAmbients;
    private HashMap<World, List<Entity>> spawnQueueWatermobs;

    MobSpawnListener(DistributedMobSpawns controller){
        this.controller = controller;
        controller.getServer().getPluginManager().registerEvents(this, controller);

        spawnQueueAnimals = new HashMap<>();
        spawnQueueMonsters = new HashMap<>();
        spawnQueueAmbients = new HashMap<>();
        spawnQueueWatermobs = new HashMap<>();
        reset();
    }

    void reset(){
        for(World world : controller.getServer().getWorlds()){
            proximityAnimals = new HashMap<>();
            proximityMonsters = new HashMap<>();
            proximityAmbients = new HashMap<>();
            proximityWatermobs = new HashMap<>();

            spawnQueueAnimals.put(world, new ArrayList<>());
            spawnQueueMonsters.put(world, new ArrayList<>());
            spawnQueueAmbients.put(world, new ArrayList<>());
            spawnQueueWatermobs.put(world, new ArrayList<>());
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
                    if (isNaturallySpawningAnimal(entity)) {
                        animalCount++;
                    }
                    else if (isNaturallySpawningMonster(entity)) {
                        monsterCount++;
                    }
                    else if (isNaturallySpawningAmbient(entity)) {
                        ambientCount++;
                    }
                    else if (isNaturallySpawningWatermob(entity)) {
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

        if(isNaturallySpawningAnimal(event.getEntity())) {
             spawnQueueAnimals.get(world).add(event.getEntity());
        }
        else if(isNaturallySpawningMonster(event.getEntity())) {
            spawnQueueMonsters.get(world).add(event.getEntity());
        }
        else if(isNaturallySpawningAmbient(event.getEntity())) {
            spawnQueueAmbients.get(world).add(event.getEntity());
        }
        else if(isNaturallySpawningWatermob(event.getEntity())) {
            spawnQueueWatermobs.get(world).add(event.getEntity());
        }
    }

    public List<Player> getNearbyPlayers(Location loc, int distance)
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

    private Collection<Player> getPlayersInSquareRange(Location location, int rangeChunks){
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

    void processQueues(){
        if(controller.isDisabled()){ return; }

        List<Entity> queue;
        int mobcap;
        Map<Player, Integer> proximityMap;
        for(World world : controller.getServer().getWorlds()){
            queue = spawnQueueAnimals.get(world);
            mobcap = controller.getMobCapAnimals(world);
            proximityMap = proximityAnimals;
            for(Entity entity : queue){
                processEntity(entity, mobcap, proximityMap);
            }

            queue = spawnQueueMonsters.get(world);
            mobcap = controller.getMobCapMonsters(world);
            proximityMap = proximityMonsters;
            for(Entity entity : queue){
                processEntity(entity, mobcap, proximityMap);
            }

            queue = spawnQueueAmbients.get(world);
            mobcap = controller.getMobCapAmbient(world);
            proximityMap = proximityAmbients;
            for(Entity entity : queue){
                processEntity(entity, mobcap, proximityMap);
            }

            queue = spawnQueueWatermobs.get(world);
            mobcap = controller.getMobCapWatermobs(world);
            proximityMap = proximityWatermobs;
            for(Entity entity : queue){
                processEntity(entity, mobcap, proximityMap);
            }
        }
    }

    private void processEntity(Entity entity, int mobcap, Map<Player, Integer>proximityMap){
        boolean anyFull = false;
        Collection<Player> players = getPlayersInSquareRange(entity.getLocation(), controller.getSpawnRange());
        for(Player player: players){
            if(proximityMap.get(player) >= mobcap){
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
