package me.kickash32.distributedmobspawns;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import util.LongHash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdExecutor implements CommandExecutor {
    private DistributedMobSpawns controller;

    CmdExecutor(DistributedMobSpawns c){
        controller = c;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(args.length == 0){ return false; }
        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "setbuffer":
                onSetBufferCommand(sender, args);
                break;
            case "butcher":
                onButcherCommand(sender, args);
                break;
            case "stats":
                onDebugCommand(sender);
                break;
            case "help":
                onHelpCommand(sender);
                break;
            case "reload":
                onReloadCommand(sender);
                break;
            case "toggle":
                onToggleCommand(sender);
                break;
            default:
                sender.sendMessage("Unknown command");
                return false;
        }
        return true;
    }

    private void onSetBufferCommand(CommandSender sender, String[] args) {
        int size;
        try{
            size = Integer.parseInt(args[1]);
        }catch (NumberFormatException e){
            size = -1;
        }
        boolean tmp = controller.setBuffer(size);
        if (tmp){
            sender.sendMessage("Successfully updated buffer amount");
        }
        else{
            sender.sendMessage("Failed to change buffer. Enter a positive buffer size.");
        }
    }

    private void onButcherCommand(CommandSender sender, String[] args) {
        if (args == null || args[1] == null) {//CHECK ARRAY LENGTH
            sender.sendMessage("Unknown mobtype, see help");
            return;
        }

        List<Entity> entityList = new ArrayList<>();
        if(sender instanceof Player){
            entityList = ((Player) sender).getWorld().getEntities();
        }
        else{
            for(World world : sender.getServer().getWorlds()){
                entityList.addAll(world.getEntities());
            }
        }

        switch (args[1].toLowerCase()){
            case "animals":
                entityList.stream()
                        .filter(entity -> MobSpawnListener.isNaturallySpawningAnimal(entity))
                        .forEach(entity -> entity.remove());
                sender.sendMessage("Successfully killed all animals");
                break;
            case "monster":
                entityList.stream()
                        .filter(entity -> MobSpawnListener.isNaturallySpawningMonster(entity))
                        .forEach(entity -> entity.remove());
                sender.sendMessage("Successfully killed all monsters");
                break;
            case "ambient":
                entityList.stream()
                        .filter(entity -> MobSpawnListener.isNaturallySpawningAmbient(entity))
                        .forEach(entity -> entity.remove());
                sender.sendMessage("Successfully killed all ambient mobs");
                break;
            case "watermobs":
                entityList.stream()
                        .filter(entity -> MobSpawnListener.isNaturallySpawningWatermob(entity))
                        .forEach(entity -> entity.remove());
                sender.sendMessage("Successfully killed all watermobs");
                break;
            default:
                sender.sendMessage("Unknown entity type, try animals, monster, ambient, watermobs");
                break;
        }
    }

    private void onReloadCommand(CommandSender sender) {
        controller.reloadConfig();
        sender.sendMessage("[DMS] reload complete");
    }

    private void onToggleCommand(CommandSender sender) {
        sender.sendMessage("[DMS] Enforcement is now: " + !controller.toggleDisabled());
    }


    private void onHelpCommand(CommandSender sender){
        sender.sendMessage("[DMS] command list");
        sender.sendMessage("butcher {mobtype}: kill all mobs of {mobtype} from the current world");
        sender.sendMessage("setbuffer {pos integer}: change the variance of the mobcap");
        sender.sendMessage("stats: view distribution of monsters");
        sender.sendMessage("help: view command info");
        sender.sendMessage("reload: reload configuration from file");
        sender.sendMessage("toggle: toggle distribution enforcement");
        //TO DO MOBTYPES
    }
    private void onDebugCommand(CommandSender sender){// TO DO move to separate class
        ArrayList<String> msgs = new ArrayList<>();
        Server server = controller.getServer();
        int radius = controller.getSpawnRange()*16;
        Chunk[] worldChunks;

        long playerAnimalCount;
        long playerAnimalLimit;
        long worldAnimalCount;
        long worldAnimalLimit;
        long worldAnimalBlackListSize;

        long playerMonsterCount;
        long playerMonsterLimit;
        long worldMonsterLimit;
        long worldMonsterCount;
        long worldMonsterBlackListSize;

        long playerAmbientCount;
        long playerAmbientLimit;
        long worldAmbientLimit;
        long worldAmbientCount;
        long worldAmbientBlackListSize;

        long playerWatermobCount;
        long playerWatermobLimit;
        long worldWatermobLimit;
        long worldWatermobCount;
        long worldWatermobBlackListSize;


        msgs.add("[Distributed Mob Spawns]");
        msgs.add("Format: [world] player: #Animal/Animal_Limit* #Monster/Monster_Limit* #Ambient/Ambient_Limit* #Watermob/Watermob_Limit*");
        msgs.add("These should only be used as a reference for troubleshooting");

        String prefix;
        String separator = ":";
        String prefixColor = "";

        for(World world : server.getWorlds()){
            prefix = String.format("%s[%s]", prefixColor, world.getName());

            worldAnimalLimit = 0;
            worldMonsterLimit = 0;
            worldAmbientLimit = 0;
            worldWatermobLimit = 0;

            worldChunks = world.getLoadedChunks();

            LongHashSet worldAnimalBlackList = controller.getListener().getWhitelistAnimalsImmutable(world);
            worldAnimalBlackListSize = Arrays.stream(worldChunks)
                    .filter(chunk -> worldAnimalBlackList.contains(
                            LongHash.toLong(chunk.getX(), chunk.getZ())))
                    .count();
            LongHashSet worldMonsterBlackList = controller.getListener().getWhitelistMonstersImmutable(world);
            worldMonsterBlackListSize = Arrays.stream(worldChunks)
                    .filter(chunk -> worldMonsterBlackList.contains(
                            LongHash.toLong(chunk.getX(), chunk.getZ())))
                    .count();
            LongHashSet worldAmbientBlackList = controller.getListener().getWhitelistAmbientImmutable(world);
            worldAmbientBlackListSize = Arrays.stream(worldChunks)
                    .filter(chunk -> worldAmbientBlackList.contains(
                            LongHash.toLong(chunk.getX(), chunk.getZ())))
                    .count();
            LongHashSet worldWatermobsBlackList = controller.getListener().getWhitelistWatermobsImmutable(world);
            worldWatermobBlackListSize = Arrays.stream(worldChunks)
                    .filter(chunk -> worldWatermobsBlackList.contains(
                            LongHash.toLong(chunk.getX(), chunk.getZ())))
                    .count();

            for(Player player : world.getPlayers()){
                List<Entity> playerEntities = player.getNearbyEntities(radius, radius, radius);

                playerAnimalCount = playerEntities.stream()
                        .filter(entity -> MobSpawnListener.isNaturallySpawningAnimal(entity))
                        .count();
                playerMonsterCount = playerEntities.stream()
                        .filter(entity -> MobSpawnListener.isNaturallySpawningMonster(entity))
                        .count();
                playerAmbientCount = playerEntities.stream()
                        .filter(entity -> MobSpawnListener.isNaturallySpawningAmbient(entity))
                        .count();
                playerWatermobCount = playerEntities.stream()
                        .filter(entity -> MobSpawnListener.isNaturallySpawningWatermob(entity))
                        .count();

                playerAnimalLimit = controller.getMobCapAnimals(world);
                worldAnimalLimit += playerAnimalLimit;//overestimation
                playerMonsterLimit = controller.getMobCapMonsters(world);
                worldMonsterLimit += playerMonsterLimit;//overestimation
                playerAmbientLimit = controller.getMobCapAmbient(world);
                worldAmbientLimit += playerAmbientLimit;//overestimation
                playerWatermobLimit = controller.getMobCapWatermobs(world);
                worldWatermobLimit += playerWatermobLimit;//overestimation

                msgs.add(String.format("%s %s%s %d/%d %d/%d %d/%d %d/%d", prefix, player.getDisplayName(), separator,
                        playerAnimalCount, playerAnimalLimit,
                        playerMonsterCount, playerMonsterLimit,
                        playerAmbientCount, playerAmbientLimit,
                        playerWatermobCount, playerWatermobLimit));
            }
            List<Entity> worldEntities = world.getEntities();

            worldAnimalCount = worldEntities.stream()
                    .filter(entity -> MobSpawnListener.isNaturallySpawningAnimal(entity))
                    .count();
            worldMonsterCount = worldEntities.stream()
                    .filter(entity -> MobSpawnListener.isNaturallySpawningMonster(entity))
                    .count();
            worldAmbientCount = worldEntities.stream()
                    .filter(entity -> MobSpawnListener.isNaturallySpawningAmbient(entity))
                    .count();
            worldWatermobCount = worldEntities.stream()
                    .filter(entity -> MobSpawnListener.isNaturallySpawningWatermob(entity))
                    .count();

            msgs.add(String.format("%s %s%s %d/%d %d/%d %d/%d %d/%d", prefix, "Total", separator,
                    worldAnimalCount, worldAnimalLimit,
                    worldMonsterCount, worldMonsterLimit,
                    worldAmbientCount, worldAmbientLimit,
                    worldWatermobCount, worldWatermobLimit));


            msgs.add(String.format("%s %s%s %d/%d  %d/%d  %d/%d  %d/%d", prefix, "Whitelist size", separator,
                    worldAnimalBlackListSize, worldChunks.length,
                    worldMonsterBlackListSize, worldChunks.length,
                    worldAmbientBlackListSize, worldChunks.length,
                    worldWatermobBlackListSize, worldChunks.length));
        }

        sender.sendMessage(msgs.toArray(new String[0]));
    }
}
