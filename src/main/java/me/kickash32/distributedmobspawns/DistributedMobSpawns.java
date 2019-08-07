package me.kickash32.distributedmobspawns;

import io.papermc.lib.PaperLib;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DistributedMobSpawns extends JavaPlugin {
    private static DistributedMobSpawns controller;
    private boolean disabled;
    private Map<UUID, Integer> mobCapsAnimals;
    private Map<UUID, Integer> mobCapsMonsters;
    private Map<UUID, Integer> mobCapsAmbient;
    private Map<UUID, Integer> mobCapsWatermobs;
    private Listener listener;
    private CommandExecutor cmdEx;
    private EntityProcessor processor;
    private Map<UUID, Integer> spawnRange;
    private Map<UUID, Boolean> countOnlyNaturalSpawned;
    private int spawnRangeDefault;
    private boolean countOnlyNaturalSpawnedDefault;
    private int countUpdatePeriod;
    private boolean verbose;

    public static DistributedMobSpawns getController() {
        return DistributedMobSpawns.controller;
    }

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
        for (World world : this.getServer().getWorlds()) {
            this.mobCapsAnimals.put(world.getUID(), world.getAnimalSpawnLimit());
            this.mobCapsMonsters.put(world.getUID(), world.getMonsterSpawnLimit());
            this.mobCapsAmbient.put(world.getUID(), world.getAmbientSpawnLimit());
            this.mobCapsWatermobs.put(world.getUID(), world.getWaterAnimalSpawnLimit());
        }
        this.loadConfig();

        this.processor = new EntityProcessor(this);
        this.listener = new EventListener(this, processor);

        this.getServer().getPluginManager().registerEvents(this.listener, this);
        this.cmdEx = new CmdExecutor(this);
        this.getCommand("dms").setExecutor(cmdEx);

        this.disabled = false;
        this.getServer().getScheduler().runTaskTimer(
                this, new UpdateMobCountsTask(this, this.processor), 0, countUpdatePeriod);

        if (DistributedMobSpawns.controller == null) { DistributedMobSpawns.controller = this; }
    }

    private void loadConfig() {
        FileConfiguration config = this.getConfig();
        verbose = config.getBoolean("verbose", false);

        if (PaperLib.isSpigot()) {
            try {
                YamlConfiguration spigotConfig = new YamlConfiguration();
                spigotConfig.load("spigot.yml");

                ConfigurationSection spigotSection = spigotConfig.getConfigurationSection("world-settings");
                this.spawnRangeDefault = spigotSection.getConfigurationSection("default")
                        .getInt("mob-spawn-range", 8);
                this.spawnRangeDefault = Util.limit(0, 8, spawnRangeDefault);
                for (String worldName : spigotSection.getKeys(false)) {
                    if (worldName.equals("default")) { continue; }

                    int tmpInt = spigotSection.getConfigurationSection(worldName)
                            .getInt("mob-spawn-range", spawnRangeDefault);
                    this.spawnRange.put(getServer().getWorld(worldName).getUID(), Util.limit(0, 8, tmpInt));
                }
            }
            catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }
        }
        else { this.spawnRangeDefault = 8; }

        if (PaperLib.isPaper() && PaperLib.getMinecraftVersion() >= 13) {
            try {
                YamlConfiguration paperConfig = new YamlConfiguration();
                paperConfig.load("paper.yml");

                ConfigurationSection paperSection = paperConfig.getConfigurationSection("world-settings");
                this.countOnlyNaturalSpawnedDefault = !paperSection.getConfigurationSection("default")
                        .getBoolean("count-all-mobs-for-spawning", true);
                for (String worldName : paperSection.getKeys(false)) {
                    if (worldName.equals("default")) { continue; }

                    boolean tmpBool = !paperSection.getConfigurationSection(worldName)
                            .getBoolean("count-all-mobs-for-spawning", true);
                    this.countOnlyNaturalSpawned.put(getServer().getWorld(worldName).getUID(), tmpBool); //TODO test this actually works
                }
            }
            catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }
        }
        else { this.countOnlyNaturalSpawnedDefault = false; }
