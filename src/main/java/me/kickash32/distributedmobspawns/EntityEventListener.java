package me.kickash32.distributedmobspawns;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;


public class EntityEventListener implements Listener {
    private DistributedMobSpawns controller;
    private EntityProcessor entityProcessor;

    EntityEventListener(DistributedMobSpawns controller, EntityProcessor entityProcessor){
        this.controller = controller;
        this.entityProcessor = entityProcessor;
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (this.controller.isDisabled()) {
            return;
        }
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }
        this.entityProcessor.enqueue(event.getEntity());
    }
}
