package me.kickash32.distributedmobspawns;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MobListener implements Listener {
    private DistributedMobSpawns controller;

    private Map<Player, Integer> proximityAnimals;
    private Map<Player, Integer> proximityMonsters;
    private Map<Player, Integer> proximityAmbients;
    private Map<Player, Integer> proximityWatermobs;

    MobListener(DistributedMobSpawns controller){
        this.controller = controller;
        controller.getServer().getPluginManager().registerEvents(this, controller);

        for(World world : controller.getServer().getWorlds()){
            proximityAnimals = new ConcurrentHashMap<>();
            proximityMonsters = new ConcurrentHashMap<>();
            proximityAmbients = new ConcurrentHashMap<>();
            proximityWatermobs = new ConcurrentHashMap<>();
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
             processEntity(event, controller.getMobCapAnimals(world), proximityAnimals);
        }
        else if(Util.isNaturallySpawningMonster(event.getEntity())) {
            processEntity(event, controller.getMobCapMonsters(world), proximityMonsters);
        }
        else if(Util.isNaturallySpawningAmbient(event.getEntity())) {
            processEntity(event, controller.getMobCapAmbient(world), proximityAmbients);
        }
        else if(Util.isNaturallySpawningWatermob(event.getEntity())) {
            processEntity(event, controller.getMobCapWatermobs(world), proximityWatermobs);
        }
    }

    private void processEntity(CreatureSpawnEvent event, int mobCap, Map<Player, Integer>proximityMap){
        boolean anyFull = false;
        Collection<Player> players = Util.getPlayersInSquareRange(event.getLocation(), controller.getSpawnRange());
        for(Player player: players){
            if(proximityMap.get(player) > mobCap){
                anyFull = true;
            }
        }

        if(anyFull){
            event.setCancelled(true);
        }else{
            for(Player player: players){
                proximityMap.put(player, proximityMap.get(player)+1);
            }
        }
    }
}
