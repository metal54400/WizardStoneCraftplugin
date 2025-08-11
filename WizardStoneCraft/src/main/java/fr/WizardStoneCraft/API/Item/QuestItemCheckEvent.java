package fr.WizardStoneCraft.API.Item;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class QuestItemCheckEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private ItemStack item;  // modifiable
    private boolean valid;

    public QuestItemCheckEvent(Player player, ItemStack item, boolean valid) {
        this.player = player;
        this.item = item;
        this.valid = valid;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
