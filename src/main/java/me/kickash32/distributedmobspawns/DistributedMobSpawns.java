package me.kickash32.distributedmobspawns;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Timer;

public final class DistributedMobSpawns extends JavaPlugin {
    private boolean disabled;
    private boolean runningPaper = false;
    private int buffer;//variance allowed for number of mobs around players
    private HashMap<World, Integer> mobCaps;

    private MobSpawnListener msl;
    private CmdExecutor cmdEx;
    private Timer fakeEventGen;

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

        this.getCommand("dms").setExecutor(cmdEx);
        this.fakeEventGen = new Timer();
        fakeEventGen.schedule(new FakePlayerNaturallySpawnCreaturesEventCreator(this),0, 50);

        loadConfig();
    }

    void loadConfig(){
        FileConfiguration config = this.getConfig();
        this.saveDefaultConfig();
        this.buffer = Math.max(config.getInt("buffer", 0), 0);
    }

    void serverPaperDetected(){
        if(!runningOnPaper()) {
            runningPaper = true;
            System.out.println("[DMS] Detected Paper");
            fakeEventGen.cancel();
        }
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
