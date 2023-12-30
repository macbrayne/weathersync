package de.macbrayne.fabric.weathersync.data;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

public class GeoIpProvider {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    private final DatabaseReader reader;

    public GeoIpProvider() {
        File database = new File("GeoLite2-City.mmdb");
        DatabaseReader reader = null;
        try {
            reader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            LOGGER.error("Could not load GeoIP database", e);
        } finally {
            this.reader = reader;
        }
    }

    public WeatherData tryGetLocation(InetAddress address) {
        Optional<CityResponse> city;
        try {
            city = reader.tryCity(address);
        } catch (IOException | GeoIp2Exception e) {
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
