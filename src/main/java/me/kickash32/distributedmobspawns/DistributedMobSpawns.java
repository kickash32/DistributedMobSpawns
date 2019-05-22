package me.kickash32.distributedmobspawns;

import io.papermc.lib.PaperLib;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class DistributedMobSpawns extends JavaPlugin {
    private static DistributedMobSpawns controller;

    private boolean disabled;
    private Map<World, Integer> mobCapsAnimals;
    private Map<World, Integer> mobCapsMonsters;
    private Map<World, Integer> mobCapsAmbient;
    private Map<World, Integer> mobCapsWatermobs;

    private EntityEventListener listener;
    private CmdExecutor cmdEx;
    private EntityProcessor processor;

    private boolean isPaperServer;
    public boolean experimental;
    private Map<World, Integer> spawnRange;
    private Map<World, Boolean> countOnlyNaturalSpawned;
    private int spawnRangeDefault;
    private boolean countOnlyNaturalSpawnedDefault;
    private int countUpdatePeriod;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.disabled = true;

        this.mobCapsAnimals = new HashMap<>();
        this.mobCapsMonsters = new HashMap<>();
        this.mobCapsAmbient = new HashMap<>();
        this.mobCapsWatermobs = new HashMap<>();

        this.spawnRange = new HashMap<>();
        this.countOnlyNaturalSpawned = new HashMap<>();
        for(World world : this.getServer().getWorlds()){
            this.mobCapsAnimals.put(world, world.getAnimalSpawnLimit());
            this.mobCapsMonsters.put(world, world.getMonsterSpawnLimit());
            this.mobCapsAmbient.put(world, world.getAmbientSpawnLimit());
            this.mobCapsWatermobs.put(world, world.getWaterAnimalSpawnLimit());
        }
        this.loadConfig();

        this.processor = new EntityProcessor(this);
        this.listener = new EntityEventListener(this, processor);
        this.cmdEx = new CmdExecutor(this);

        this.disabled = false;

        this.getCommand("dms").setExecutor(cmdEx);

        this.getServer().getScheduler().runTaskTimer(
                this, new UpdateMobCountsTask(this, processor), 0,countUpdatePeriod);
//        this.getServer().getScheduler().runTaskTimer(
//                this,
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        if(isDisabled()){ return; }
//                        processor.processQueues();
//                    }
//                },
//                1, queueProcessPeriod);

        DistributedMobSpawns.controller = this;
    }

    void loadConfig(){
        FileConfiguration config = this.getConfig();
        this.saveDefaultConfig();

        this.isPaperServer = PaperLib.isPaper();

        if(PaperLib.isSpigot()) {
            try {
                YamlConfiguration spigotConfig = new YamlConfiguration();
                spigotConfig.load("spigot.yml");

                ConfigurationSection spigotSection = spigotConfig.getConfigurationSection("world-settings");
                this.spawnRangeDefault = spigotSection.getConfigurationSection("default")
                        .getInt("mob-spawn-range");
                for (String worldName : spigotSection.getKeys(false)) {
                    if (worldName.equals("default")) {
                        continue;
                    }

                    int tmpInt = spigotSection.getConfigurationSection(worldName)
                            .getInt("mob-spawn-range");
                    this.spawnRange.put(getServer().getWorld(worldName), tmpInt);
                }
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }else{
            this.spawnRangeDefault = 8;

        }

        if(PaperLib.isPaper()) {
            try {
                YamlConfiguration paperConfig = new YamlConfiguration();
                paperConfig.load("paper.yml");

                ConfigurationSection paperSection = paperConfig.getConfigurationSection("world-settings");
                this.countOnlyNaturalSpawnedDefault = paperSection.getConfigurationSection("default")
                        .getBoolean("count-all-mobs-for-spawning");
                for (String worldName : paperSection.getKeys(false)) {
                    if (worldName.equals("default")) {
                        continue;
                    }
                    boolean tmpBool = !paperSection.getConfigurationSection(worldName)
                            .getBoolean("count-all-mobs-for-spawning");
                    this.countOnlyNaturalSpawned.put(getServer().getWorld(worldName), tmpBool);
                }
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        else{
            this.countOnlyNaturalSpawnedDefault = false;
        }

        this.countUpdatePeriod = config.getInt("countUpdatePeriod", 20);
        this.experimental = config.getBoolean("experimentalFeatures", false);

        if(config.getBoolean("adjust-spawn-limits-for-range", false))
            { this.adjustLimits(); }
        else
            { this.adjustCaps(); }
    }

    private void adjustLimits(){
        int tmp;
        for(World world : this.getServer().getWorlds()){
            int range = getSpawnRange(world);
            int chunks = Util.chunksInRadius(range);
            tmp = (int)(0.0+this.getMobCapAnimals(world) * 289 / chunks);
            System.out.println("[DMS] Set Animal limit to: "+tmp+" was: "+getMobCapAnimals(world)+" with radius: "+range + " in "+world.getName());
            world.setAnimalSpawnLimit(tmp);

            tmp = (int)(0.0+this.getMobCapMonsters(world) * 289 / chunks);
            System.out.println("[DMS] Set Monster limit to: "+tmp+" was: "+getMobCapMonsters(world)+" with radius: "+range + " in "+world.getName());
            world.setMonsterSpawnLimit(tmp);

            tmp = (int)(0.0+this.getMobCapAmbient(world) * 289 / chunks);
            System.out.println("[DMS] Set Ambient limit to: "+tmp+" was: "+getMobCapAmbient(world)+" with radius: "+range + " in "+world.getName());
            world.setAmbientSpawnLimit(tmp);


            tmp = (int)(0.0+this.getMobCapWatermobs(world) * 289 / chunks);
            System.out.println("[DMS] Set Watermobs limit to: "+tmp+" was: "+getMobCapWatermobs(world)+" with radius: "+range + " in "+world.getName());
            world.setWaterAnimalSpawnLimit(tmp);

        }
    }

    private void adjustCaps(){
        int tmp;
        for(World world : this.getServer().getWorlds()){
            int range = getSpawnRange(world);
            int chunks = Util.chunksInRadius(range);
            tmp = (int)(0.0+chunks * this.getMobCapAnimals(world)/289);
            this.mobCapsAnimals.put(world, tmp);
            System.out.println("[DMS] Set Animals mobcap to: "+tmp+" with radius: "+range + " in "+world.getName());

            tmp = (int)(0.0+chunks * this.getMobCapMonsters(world)/289);
            this.mobCapsMonsters.put(world, tmp);
            System.out.println("[DMS] Set Monsters mobcap to: "+tmp+" with radius: "+range + " in "+world.getName());

            tmp = (int)(0.0+chunks * this.getMobCapAmbient(world)/289);
            this.mobCapsAmbient.put(world, tmp);
            System.out.println("[DMS] Set Ambient mobcap to: "+tmp+" with radius: "+range + " in "+world.getName());

            tmp = (int)(0.0+chunks * this.getMobCapWatermobs(world)/289);
            this.mobCapsWatermobs.put(world, tmp);
            System.out.println("[DMS] Set Watermobs mobcap to: "+tmp+" with radius: "+range + " in "+world.getName());
        }
    }

    @Override
    public void onDisable() {
        for(World world : this.getServer().getWorlds()) {
            world.setAnimalSpawnLimit(this.getMobCapAnimals(world));
            world.setMonsterSpawnLimit(this.getMobCapMonsters(world));
            world.setAmbientSpawnLimit(this.getMobCapAmbient(world));
            world.setWaterAnimalSpawnLimit(this.getMobCapWatermobs(world));
        }
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

    int getSpawnRange(World world){
        return spawnRange.getOrDefault(world, spawnRangeDefault);
    }

    boolean countOnlyNaturalSpawned(World world){
        return this.countOnlyNaturalSpawned.getOrDefault(world, countOnlyNaturalSpawnedDefault);
    }

    boolean isDisabled(){
        return disabled;
    }
    boolean toggleDisabled(){
        disabled = !disabled;
        return disabled;
    }

    public static DistributedMobSpawns getController(){
        return DistributedMobSpawns.controller;
    }
}
