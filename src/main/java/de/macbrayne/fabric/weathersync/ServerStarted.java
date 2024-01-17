package de.macbrayne.fabric.weathersync;

import de.macbrayne.fabric.weathersync.data.City;
import de.macbrayne.fabric.weathersync.data.DWDParser;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class ServerStarted {

    public static void fetchCityWeather(MinecraftServer minecraftServer) {
        for (City city : City.values()) {
            DWDParser.requestCity(city, minecraftServer, List.of());
        }
    }
}
