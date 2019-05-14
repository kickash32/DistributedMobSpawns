package me.kickash32.distributedmobspawns;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;

import java.util.*;


public class MobSpawnListener implements Listener {
    private DistributedMobSpawns controller;
    private Map<World, LongHashSet> whiteListsAnimals;//Each world has its own whitelist of chunks(stored as a set of location hashes) where monsters can spawn
    private Map<World, LongHashSet> whiteListsMonsters;
    private Map<World, LongHashSet> whiteListsAmbient;
    private Map<World, LongHashSet> whiteListsWatermobs;

    private HashMap<World, List<Entity>> newAnimals;
    private HashMap<World, List<Entity>> newMonsters;
    private HashMap<World, List<Entity>> newAmbient;
    private HashMap<World, List<Entity>> newWatermobs;

    MobSpawnListener(DistributedMobSpawns controller){
        this.controller = controller;
        controller.getServer().getPluginManager().registerEvents(this, controller);

        whiteListsAnimals = new HashMap<>();
        whiteListsMonsters = new HashMap<>();
        whiteListsAmbient = new HashMap<>();
        whiteListsWatermobs = new HashMap<>();

        newAnimals = new HashMap<>();
        newMonsters = new HashMap<>();
        newAmbient = new HashMap<>();
        newWatermobs = new HashMap<>();
        reset();
    }

