package de.macbrayne.fabric.weathersync.data;

import com.google.gson.JsonParser;
import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.components.LocationComponent;
import de.macbrayne.fabric.weathersync.mixin.ServerPlayerAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DWDParser {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    private final String latitude;
    private final String longitude;
    private final boolean isRaining;
    private final boolean isThundering;

    public DWDParser(ServerPlayer player) {
        LocationComponent location = Components.LOCATION.get(player);
        if(location.getWeatherData() == null) {
            location.setWeatherData(WeatherData.fromCode("51.5344", "9.9349", 0));
        }
        this.latitude = location.getWeatherData().latitude();
        this.longitude = location.getWeatherData().longitude();
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

        weatherData.send(connection, isRaining);

        LocationComponent location = Components.LOCATION.get(player);
        location.setWeatherData(weatherData);

        player.sendSystemMessage(Component.literal("Your weather has been synced with the real world!"));
        player.sendSystemMessage(Component.literal("It's " + (weatherData.isRaining() ? "raining" : "not raining") +
                " and " + (weatherData.isThundering() ? "thundering" : "not thundering") +
                " at your location (weather code " + weatherCode + ")"));
    }
}
