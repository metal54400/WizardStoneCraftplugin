package fr.WizardStoneCraft.API.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import java.util.UUID;

public class WeeklyReputationGiveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final UUID playerId;

    public WeeklyReputationGiveEvent(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

