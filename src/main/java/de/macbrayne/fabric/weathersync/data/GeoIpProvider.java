package de.macbrayne.fabric.weathersync.data;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Optional;

public class GeoIpProvider {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    private final DatabaseReader reader;

    public GeoIpProvider(MinecraftServer server) {
        DatabaseReader reader = null;
        try {
            var loader = FabricLoader.getInstance();
            var path = loader.getModContainer("weathersync").get().findPath("assets/weathersync/dbip-city-lite-2023-12.mmdb").get();
            InputStream database = Files.newInputStream(path);
            reader = new DatabaseReader.Builder(database).build();
            LOGGER.info("Loaded GeoIP database");
        } catch (IOException e) {
            LOGGER.error("Could not load GeoIP database", e);
        } finally {
            this.reader = reader;
        }
    }

    public WeatherData tryGetLocation(InetAddress address) {
        Optional<CityResponse> city;
        System.out.println(address);
        try {
            city = reader.tryCity(address);
        } catch (IOException | GeoIp2Exception e) {
            LOGGER.error("Could not get location for " + address, e);
            city = Optional.empty();
        }
        if(city.isPresent()) {
            Location location = city.get().getLocation();
            return WeatherData.fromLocation(location.getLatitude().toString(), location.getLongitude().toString());
        } else {
            return null;
        }
    }

    public boolean isAvailable() {
        return reader != null;
    }
}
