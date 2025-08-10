package fr.WizardStoneCraft.API.event;

import dev.geco.gsit.object.GSeat;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerAttemptSitEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final GSeat seat;
    private boolean cancelled;

    public PlayerAttemptSitEvent(Player player, GSeat seat) {
        this.player = player;
        this.seat = seat;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public GSeat getSeat() {
        return seat;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
