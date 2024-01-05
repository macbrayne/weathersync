package de.macbrayne.fabric.weathersync.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncState extends SavedData {
    private static final SavedData.Factory<SyncState> TYPE = new SavedData.Factory<>(SyncState::new, SyncState::fromNbt, null);
    public long lastSync = -1;
    public AtomicInteger apiRequests = new AtomicInteger(0);
    public LocalDate nextReset = LocalDate.now();
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putLong("lastSync", lastSync);
        compoundTag.putInt("apiRequests", apiRequests.get());
        compoundTag.putLong("nextReset", nextReset.toEpochDay());
        return compoundTag;
    }

    public static SyncState fromNbt(CompoundTag compoundTag) {
        SyncState syncState = new SyncState();
        syncState.lastSync = compoundTag.getLong("lastSync");
        int previousQuota = compoundTag.getInt("apiRequests");
        LocalDate nextReset = LocalDate.ofEpochDay(compoundTag.getLong("nextReset"));
        LocalDate currentDate = LocalDate.now(Clock.systemUTC());
        if(currentDate.isAfter(nextReset)) {
            syncState.apiRequests.set(0);
            syncState.nextReset = currentDate.plusDays(1);
            syncState.setDirty();
        } else {
            syncState.apiRequests.set(previousQuota);
            syncState.nextReset = nextReset;
        }
        return syncState;
    }

    public static SyncState getServerState(MinecraftServer server) {
        DimensionDataStorage storage = server.getLevel(Level.OVERWORLD).getDataStorage();
        SyncState state = storage.computeIfAbsent(TYPE, "weathersync");
        return state;
    }
}
