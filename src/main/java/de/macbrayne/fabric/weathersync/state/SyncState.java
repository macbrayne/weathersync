package de.macbrayne.fabric.weathersync.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class SyncState extends SavedData {
    public static SyncState reference = null;
    private static SavedData.Factory<SyncState> TYPE = new SavedData.Factory<>(SyncState::new, SyncState::fromNbt, null);
    public long lastSync = -1;
    public String defaultLatitude = "52.5162";
    public String defaultLongitude = "13.3777";
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putLong("lastSync", lastSync);
        compoundTag.putString("defaultLatitude", defaultLatitude);
        compoundTag.putString("defaultLongitude", defaultLongitude);
        return compoundTag;
    }

    public static SyncState fromNbt(CompoundTag compoundTag) {
        SyncState syncState = new SyncState();
        syncState.lastSync = compoundTag.getLong("lastSync");
        syncState.defaultLatitude = compoundTag.getString("defaultLatitude");
        syncState.defaultLongitude = compoundTag.getString("defaultLongitude");
        reference = syncState;
        return syncState;
    }

    public static SyncState getServerState(MinecraftServer server) {
        DimensionDataStorage storage = server.getLevel(Level.OVERWORLD).getDataStorage();
        SyncState state = storage.computeIfAbsent(TYPE, "weathersync");
        reference = state;
        return state;
    }
}
