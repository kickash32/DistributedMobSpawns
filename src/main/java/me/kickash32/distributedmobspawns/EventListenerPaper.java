package me.kickash32.distributedmobspawns;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EventListenerPaper implements Listener {
    private DistributedMobSpawns controller;
    private EntityProcessor entityProcessor;

    EventListenerPaper(DistributedMobSpawns controller, EntityProcessor entityProcessor){
        this.controller = controller;
        this.entityProcessor = entityProcessor;
    }

    @EventHandler
    public void onPlayerNaturallySpawnCreaturesEvent(PlayerNaturallySpawnCreaturesEvent event) {
        if(entityProcessor.allFull(event.getPlayer())){
            event.setCancelled(true);
        }
    }

    //ENTITY EVENTS
    @EventHandler
    public void onPreCreatureSpawnEvent(PreCreatureSpawnEvent event) {
        if (event.getReason() != CreatureSpawnEvent.SpawnReason.NATURAL) { return; }

        boolean allowed = this.entityProcessor.isSpawnAllowed(event.getSpawnLocation(), event.getType());
        event.setCancelled(!allowed);
        event.setShouldAbortSpawn(!allowed);
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) { return; }

        boolean allowed = this.entityProcessor.isSpawnAllowed(event.getLocation(), event.getEntityType());
        event.setCancelled(!allowed);
    }
}
