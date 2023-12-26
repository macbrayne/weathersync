package de.macbrayne.fabric.weathersync.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record WeatherData(String latitude, String longitude, boolean isRaining, boolean isThundering, float precipitationLevel, float thunderLevel) {
    public static final Codec<WeatherData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("latitude").forGetter(WeatherData::latitude),
            Codec.STRING.fieldOf("longitude").forGetter(WeatherData::longitude),
            Codec.BOOL.fieldOf("isRaining").forGetter(WeatherData::isRaining),
            Codec.BOOL.fieldOf("isThundering").forGetter(WeatherData::isThundering),
            Codec.FLOAT.fieldOf("precipitationLevel").forGetter(WeatherData::precipitationLevel),
            Codec.FLOAT.fieldOf("thunderLevel").forGetter(WeatherData::thunderLevel)
    ).apply(instance, WeatherData::new));

    public WeatherData withLocation(String latitude, String longitude) {
        return new WeatherData(latitude, longitude, this.isRaining, this.isThundering, this.precipitationLevel, this.thunderLevel);
    }

    public static WeatherData fromCode(String latitude, String longitude, int weatherCode) {
        boolean isRaining = weatherCode < 95 && weatherCode >= 50;
        boolean isThundering = weatherCode >= 95;
        float precipitationLevel = switch(weatherCode) {
            case 51 -> 0.1F;
            case 53, 56, 71 -> 0.2F;
            case 55, 57 -> 0.3F;
            case 80, 61, 73 -> 0.5F;
            case 81, 63, 66, 75, 85 -> 0.75F;
            case 82, 65, 67, 77, 86 -> 1F;
            default -> 0F;
        };
        float thunderLevel = switch(weatherCode) {
            case 91, 93 -> 0.1F;
            case 92, 94 -> 0.3F;
            case 95 -> 0.5F;
            case 96 -> 0.75F;
            case 97, 98, 99 -> 1F;
            default -> 0F;
        };
        return new WeatherData(latitude, longitude, isRaining, isThundering, precipitationLevel, thunderLevel);
    }

    public void send(ServerGamePacketListenerImpl connection, boolean isRaining) {
        if(isRaining() && !isRaining) {
            connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
        } else if (isRaining() && isRaining) {
            connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
        }
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thunderLevel()));
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, precipitationLevel()));
    }
}
