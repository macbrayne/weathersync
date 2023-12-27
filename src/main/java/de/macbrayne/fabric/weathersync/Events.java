package de.macbrayne.fabric.weathersync;

import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.components.LocationComponent;
import de.macbrayne.fabric.weathersync.data.DWDParser;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Events {
    private static final Logger LOGGER = LoggerFactory.getLogger("weathersync");
    static void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        LocationComponent location = Components.LOCATION.get(handler.player);
        if(location.isEnabled()) {
            if(location.getWeatherData() == null) {
                DWDParser parser = new DWDParser(handler.player);
                parser.request(handler.player);
            } else {
                location.getWeatherData().send(handler.player);
            }
        }
    }

    public static void onRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        LocationComponent location = Components.LOCATION.get(oldPlayer);
        if(location.isEnabled()) {
            location.getWeatherData().send(newPlayer);
        }
    }
}
