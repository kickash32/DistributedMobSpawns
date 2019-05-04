package me.kickash32.distributedmobspawns;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Timer;

public final class DistributedMobSpawns extends JavaPlugin {
    private MobSpawnListener msl;
    private CmdExecutor cmdEx;
    private Timer fakeEventGen;
    private boolean disabled;
    private boolean runningPaper = false;

    //private int defaultRadius;
    private int buffer;
    private HashMap<World, Integer> mobCaps;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.msl = new MobSpawnListener(this);
        this.cmdEx = new CmdExecutor(this);
        this.getCommand("dms").setExecutor(cmdEx);
        this.fakeEventGen = new Timer();
        fakeEventGen.schedule(new FakePlayerNaturallySpawnCreaturesEventCreator(this),0, 50);
        this.disabled = false;
        //this.defaultRadius = 8;//Bukkit.spigot().getSpigotConfig().getInt("world-settings.default.mob-spawn-range", 8);
        this.buffer = 1;

        mobCaps = new HashMap<>();
        for(World world : this.getServer().getWorlds()){
            mobCaps.put(world, world.getMonsterSpawnLimit());
        }
    }

    void serverPaperDetected(){
        if(!runningOnPaper()) {
            runningPaper = true;
            System.out.println("Detected Paper");
            fakeEventGen.cancel();
        }
    }

//    int getDefaultRadius(){
//        return defaultRadius;
//    }

    int getBuffer(){
        return buffer;
    }

    HashMap<World, Integer> getMobCaps(){
        return new HashMap<>(mobCaps);
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
        // Plugin shutdown logic
        this.disabled = true;
        fakeEventGen.cancel();
    }
}
