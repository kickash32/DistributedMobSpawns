package me.kickash32.distributedmobspawns;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CmdExecutor implements CommandExecutor, TabCompleter, TabExecutor {
    private DistributedMobSpawns controller;

    CmdExecutor(DistributedMobSpawns controller){
        this.controller = controller;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String s, String[] args) {
        if(args.length == 0){ return false; }
        String subCmd = args[0].toLowerCase();

        switch (subCmd) {
            case "butcher":
                onButcherCommand(sender, args);
                break;
            case "stats":
                onDebugCommand(sender);
                break;
            case "help":
                onHelpCommand(sender);
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

    private void onButcherCommand(CommandSender sender, String[] args) {
        if (args == null || args.length < 2 ||args[1] == null) {
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
                        .filter(Util::isNaturallySpawningAnimal)
                        .forEach(Entity::remove);
                sender.sendMessage("Successfully killed all animals");
                break;
            case "monsters":
                entityList.stream()
                        .filter(Util::isNaturallySpawningMonster)
                        .forEach(Entity::remove);
                sender.sendMessage("Successfully killed all monsters");
                break;
            case "ambients":
                entityList.stream()
                        .filter(Util::isNaturallySpawningAmbient)
                        .forEach(Entity::remove);
                sender.sendMessage("Successfully killed all ambient mobs");
                break;
            case "watermobs":
                entityList.stream()
                        .filter(Util::isNaturallySpawningWatermob)
                        .forEach(Entity::remove);
                sender.sendMessage("Successfully killed all watermobs");
                break;
            case "all":
                entityList.stream()
                        .filter(entity -> Util.isNaturallySpawningAnimal(entity)||
                                Util.isNaturallySpawningMonster(entity)||
                                Util.isNaturallySpawningAmbient(entity)||
                                Util.isNaturallySpawningWatermob(entity))
                        .forEach(Entity::remove);
                sender.sendMessage("Successfully killed all mobs");
                break;
            default:
                sender.sendMessage("Unknown entity type, try animals, monster, ambient, watermobs");
                break;
        }
    }

    private void onToggleCommand(CommandSender sender) {
        sender.sendMessage("[DMS] Enforcement is now: " + !this.controller.toggleDisabled());
    }


    private void onHelpCommand(CommandSender sender){
        sender.sendMessage("[DMS] command list");
        sender.sendMessage("butcher {mobtype}: kill all mobs of {mobtype} from the current world");
        sender.sendMessage("stats: view distribution of monsters");
        sender.sendMessage("help: view command info");
        sender.sendMessage("toggle: toggle distribution enforcement");
        //sender.sendMessage("Supported [animals] [monsters] [ambients] [watermobs] [all]");
    }
    private void onDebugCommand(CommandSender sender){// TODO move to separate class
        ArrayList<String> msgs = new ArrayList<>();
        Server server = this.controller.getServer();

        long playerAnimalCount;
        long playerAnimalLimit;
        long worldAnimalCount;
        long worldAnimalLimit;

        long playerMonsterCount;
        long playerMonsterLimit;
        long worldMonsterLimit;
        long worldMonsterCount;

        long playerAmbientCount;
        long playerAmbientLimit;
        long worldAmbientLimit;
        long worldAmbientCount;

        long playerWatermobCount;
        long playerWatermobLimit;
        long worldWatermobLimit;
        long worldWatermobCount;

        int radius;

        msgs.add("[Distributed Mob Spawns]");
        msgs.add("Format: [world] player: #Animal/Animal_Limit* #Monster/Monster_Limit* #Ambient/Ambient_Limit* #Watermob/Watermob_Limit*");
        msgs.add("These should only be used as a reference for troubleshooting");

        String prefix;
        String separator = ":";
        String prefixColor = "";

        for(World world : server.getWorlds()){
            radius = this.controller.getSpawnRange(world)*16;
            prefix = String.format("%s[%s]", prefixColor, world.getName());

            worldAnimalLimit = 0;
            worldMonsterLimit = 0;
            worldAmbientLimit = 0;
            worldWatermobLimit = 0;

            for(Player player : world.getPlayers()){
                List<Entity> playerEntities = player.getNearbyEntities(radius, radius, radius);

                playerAnimalCount = playerEntities.stream()
                        .filter(Util::isNaturallySpawningAnimal)
                        .count();
                playerMonsterCount = playerEntities.stream()
                        .filter(Util::isNaturallySpawningMonster)
                        .count();
                playerAmbientCount = playerEntities.stream()
                        .filter(Util::isNaturallySpawningAmbient)
                        .count();
                playerWatermobCount = playerEntities.stream()
                        .filter(Util::isNaturallySpawningWatermob)
                        .count();

                playerAnimalLimit = this.controller.getMobCapAnimals(world);
                worldAnimalLimit += playerAnimalLimit;
                playerMonsterLimit = this.controller.getMobCapMonsters(world);
                worldMonsterLimit += playerMonsterLimit;
                playerAmbientLimit = this.controller.getMobCapAmbient(world);
                worldAmbientLimit += playerAmbientLimit;
                playerWatermobLimit = this.controller.getMobCapWatermobs(world);
                worldWatermobLimit += playerWatermobLimit;

                msgs.add(String.format("%s %s%s %d/%d %d/%d %d/%d %d/%d", prefix, player.getDisplayName(), separator,
                        playerAnimalCount, playerAnimalLimit,
                        playerMonsterCount, playerMonsterLimit,
                        playerAmbientCount, playerAmbientLimit,
                        playerWatermobCount, playerWatermobLimit));
            }
            List<Entity> worldEntities = world.getEntities();

            worldAnimalCount = worldEntities.stream()
                    .filter(Util::isNaturallySpawningAnimal)
                    .count();
            worldMonsterCount = worldEntities.stream()
                    .filter(Util::isNaturallySpawningMonster)
                    .count();
            worldAmbientCount = worldEntities.stream()
                    .filter(Util::isNaturallySpawningAmbient)
                    .count();
            worldWatermobCount = worldEntities.stream()
                    .filter(Util::isNaturallySpawningWatermob)
                    .count();

            msgs.add(String.format("%s %s%s %d/%d %d/%d %d/%d %d/%d", prefix, "Total", separator,
                    worldAnimalCount, worldAnimalLimit,
                    worldMonsterCount, worldMonsterLimit,
                    worldAmbientCount, worldAmbientLimit,
                    worldWatermobCount, worldWatermobLimit));
        }

        sender.sendMessage(msgs.toArray(new String[0]));
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, String[] args) {
        List<String> options = new ArrayList<>();
        //TODO rework to be modular
        switch (args.length) {
            case 0:
                break;
            case 1:
                options.add("butcher");
                options.add("stats");
                options.add("help");
                options.add("toggle");
                break;
            case 2:
                options.add("animals");
                options.add("monsters");
                options.add("ambient");
                options.add("watermobs");
                options.add("all");
                break;
        }

        options.removeIf(string ->
                !string.contains(
                    args[args.length-1].toLowerCase()));
        return options;
    }
}
