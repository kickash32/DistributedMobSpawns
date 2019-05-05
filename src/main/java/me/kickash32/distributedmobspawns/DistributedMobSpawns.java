package me.kickash32.distributedmobspawns;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Timer;

public final class DistributedMobSpawns extends JavaPlugin {
    private boolean disabled;
    private boolean runningPaper = false;
    private HashMap<World, Integer> mobCaps;

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

        mobCaps = new HashMap<>();
        for(World world : this.getServer().getWorlds()){
            mobCaps.put(world, world.getMonsterSpawnLimit());
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
        this.spawnRange = config.getInt("mob-spawn-range", 8);
        if(config.getBoolean("adjust-spawn-limits-for-range", true)){ adjustLimits(); }
        else { adjustCaps(); }
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
            System.out.println("Set Mobcap to: "+tmp+" with radius: "+spawnRange);
        }
    }

    private void adjustCaps(){
        for(World world : this.getServer().getWorlds()){
            int tmp = (int)(0.0+chunksInRadius(spawnRange) * getMobCapMonsters(world)/289);
            mobCaps.put(world, tmp);
            System.out.println("Set Mobcap to: "+tmp+" with radius: "+spawnRange);
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

    int getMobCapMonsters(World world){
        return mobCaps.get(world);
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
        fakeEventGen.cancel();
    }
}
