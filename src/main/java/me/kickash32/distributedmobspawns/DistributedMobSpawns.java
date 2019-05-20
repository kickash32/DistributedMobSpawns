package me.kickash32.distributedmobspawns;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Timer;

public final class DistributedMobSpawns extends JavaPlugin {
    private boolean disabled;
    private HashMap<World, Integer> mobCapsAnimals;
    private HashMap<World, Integer> mobCapsMonsters;
    private HashMap<World, Integer> mobCapsAmbient;
    private HashMap<World, Integer> mobCapsWatermobs;

    private MobListener msl;
    private CmdExecutor cmdEx;
    private Timer fakeEventGen;

    private int spawnRange;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.msl = new MobListener(this);
        this.cmdEx = new CmdExecutor(this);
        this.disabled = false;

        mobCapsAnimals = new HashMap<>();
        mobCapsMonsters = new HashMap<>();
        mobCapsAmbient = new HashMap<>();
        mobCapsWatermobs = new HashMap<>();
        for(World world : this.getServer().getWorlds()){
            mobCapsAnimals.put(world, world.getAnimalSpawnLimit());
            mobCapsMonsters.put(world, world.getMonsterSpawnLimit());
            mobCapsAmbient.put(world, world.getAmbientSpawnLimit());
            mobCapsWatermobs.put(world, world.getWaterAnimalSpawnLimit());
        }
        loadConfig();

        this.getCommand("dms").setExecutor(cmdEx);
        this.fakeEventGen = new Timer();
        fakeEventGen.schedule(new MyScheduler(this),0, 50);
    }

    void loadConfig(){
        FileConfiguration config = this.getConfig();
        this.saveDefaultConfig();
        this.spawnRange = config.getInt("mob-spawn-range", 6);
        if(config.getBoolean("adjust-spawn-limits-for-range", false))
            { adjustLimits(); }
        else
            { adjustCaps(); }
    }

    private void adjustLimits(){
        int tmp;
        for(World world : this.getServer().getWorlds()){
            tmp = (int)(0.0+getMobCapAnimals(world) * 289 / Util.chunksInRadius(spawnRange));
            System.out.println("[DMS] Set Animal limit to: "+tmp+" was: "+getMobCapAnimals(world)+" with radius: "+spawnRange + " in "+world.getName());
            world.setAnimalSpawnLimit(tmp);

            tmp = (int)(0.0+getMobCapMonsters(world) * 289 / Util.chunksInRadius(spawnRange));
            System.out.println("[DMS] Set Monster limit to: "+tmp+" was: "+getMobCapMonsters(world)+" with radius: "+spawnRange + " in "+world.getName());
            world.setMonsterSpawnLimit(tmp);

            tmp = (int)(0.0+getMobCapAmbient(world) * 289 / Util.chunksInRadius(spawnRange));
            System.out.println("[DMS] Set Ambient limit to: "+tmp+" was: "+getMobCapAmbient(world)+" with radius: "+spawnRange + " in "+world.getName());
            world.setAmbientSpawnLimit(tmp);


            tmp = (int)(0.0+getMobCapWatermobs(world) * 289 / Util.chunksInRadius(spawnRange));
            System.out.println("[DMS] Set Watermobs limit to: "+tmp+" was: "+getMobCapWatermobs(world)+" with radius: "+spawnRange + " in "+world.getName());
            world.setWaterAnimalSpawnLimit(tmp);

        }
    }

    private void adjustCaps(){
        int tmp;
        for(World world : this.getServer().getWorlds()){
            tmp = (int)(0.0+Util.chunksInRadius(spawnRange) * getMobCapAnimals(world)/289);
            mobCapsAnimals.put(world, tmp);
            System.out.println("[DMS] Set Animals mobcap to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());

            tmp = (int)(0.0+Util.chunksInRadius(spawnRange) * getMobCapMonsters(world)/289);
            mobCapsMonsters.put(world, tmp);
            System.out.println("[DMS] Set Monsters mobcap to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());

            tmp = (int)(0.0+Util.chunksInRadius(spawnRange) * getMobCapAmbient(world)/289);
            mobCapsAmbient.put(world, tmp);
            System.out.println("[DMS] Set Ambient mobcap to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());

            tmp = (int)(0.0+Util.chunksInRadius(spawnRange) * getMobCapWatermobs(world)/289);
            mobCapsWatermobs.put(world, tmp);
            System.out.println("[DMS] Set Watermobs mobcap to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());
        }
    }

    @Override
    public void onDisable() {
        for(World world : this.getServer().getWorlds()) {
            world.setAnimalSpawnLimit(getMobCapAnimals(world));
            world.setMonsterSpawnLimit(getMobCapMonsters(world));
            world.setAmbientSpawnLimit(getMobCapAmbient(world));
            world.setWaterAnimalSpawnLimit(getMobCapWatermobs(world));
        }

        fakeEventGen.cancel();
    }

    int getMobCapAnimals(World world){
        return mobCapsAnimals.get(world);
    }
    int getMobCapMonsters(World world){
        return mobCapsMonsters.get(world);
    }
    int getMobCapAmbient(World world){
        return mobCapsAmbient.get(world);
    }
    int getMobCapWatermobs(World world){
        return mobCapsWatermobs.get(world);
    }

    int getSpawnRange(){
        return spawnRange;
    }

    boolean isDisabled(){
        return disabled;
    }

    boolean toggleDisabled(){
        disabled = !disabled;
        return disabled;
    }

    MobListener getListener(){
        return msl;
    }
}
