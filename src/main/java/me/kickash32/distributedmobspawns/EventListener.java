package me.kickash32.distributedmobspawns;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.logging.Level;


public class EventListener implements Listener {
    private DistributedMobSpawns controller;
    private EntityProcessor entityProcessor;

    EventListener(DistributedMobSpawns controller, EntityProcessor entityProcessor) {
        this.controller = controller;
        this.entityProcessor = entityProcessor;
    }

    //ENTITY EVENTS
    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (event.isCancelled()) { return; }
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) { return; }

        boolean allowed = this.entityProcessor.isSpawnAllowed(event.getLocation(), event.getEntityType());
        event.setCancelled(!allowed);
    }

    //WORLD EVENTS
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        controller.getServer().getLogger().log(Level.SEVERE, "[DMS] World loaded after startup unsupported!!!");
        controller.getServer().getLogger().log(Level.SEVERE, "[DMS] Restart the server to re-enable plugin");
    }
}
