package de.macbrayne.fabric.weathersync.data;

import com.google.gson.JsonParser;
import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.components.LocationComponent;
import de.macbrayne.fabric.weathersync.mixin.ServerPlayerAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DWDParser {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    private String latitude;
    private String longitude;
    private boolean isRaining;
    private boolean isThundering;

    public DWDParser(ServerPlayer player) {
        LocationComponent location = Components.LOCATION.get(player);
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.isRaining = player.level().isRaining();
        this.isThundering = player.level().isThundering();
    }

    public void request(ServerPlayer player) {
        LOGGER.debug("Uh oh, " + player.getName().getString() + " wants to know the weather!");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.open-meteo.com/v1/dwd-icon?latitude=" + latitude + "&longitude=" + longitude + "&current=precipitation,weather_code&timezone=GMT"))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(s -> DWDParser.parse(player, s, isThundering, isRaining));
    }

    private static void parse(ServerPlayer player, String json, boolean wasThundering, boolean isRaining) {
        var root = JsonParser.parseString(json);
        var current = root.getAsJsonObject().get("current");
        var weatherCode = current.getAsJsonObject().get("weather_code").getAsInt();
        WeatherData weatherData = WeatherData.fromCode("51.5344", "9.9349", weatherCode);
        ServerGamePacketListenerImpl connection = ((ServerPlayerAccessor) player).getConnection();
        LOGGER.debug("Updating Client Weather: It's " + (weatherData.isRaining() ? "raining" : "not raining") +
                " and " + (weatherData.isThundering() ? "thundering" : "not thundering") +
                " at " + player.getName().getString() + "'s location (weather code " + weatherCode + ")");
        if(weatherData.isRaining() && !isRaining) {
            connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
        } else if (!weatherData.isRaining() && isRaining) {
            connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
        }
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, weatherData.isThundering() ? 1.0F : 0.0F));
        connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, weatherData.isRaining() ? 1.0F : 0.0F));
        player.sendSystemMessage(Component.literal("Your weather has been synced with the real world!"));
        player.sendSystemMessage(Component.literal("It's " + (weatherData.isRaining() ? "raining" : "not raining") +
                " and " + (weatherData.isThundering() ? "thundering" : "not thundering") +
                " at your location (weather code " + weatherCode + ")"));
    }
}
