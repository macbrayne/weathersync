package de.macbrayne.fabric.weathersync;

import de.macbrayne.fabric.weathersync.data.DWDParser;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Events {
    private static final Logger LOGGER = LoggerFactory.getLogger("weathersync");
    static void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        LOGGER.debug("Sending weather data to player " + handler.player.getName().getString());
        DWDParser parser = new DWDParser(handler.player);
        parser.request(handler.player);
    }


}
