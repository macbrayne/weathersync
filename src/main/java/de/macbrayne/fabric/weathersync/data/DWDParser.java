package de.macbrayne.fabric.weathersync.data;

import com.google.gson.JsonParser;
import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.components.LocationComponent;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DWDParser {
    private static final String API_BACKEND = System.getProperty("weathersync.api-backend", "https://api.open-meteo.com/v1/dwd-icon");
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    private final String latitude;
    private final String longitude;

    public DWDParser(ServerPlayer player) {
        LocationComponent location = Components.LOCATION.get(player);
        if(location.getWeatherData() == null) {
            location.setWeatherData(WeatherData.fromCode("51.5344", "9.9349", 0));
        }
        this.latitude = location.getWeatherData().latitude();
        this.longitude = location.getWeatherData().longitude();
    }

    public void request(ServerPlayer player) {
        LOGGER.debug("Uh oh, " + player.getName().getString() + " wants to know the weather!");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BACKEND + "?latitude=" + latitude + "&longitude=" + longitude + "&current=weather_code&timezone=GMT"))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(s -> DWDParser.parse(player, s));
    }

    private static void parse(ServerPlayer player, String json) {
        var root = JsonParser.parseString(json);
        var current = root.getAsJsonObject().get("current");
        var weatherCode = current.getAsJsonObject().get("weather_code").getAsInt();
        WeatherData weatherData = WeatherData.fromCode("51.5344", "9.9349", weatherCode);
        weatherData.send(player);
        LocationComponent location = Components.LOCATION.get(player);
        location.setWeatherData(weatherData);
    }
}
