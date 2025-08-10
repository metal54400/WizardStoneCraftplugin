package fr.WizardStoneCraft.data;

import java.util.UUID;

public class TombData {
    private final UUID ownerUUID;
    private final long creationTime;
    private final int hologramTaskId;

    public TombData(UUID ownerUUID, long creationTime, int hologramTaskId) {
        this.ownerUUID = ownerUUID;
        this.creationTime = creationTime;
        this.hologramTaskId = hologramTaskId;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public int getHologramTaskId() {
        return hologramTaskId;
    }
}

