package de.macbrayne.fabric.weathersync.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;

public record WeatherData(String latitude, String longitude, boolean isRaining, boolean isThundering, float rainLevel, float thunderLevel) {
    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    public static final Codec<WeatherData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("latitude").forGetter(WeatherData::latitude),
            Codec.STRING.fieldOf("longitude").forGetter(WeatherData::longitude),
            Codec.BOOL.fieldOf("isRaining").forGetter(WeatherData::isRaining),
            Codec.BOOL.fieldOf("isThundering").forGetter(WeatherData::isThundering),
            Codec.FLOAT.fieldOf("rainLevel").forGetter(WeatherData::rainLevel),
            Codec.FLOAT.fieldOf("thunderLevel").forGetter(WeatherData::thunderLevel)
    ).apply(instance, WeatherData::new));

    public WeatherData withLocation(String latitude, String longitude) {
        return new WeatherData(latitude, longitude, this.isRaining, this.isThundering, this.rainLevel, this.thunderLevel);
    }

    public static WeatherData fromLocation(String latitude, String longitude) {
        return new WeatherData(latitude, longitude, false, false, 0.0F, 0.0F);
    }

    public WeatherData withCode(int weatherCode) {
        WeatherCode code = WeatherCode.fromCode(weatherCode);
        float rainLevel = code.rainLevel();
        float thunderLevel = code.thunderLevel();
        return new WeatherData(this.latitude, this.longitude, rainLevel > 0, thunderLevel > 0, rainLevel, thunderLevel);
    }

    public void send(ServerPlayer player) {
        LOGGER.debug("Sending weather data to player " + player.getName().getString());
        ServerGamePacketListenerImpl connection = player.connection;
        if(isRaining()) {
            connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
        } else {
            connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
        }
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thunderLevel()));
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, rainLevel()));


        player.sendSystemMessage(Component.translatable("chat.weathersync.sync"));
        String suffix = isThundering() ? "thunder" : (isRaining() ? "rain" : "clear");
        player.sendSystemMessage(Component.translatable("chat.weathersync.status", Component.translatable("chat.weathersync.status." + suffix)));
    }
}
