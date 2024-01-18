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
    public static int ticksBetweenSyncs = Integer.parseInt(System.getProperty("weathersync.minutesBetweenSyncs", "30")) * 60 * 20;
    public int apiRequests = 0;
    public LocalDate nextReset = LocalDate.now();
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("apiRequests", apiRequests);
        compoundTag.putLong("nextReset", nextReset.toEpochDay());
        return compoundTag;
    }

    public static SyncState fromNbt(CompoundTag compoundTag) {
        SyncState syncState = new SyncState();
        syncState.apiRequests = compoundTag.getInt("apiRequests");
        syncState.nextReset = LocalDate.ofEpochDay(compoundTag.getLong("nextReset"));
        syncState.checkDateReset();
        return syncState;
    }

    public void checkDateReset() {
        LocalDate currentDate = LocalDate.now(Clock.systemUTC());
        if(currentDate.isAfter(nextReset)) {
            apiRequests = 0;
            nextReset = currentDate;
            setDirty();
        }
    }

    public static SyncState getServerState(MinecraftServer server) {
        DimensionDataStorage storage = server.getLevel(Level.OVERWORLD).getDataStorage();
        SyncState state = storage.computeIfAbsent(TYPE, "weathersync");
        return state;
    }
}
