package de.macbrayne.fabric.weathersync.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.macbrayne.fabric.weathersync.api.PriorityUrl;
import de.macbrayne.fabric.weathersync.api.WeatherApi;
import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.components.LocationComponent;
import de.macbrayne.fabric.weathersync.impl.WeatherApiImpl;
import de.macbrayne.fabric.weathersync.state.SyncState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Consumer;

public class DWDParser {
    private static GeoIpProvider geoIpProvider = null;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");

    public DWDParser() {
    }

    public WeatherData doGeoLocationIfPossible(ServerPlayer player, LocationComponent location) {
        LOGGER.debug("Attempting GeoIP for " + player.getName().getString());
        if(geoIpProvider == null) {
            geoIpProvider = new GeoIpProvider();
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
        return null;
    }

    public void request(ServerPlayer player, String latitude, String longitude) {
        LOGGER.debug("Uh oh, " + player.getName().getString() + " wants to know the weather!");
        SyncState state = SyncState.getServerState(player.getServer());
        if(state.apiRequests.get() > 8_000) {
            LOGGER.error("Daily API quota at 90%, stop syncing player weather");
            return;
        }
        LocationComponent location = Components.LOCATION.get(player);
        commonRequest(player.getServer(), latitude, longitude, location.getWeatherData(), weatherData -> {
            location.setWeatherData(weatherData);
            location.send(player);
        });
    }

    public static void requestCity(City city, MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        commonRequest(server, city.latitude, city.longitude, City.getWeather(city), weatherData -> {
            City.updateWeather(city, weatherData);
            for(ServerPlayer player : players) {
                LocationComponent location = Components.LOCATION.get(player);
                if(location.getCity() == city) {
                    location.send(player);
                }
            }
        });
    }

    private static void commonRequest(MinecraftServer server, String latitude, String longitude, WeatherData original, Consumer<WeatherData> callback) {
        PriorityUrl backend = WeatherApi.getInstance().getCurrentBackend();
        if(backend == null) {
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backend.url() + "?latitude=" + latitude + "&longitude=" + longitude + "&current=" + WeatherApiImpl.INSTANCE.getWeatherVariableQueryString() + "&timezone=GMT"))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(s -> DWDParser.parse(original, s, callback));
        SyncState state = SyncState.getServerState(server);
        state.apiRequests.getAndIncrement();
        state.setDirty();
    }

    private static void parse(WeatherData original, String json, Consumer<WeatherData> callback) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject current = root.getAsJsonObject("current");
        int weatherCode = current.get("weather_code").getAsInt();
        WeatherData weatherData = original.withCode(weatherCode);
        callback.accept(weatherData);
    }
}
