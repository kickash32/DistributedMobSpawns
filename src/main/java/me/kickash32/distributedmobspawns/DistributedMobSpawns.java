package me.kickash32.distributedmobspawns;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Timer;

public final class DistributedMobSpawns extends JavaPlugin {
    private boolean disabled;
    private boolean runningPaper = false;
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
        adjustLimits();

        this.getCommand("dms").setExecutor(cmdEx);
        this.fakeEventGen = new Timer();
        fakeEventGen.schedule(new FakePlayerNaturallySpawnCreaturesEventCreator(this),0, 50);
    }

    void loadConfig(){
        FileConfiguration config = this.getConfig();
        this.saveDefaultConfig();
        this.buffer = Math.max(config.getInt("buffer", 0), 0);
        this.spawnRange = config.getInt("mob-spawn-range", 6);
        if(config.getBoolean("adjust-spawn-limits-for-range", true))
            { adjustLimits(); }
        else
            { adjustCaps(); }
    }

    void serverPaperDetected(){
        if(!runningOnPaper()) {
            runningPaper = true;
            System.out.println("[DMS] Detected Paper");
            fakeEventGen.cancel();
        }
    }

    private void adjustLimits(){
        for(World world : this.getServer().getWorlds()){
            int tmp = (int)(0.0+getMobCapMonsters(world) * 289 / chunksInRadius(spawnRange));
            world.setMonsterSpawnLimit(tmp);
            System.out.println("Set Monster limit to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());
        }
    }

    private void adjustCaps(){
        for(World world : this.getServer().getWorlds()){
            int tmp = (int)(0.0+chunksInRadius(spawnRange) * getMobCapMonsters(world)/289);
            mobCaps.put(world, tmp);
            System.out.println("Set Monsters mobcap to: "+tmp+" with radius: "+spawnRange + " in "+world.getName());
        }
    }

    public int chunksInRadius(int radius){
        return ((radius*2)+1)*((radius*2)+1);
    }

    int getSpawnRange(){
        return spawnRange;
    }

    boolean setBuffer(int x){
        if(x >= 0) {
            buffer = x;
            return true;
        }
        else { return false; }
    }

    int getBuffer(){
        return buffer;
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

    boolean runningOnPaper(){
        return runningPaper;
    }

    boolean toggleDisabled(){
        disabled = !disabled;
        return disabled;
    }

    boolean isDisabled(){
        return disabled;
    }

    MobSpawnListener getListener(){
        return msl;
    }

    @Override
    public void onDisable() {
        for(World world : this.getServer().getWorlds()) {
            world.setMonsterSpawnLimit(getMobCapMonsters(world));
        }

        fakeEventGen.cancel();
    }
}
