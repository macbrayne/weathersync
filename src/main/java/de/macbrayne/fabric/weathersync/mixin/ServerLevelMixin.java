package de.macbrayne.fabric.weathersync.mixin;

import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.components.LocationComponent;
import de.macbrayne.fabric.weathersync.data.City;
import de.macbrayne.fabric.weathersync.data.DWDParser;
import de.macbrayne.fabric.weathersync.data.LocationType;
import de.macbrayne.fabric.weathersync.data.WeatherData;
import de.macbrayne.fabric.weathersync.state.SyncState;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Debug(export = true)
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements
        WorldGenLevel {
    @Unique
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");

    @Shadow public abstract @NotNull MinecraftServer getServer();

    protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
    }

    @Inject(method = "advanceWeatherCycle()V", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;rainLevel:F", opcode = Opcodes.GETFIELD, ordinal = 4), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private void doGeoWeather(CallbackInfo ci, boolean wasRaining) {
        ci.cancel();
        if(wasRaining != this.isRaining()) {

            SyncState state = SyncState.getServerState(this.getServer());
            boolean doNewSync = state.lastSync + 1.8e6 > System.currentTimeMillis() || state.lastSync == -1;
            for (ServerPlayer player : this.getServer().getPlayerList().getPlayers()) {
                LocationComponent location = Components.LOCATION.get(player);
                if (!location.isEnabled()) {
                    if (this.isRaining()) {
                        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
                    } else {
                        player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
                    }
                    player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel));
                    player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel));
                } else if (!doNewSync) {
                    LocationComponent locationComponent = Components.LOCATION.get(player);
                    locationComponent.send(player);
                }
            }
            if (!doNewSync) {
                LOGGER.debug("Not syncing weather with real world");
                return;
            }
            LOGGER.debug("Syncing weather with real world");
            state.lastSync = System.currentTimeMillis();
            state.setDirty();
            for (ServerPlayer player : this.getServer().getPlayerList().getPlayers()) {
                LocationComponent location = Components.LOCATION.get(player);
                if (!location.isEnabled()) {
                    continue;
                }
                if (location.getLocationType() != LocationType.CITY) {
                    WeatherData data = location.getWeatherData();
                    DWDParser parser = new DWDParser();
                    parser.request(player, data.latitude(), data.longitude());
                }
            }
            for (City city : City.values()) {
                DWDParser.requestCity(city, getServer());
            }
        }
    }
}
