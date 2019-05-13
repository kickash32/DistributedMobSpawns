package me.kickash32.distributedmobspawns;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Timer;

public final class DistributedMobSpawns extends JavaPlugin {
    private boolean disabled;
    private boolean runningPaper;
    private HashMap<World, Integer> mobCapsAnimals;
    private HashMap<World, Integer> mobCapsMonsters;
    private HashMap<World, Integer> mobCapsAmbient;
    private HashMap<World, Integer> mobCapsWatermobs;

    private MobSpawnListener msl;
    private CmdExecutor cmdEx;
    private Timer fakeEventGen;

    private int buffer;//variance allowed for number of mobs around players
    private int spawnRange;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.msl = new MobSpawnListener(this);
        this.cmdEx = new CmdExecutor(this);
        this.disabled = false;
        this.runningPaper = false;

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
        fakeEventGen.schedule(new FakePlayerNaturallySpawnCreaturesEventCreator(this),0, 50);
    }

    void loadConfig(){
        FileConfiguration config = this.getConfig();
        this.saveDefaultConfig();
        this.buffer = Math.max(config.getInt("buffer", 0), 0);
        this.spawnRange = config.getInt("mob-spawn-range", 6);
        if(config.getBoolean("adjust-spawn-limits-for-range", false))
            { adjustLimits(); }
        else
            { adjustCaps(); }
    }

    private void adjustLimits(){
        int tmp;
        for(World world : this.getServer().getWorlds()){
            tmp = (int)(0.0+getMobCapAnimals(world) * 289 / chunksInRadius(spawnRange));
            System.out.println("Set Animal limit to: "+tmp+" was: "+getMobCapAnimals(world)+" with radius: "+spawnRange + " in "+world.getName());
            world.setAnimalSpawnLimit(tmp);

            tmp = (int)(0.0+getMobCapMonsters(world) * 289 / chunksInRadius(spawnRange));
            System.out.println("Set Monster limit to: "+tmp+" was: "+getMobCapMonsters(world)+" with radius: "+spawnRange + " in "+world.getName());
            world.setMonsterSpawnLimit(tmp);

            tmp = (int)(0.0+getMobCapAmbient(world) * 289 / chunksInRadius(spawnRange));
            System.out.println("Set Ambient limit to: "+tmp+" was: "+getMobCapAmbient(world)+" with radius: "+spawnRange + " in "+world.getName());
            world.setAmbientSpawnLimit(tmp);


            tmp = (int)(0.0+getMobCapWatermobs(world) * 289 / chunksInRadius(spawnRange));
            System.out.println("Set Watermobs limit to: "+tmp+" was: "+getMobCapWatermobs(world)+" with radius: "+spawnRange + " in "+world.getName());
            world.setWaterAnimalSpawnLimit(tmp);

        }
    }

    private void adjustCaps(){
        int tmp;
        for(World world : this.getServer().getWorlds()){
            tmp = (int)(0.0+chunksInRadius(spawnRange) * getMobCapAnimals(world)/289);
            mobCapsAnimals.put(world, tmp);
            System.out.println("Set Animals mobcap to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());

            tmp = (int)(0.0+chunksInRadius(spawnRange) * getMobCapMonsters(world)/289);
            mobCapsMonsters.put(world, tmp);
            System.out.println("Set Monsters mobcap to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());

            tmp = (int)(0.0+chunksInRadius(spawnRange) * getMobCapAmbient(world)/289);
            mobCapsAmbient.put(world, tmp);
            System.out.println("Set Ambient mobcap to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());

            tmp = (int)(0.0+chunksInRadius(spawnRange) * getMobCapWatermobs(world)/289);
            mobCapsWatermobs.put(world, tmp);
            System.out.println("Set Watermobs mobcap to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());
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

    int getBuffer(){
        return buffer;
    }

    boolean setBuffer(int x){
        if(x >= 0) {
            buffer = x;
            return true;
        }
        else { return false; }
    }

    boolean runningOnPaper(){
        return runningPaper;
    }

    void serverPaperDetected(){
        if(!runningOnPaper()) {
            runningPaper = true;
            System.out.println("[DMS] Detected Paper");
            fakeEventGen.cancel();
        }
    }

    boolean isDisabled(){
        return disabled;
    }

    boolean toggleDisabled(){
        disabled = !disabled;
        return disabled;
    }

    MobSpawnListener getListener(){
        return msl;
    }

    public static int chunksInRadius(int radius){
        return ((radius*2)+1)*((radius*2)+1);
    }
}
