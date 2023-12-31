package de.macbrayne.fabric.weathersync.data;

import com.google.gson.JsonParser;
import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.components.LocationComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DWDParser {
    private static GeoIpProvider geoIpProvider = null;
    private static final String API_BACKEND = System.getProperty("weathersync.api-backend", "https://api.open-meteo.com/v1/dwd-icon");
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    private final String latitude;
    private final String longitude;

    public DWDParser(ServerPlayer player) {
        LocationComponent location = Components.LOCATION.get(player);
        location.setWeatherData(doGeoLocationIfPossible(player, location));
        this.latitude = location.getWeatherData().latitude();
        this.longitude = location.getWeatherData().longitude();
    }

    public WeatherData doGeoLocationIfPossible(ServerPlayer player, LocationComponent location) {
        if(location.getWeatherData() == null) {
            var defaultData = WeatherData.fromLocation("51.5344", "9.9349");
            if(geoIpProvider == null) {
                geoIpProvider = new GeoIpProvider(player.server);
            }
            if(geoIpProvider.isAvailable()) {
                var maybeIp = player.connection.getRemoteAddress();
                if(maybeIp instanceof InetSocketAddress socket) {
                    var geoLocation = geoIpProvider.tryGetLocation(socket.getAddress());
                    if(geoLocation != null) {
                        return geoLocation;
                    }
                    player.sendSystemMessage(Component.translatable("chat.weathersync.geoIpFailed", ChatFormatting.YELLOW));
                }
            }
            return defaultData;
        }
        return location.getWeatherData();
    }

    public void request(ServerPlayer player) {
        LOGGER.debug("Uh oh, " + player.getName().getString() + " wants to know the weather!");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BACKEND + "?latitude=" + latitude + "&longitude=" + longitude + "&current=weather_code&timezone=GMT"))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(s -> DWDParser.parse(player, s));
    }

    public static void requestCities(List<ServerPlayer> players) {
        for (City city : City.values()) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BACKEND + "?latitude=" + city.latitude + "&longitude=" + city.longitude + "&current=weather_code&timezone=GMT"))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(s -> DWDParser.parse(city, s, players));
        }
    }

    private static void parse(City city, String json, List<ServerPlayer> players) {
        var root = JsonParser.parseString(json);
        var current = root.getAsJsonObject().get("current");
        var weatherCode = current.getAsJsonObject().get("weather_code").getAsInt();
        WeatherData weatherData = WeatherData.fromLocation(city.latitude, city.longitude).withCode(weatherCode);
        City.updateWeather(city, weatherData);
        for(ServerPlayer player : players) {
            LocationComponent location = Components.LOCATION.get(player);
            if(location.getCity() == city) {
                location.send(player);
            }
        }
    }

    private static void parse(ServerPlayer player, String json) {
        var root = JsonParser.parseString(json);
        var current = root.getAsJsonObject().get("current");
        var weatherCode = current.getAsJsonObject().get("weather_code").getAsInt();
        LocationComponent location = Components.LOCATION.get(player);
        WeatherData weatherData = location.getWeatherData().withCode(weatherCode);
        location.setWeatherData(weatherData);
        location.send(player);
    }
}
