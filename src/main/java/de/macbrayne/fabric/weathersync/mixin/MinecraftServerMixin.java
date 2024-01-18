package de.macbrayne.fabric.weathersync.mixin;

import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.components.LocationComponent;
import de.macbrayne.fabric.weathersync.data.City;
import de.macbrayne.fabric.weathersync.data.DWDParser;
import de.macbrayne.fabric.weathersync.data.LocationType;
import de.macbrayne.fabric.weathersync.data.WeatherData;
import de.macbrayne.fabric.weathersync.state.SyncState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "tickChildren", at = @At("RETURN"))
    private void tickChildren(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer)(Object)this;
        if(server.getTickCount() % SyncState.ticksBetweenSyncs == 0) {
            SyncState state = SyncState.getServerState(server);
            state.checkDateReset();

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
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
                DWDParser.requestCity(city, server);
            }
        } else if (server.getTickCount() % SyncState.ticksBetweenSyncs == SyncState.ticksBetweenSyncs / 4) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                LocationComponent location = Components.LOCATION.get(player);
                location.send(player);
            }
        }
    }
}