    void reset(){
        for(World world : controller.getServer().getWorlds()){
            whiteListsAnimals.put(world, new LongHashSet());
            whiteListsMonsters.put(world, new LongHashSet());
            whiteListsAmbient.put(world, new LongHashSet());
            whiteListsWatermobs.put(world, new LongHashSet());

            newAnimals.put(world, new ArrayList<>());
            newMonsters.put(world, new ArrayList<>());
            newAmbient.put(world, new ArrayList<>());
            newWatermobs.put(world, new ArrayList<>());
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

    LongHashSet getWhitelistAnimalsImmutable(World world){
        return new LongHashSet(getWhiteListAnimals(world));
    }
    LongHashSet getWhitelistMonstersImmutable(World world){
        return new LongHashSet(getWhiteListMonsters(world));
    }
    LongHashSet getWhitelistAmbientImmutable(World world){
        return new LongHashSet(getWhiteListAmbient(world));
    }
    LongHashSet getWhitelistWatermobsImmutable(World world){
        return new LongHashSet(getWhiteListWatermobs(world));
    }
//    @EventHandler //removed until a better way is found for supporting spigot and paper optimizations at the same time
//    public void onPlayerNaturallySpawnCreaturesEvent(PlayerNaturallySpawnCreaturesEvent event){
//        controller.serverPaperDetected();
//        update(event.getPlayer(), event.getSpawnRadius());
//    }

    void update(Player player, int radius){
        radius = 8;
        World world = player.getWorld();

        LongHashSet playerChunks = new LongHashSet();

        Location location = player.getLocation();
        //get player's chunk co-ordinates
        int ii = (int)Math.floor(0.0+location.getBlockX() / 16.0D);
        int kk = (int)Math.floor(0.0+location.getBlockZ() / 16.0D);
        int animalCount = 0;
        int monsterCount = 0;
        int ambientCount = 0;
        int watermobCount = 0;
        int spawnChunksCount = controller.chunksInRadius(radius);

        //the maximum density for each player is defined as the mobcap distributed over 17x17 chunks (refer to mojang's code)
        double densityLimitAnimals = (double) (controller.getMobCapAnimals(world) + controller.getBuffer()) / spawnChunksCount;
        double densityLimitMonsters = (double) (controller.getMobCapMonsters(world) + controller.getBuffer()) / spawnChunksCount;
        double densityLimitAmbient = (double) (controller.getMobCapAmbient(world) + controller.getBuffer()) / spawnChunksCount;
        double densityLimitWatermobs = (double) (controller.getMobCapWatermobs(world) + controller.getBuffer()) / spawnChunksCount;

        //get Chunk info
        Chunk chunk;
        for(int i = -radius; i <= radius; i++){
            for(int k = -radius; k <= radius; k++) {
                int chunkX = i + ii;
                int chunkZ = k + kk;
                if (!world.isChunkLoaded(chunkX, chunkZ)) { continue; }

                chunk = world.getChunkAt(chunkX, chunkZ);
                playerChunks.add(util.LongHash.toLong(chunkX,chunkZ));

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

        //add or remove chunks from whitelists accordingly
        if ((double)(animalCount)/spawnChunksCount < densityLimitAnimals){
            getWhiteListAnimals(world).addAll(playerChunks);
        }
        if ((double)(monsterCount)/spawnChunksCount < densityLimitMonsters){
            getWhiteListMonsters(world).addAll(playerChunks);
        }
        if ((double)(ambientCount)/spawnChunksCount < densityLimitAmbient){
            getWhiteListAmbient(world).addAll(playerChunks);
        }
        if ((double)(watermobCount)/spawnChunksCount < densityLimitWatermobs){
            getWhiteListWatermobs(world).addAll(playerChunks);
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

        Location location = event.getLocation();
        World world = location.getWorld();
        LongHashSet whitelist;
        List<Entity> queue;

        if(isNaturallySpawningAnimal(event.getEntity())) {
             whitelist = getWhiteListAnimals(world);
             queue = newAnimals.get(world);
        }
        else if(isNaturallySpawningMonster(event.getEntity())) {
            whitelist = getWhiteListMonsters(world);
            queue = newMonsters.get(world);
        }
        else if(isNaturallySpawningAmbient(event.getEntity())) {
            whitelist = getWhiteListAmbient(world);
            queue = newAmbient.get(world);
        }
        else if(isNaturallySpawningWatermob(event.getEntity())) {
            whitelist = getWhiteListWatermobs(world);
            queue = newWatermobs.get(world);
        }
        else{
            return;
        }

        int chunkX = (int) Math.floor(0.0 + location.getBlockX() / 16.0D);
        int chunkZ = (int) Math.floor(0.0 + location.getBlockZ() / 16.0D);

        if (!whitelist.contains(util.LongHash.toLong(chunkX, chunkZ))) {
            event.setCancelled(true);
        }
        else{
            queue.add(event.getEntity());
        }
    }

    private Player getNearestPlayer(Location location){
        Player nearestPlayer = null;
        double minDist = controller.getSpawnRange()*16;
        for(Player player : location.getNearbyPlayers(controller.getSpawnRange()*16*2)){
            if(location.distance(player.getLocation()) < minDist){
                minDist = location.distance(player.getLocation());
                nearestPlayer = player;
            }
        }
        return nearestPlayer;
    }

    void cull(){
        if(controller.isDisabled()){ return; }

        List<Entity> queue;
        for(World world : controller.getServer().getWorlds()){
            queue = newAnimals.get(world);
            Collections.shuffle(queue);
            for(int i = controller.getMobCapAnimals(world)/2; i < queue.size(); i++){
                queue.get(i).remove();
            }

            queue = newMonsters.get(world);
            Collections.shuffle(queue);
            for(int i = controller.getMobCapMonsters(world)/2; i < queue.size(); i++){
                queue.get(i).remove();
            }

            queue = newAmbient.get(world);
            Collections.shuffle(queue);
            for(int i = controller.getMobCapAmbient(world)/2; i < queue.size(); i++){
                queue.get(i).remove();
            }

            queue = newWatermobs.get(world);
            Collections.shuffle(queue);
            for(int i = controller.getMobCapWatermobs(world)/2; i < queue.size(); i++){
                queue.get(i).remove();
            }
        }
    }

    static boolean isNaturallySpawningAnimal(Entity entity){
        if (entity == null) { return false; }
        return isAnimal(entity.getType()) &&
                (entity.getEntitySpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)||
                 entity.getEntitySpawnReason().equals(CreatureSpawnEvent.SpawnReason.CHUNK_GEN)
                );
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
