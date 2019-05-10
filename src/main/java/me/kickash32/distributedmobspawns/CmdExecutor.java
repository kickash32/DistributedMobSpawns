package me.kickash32.distributedmobspawns;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import util.LongHash;

import java.util.*;

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
                onButcherCommand(sender);
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

    private void onButcherCommand(CommandSender sender) {
        if(sender instanceof Player){
            ((Player) sender).getWorld().getEntities().stream()
                    .filter(entity -> MobSpawnListener.isNaturallySpawningMonster(entity))
                    .forEach(entity -> entity.remove());
            sender.sendMessage("Killed all naturally spawning monsters");
        }
        else { sender.sendMessage("Not currently possible from console"); }
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
        sender.sendMessage("butcher: kill all monsters");
        sender.sendMessage("setbuffer: change the variance of the mobcap");
        sender.sendMessage("stats: view distribution of monsters");
        sender.sendMessage("help: view command info");
        sender.sendMessage("reload: reload configuration from file");
        sender.sendMessage("toggle: toggle distribution enforcement");
    }
    private void onDebugCommand(CommandSender sender){
        ArrayList<String> msgs = new ArrayList<>();
        Server server = controller.getServer();
        long worldMonsters;
        long playerMonsters;
        long worldLimit;
        long playerLimit;
        int radius;
        Chunk[] worldChunks;
        HashSet<Long> worldBlackList;
        long worldBlackListSize;

        msgs.add("[Distributed Mob Spawns]");
        msgs.add("Format: [world] player: #monsters/Limit*");
        msgs.add("");

        String prefix;
        String separator = ": ";
        String prefixColor = "";

        for(World world : server.getWorlds()){
            prefix = String.format("%s[%s]", prefixColor, world.getName());

            worldLimit = 0;
            worldChunks = world.getLoadedChunks();
            worldBlackList = controller.getListener().getWhitelistMonstersImmutable(world);

            HashSet<Long> finalWorldBlackList = worldBlackList;
            worldBlackListSize = Arrays.stream(worldChunks)
                    .filter(chunk -> finalWorldBlackList.contains(
                            LongHash.toLong(chunk.getX(), chunk.getZ())))
                    .count();

            for(Player player : world.getPlayers()){
                playerMonsters = player.getNearbyEntities(128, 128, 128).stream()
                        .filter(entity -> MobSpawnListener.isNaturallySpawningMonster(entity))
                        .count();
                playerLimit = controller.getMobCapMonsters(world);
                worldLimit += playerLimit;//overestimation

                msgs.add(String.format("%s %s%s%d/%d", prefix, player.getDisplayName(), separator, playerMonsters, playerLimit));
            }
            worldLimit = Math.min(worldLimit, world.getMonsterSpawnLimit()*worldChunks.length/(17*17));
            worldMonsters = world.getEntities().stream()
                    .filter(entity -> MobSpawnListener.isNaturallySpawningMonster(entity))
                    .count();

            msgs.add(String.format("%s %s%s%d/%d", prefix, "Total", separator, worldMonsters, worldLimit));
            msgs.add(String.format("%s %s%s%d/%d", prefix, "Whitelist size", separator, worldBlackListSize, worldChunks.length));
        }

        sender.sendMessage(msgs.toArray(new String[0]));
    }
}
