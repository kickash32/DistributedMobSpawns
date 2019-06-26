package me.kickash32.distributedmobspawns;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;


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
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) { return; }

        boolean allowed = this.entityProcessor.isSpawnAllowed(event.getLocation(), event.getEntityType());
        event.setCancelled(!allowed);
    }
}
