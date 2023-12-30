package de.macbrayne.fabric.weathersync;

import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class Util {
    public static void sendVanilla(ServerPlayer serverPlayer) {
        Level serverLevel = serverPlayer.level();
        serverPlayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverLevel.getSharedSpawnPos(), serverLevel.getSharedSpawnAngle()));
        if (serverLevel.isRaining()) {
            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, serverLevel.getRainLevel(1.0F)));
            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, serverLevel.getThunderLevel(1.0F)));
        } else {
            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
        }
    }
}