//        System.out.println("[DMS] Counting only naturally spawned mobs: " + countOnlyNaturalSpawnedDefault);

        this.countUpdatePeriod = config.getInt("countUpdatePeriod", 20);

        if (config.getBoolean("adjust-spawn-limits-for-range", false)) { this.adjustLimits(); }
        else { this.adjustCaps(); }

        if (verbose) {
            for(World world : this.getServer().getWorlds()){
                System.out.println("[DMS] ["+world.getName()+"] "+
                        "mob-spawn-range/count-natural-mobs-only: "+ getSpawnRange(world)+'/'+getCountOnlyNaturalSpawned(world) + " "+
                        "Animal limit/cap: "+ getMobCapAnimals(world)+'/'+world.getAnimalSpawnLimit() + " "+
                        "Monster limit/cap: "+ getMobCapMonsters(world)+'/'+world.getMonsterSpawnLimit() + " "+
                        "Ambient limit/cap: "+ getMobCapAmbient(world)+'/'+world.getAmbientSpawnLimit() + " "+
                        "Watermob limit/cap: "+ getMobCapWatermobs(world)+'/'+world.getWaterAnimalSpawnLimit() + " "
                );
            }
        }
        this.saveDefaultConfig();
    }

    private void adjustLimits() {
        int tmp;
        for (World world : this.getServer().getWorlds()) {
            int range = getSpawnRange(world);
            int chunks = Util.chunksInRadius(range);

            tmp = (int) (0.0 + this.getMobCapAnimals(world) * 289 / chunks);
//            System.out.println("[DMS] Set Animal limit to: " + tmp + " was: " + getMobCapAnimals(world) + " with radius: " + range + " in " + world.getName());
            world.setAnimalSpawnLimit(tmp);

            tmp = (int) (0.0 + this.getMobCapMonsters(world) * 289 / chunks);
//            System.out.println("[DMS] Set Monster limit to: " + tmp + " was: " + getMobCapMonsters(world) + " with radius: " + range + " in " + world.getName());
            world.setMonsterSpawnLimit(tmp);

            tmp = (int) (0.0 + this.getMobCapAmbient(world) * 289 / chunks);
//            System.out.println("[DMS] Set Ambient limit to: " + tmp + " was: " + getMobCapAmbient(world) + " with radius: " + range + " in " + world.getName());
            world.setAmbientSpawnLimit(tmp);

            tmp = (int) (0.0 + this.getMobCapWatermobs(world) * 289 / chunks);
//            System.out.println("[DMS] Set Watermobs limit to: " + tmp + " was: " + getMobCapWatermobs(world) + " with radius: " + range + " in " + world.getName());
            world.setWaterAnimalSpawnLimit(tmp);

        }
    }

    private void adjustCaps() {
        int tmp;
        for (World world : this.getServer().getWorlds()) {
            int range = getSpawnRange(world);
            int chunks = Util.chunksInRadius(range);

            tmp = (int) (0.0 + chunks * this.getMobCapAnimals(world) / 289);
            this.mobCapsAnimals.put(world.getUID(), tmp);
//            System.out.println("[DMS] Set Animals mobcap to: " + tmp + " with radius: " + range + " in " + world.getName());

            tmp = (int) (0.0 + chunks * this.getMobCapMonsters(world) / 289);
            this.mobCapsMonsters.put(world.getUID(), tmp);
//            System.out.println("[DMS] Set Monsters mobcap to: " + tmp + " with radius: " + range + " in " + world.getName());

            tmp = (int) (0.0 + chunks * this.getMobCapAmbient(world) / 289);
            this.mobCapsAmbient.put(world.getUID(), tmp);
//            System.out.println("[DMS] Set Ambient mobcap to: " + tmp + " with radius: " + range + " in " + world.getName());

            tmp = (int) (0.0 + chunks * this.getMobCapWatermobs(world) / 289);
            this.mobCapsWatermobs.put(world.getUID(), tmp);
//            System.out.println("[DMS] Set Watermobs mobcap to: " + tmp + " with radius: " + range + " in " + world.getName());
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this.listener);
        for (World world : this.getServer().getWorlds()) {
            world.setAnimalSpawnLimit(this.getMobCapAnimals(world));
            world.setMonsterSpawnLimit(this.getMobCapMonsters(world));
            world.setAmbientSpawnLimit(this.getMobCapAmbient(world));
            world.setWaterAnimalSpawnLimit(this.getMobCapWatermobs(world));
        }
    }

    int getMobCapAnimals(World world) {
        UUID id = world.getUID();
        return mobCapsAnimals.get(id);
    }

    int getMobCapMonsters(World world) {
        UUID id = world.getUID();
        return mobCapsMonsters.get(id);
    }

    int getMobCapAmbient(World world) {
        UUID id = world.getUID();
        return mobCapsAmbient.get(id);
    }

    int getMobCapWatermobs(World world) {
        UUID id = world.getUID();
        return mobCapsWatermobs.get(id);
    }

    int getSpawnRange(World world) {
        UUID id = world.getUID();
        return spawnRange.getOrDefault(id, spawnRangeDefault);
    }

    boolean getCountOnlyNaturalSpawned(World world) {
        UUID id = world.getUID();
        return this.countOnlyNaturalSpawned.getOrDefault(id, countOnlyNaturalSpawnedDefault);
    }

    boolean isDisabled() {
        return disabled;
    }

    boolean toggleDisabled() {
        disabled = !disabled;

        if (disabled) {
            HandlerList.unregisterAll(this.listener);
        } else {
            this.getServer().getPluginManager().registerEvents(this.listener, this);
        }

        return disabled;
    }
}
