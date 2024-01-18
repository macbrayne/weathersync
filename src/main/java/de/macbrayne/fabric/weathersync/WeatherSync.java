package de.macbrayne.fabric.weathersync;

import de.macbrayne.fabric.weathersync.api.PriorityUrl;
import de.macbrayne.fabric.weathersync.api.WeatherApi;
import de.macbrayne.fabric.weathersync.commands.CityArgumentType;
import de.macbrayne.fabric.weathersync.commands.WeatherLocationCommand;
import de.macbrayne.fabric.weathersync.data.GeoIpProvider;
import de.macbrayne.fabric.weathersync.data.WeatherData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WeatherSync implements ModInitializer {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    @Override
    public void onInitialize() {
        LOGGER.info("WeatherSync is initializing");
        ArgumentTypeRegistry.registerArgumentType(new ResourceLocation("weathersync", "city"), CityArgumentType.class, SingletonArgumentInfo.contextFree(CityArgumentType::city));
        ServerLifecycleEvents.SERVER_STARTED.register(ServerStarted::fetchCityWeather);
        CommandRegistrationCallback.EVENT.register(WeatherLocationCommand::register);

        LOGGER.info("Registering weather backends");
        WeatherApi api = WeatherApi.getInstance();
        api.registerWeatherVariable("weather_code");
        api.registerBackend(PriorityUrl.of("https://api.open-meteo.com/v1/dwd-icon", 0));
        api.registerBackend(PriorityUrl.of(System.getProperty("weathersync.api-backend", "https://api.macbrayne.de/v1/dwd-icon"), 100));

        try {
            runSelfTest();
        } catch (Exception e) {
            LOGGER.error("Geo-ip self-test failed", e);
        }
    }

    private void runSelfTest() throws UnknownHostException {
        LOGGER.info("Executing geo-ip self-test");
        GeoIpProvider provider = new GeoIpProvider();
        LOGGER.debug("Created GeoIpProvider");
        InetAddress address = InetAddress.getByName("8.8.8.8");
        LOGGER.debug("Trying 8.8.8.8");
        WeatherData data = provider.tryGetLocation(address);
        if (data != null) {
            LOGGER.info("Found location: " + data.latitude() + " " + data.longitude() + " for ip 8.8.8.8");
        } else {
            LOGGER.info("Couldn't find location, weird");
        }
        LOGGER.info("Geo-ip self-test successful!");
    }
}
