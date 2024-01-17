package de.macbrayne.fabric.weathersync.api;

import de.macbrayne.fabric.weathersync.impl.WeatherApiImpl;

import java.util.List;

public interface WeatherApi {
    static WeatherApi getInstance() {
        return WeatherApiImpl.INSTANCE;
    }
    boolean registerBackend(PriorityUrl backend);
    PriorityUrl getCurrentBackend();
    void registerWeatherVariable(String variable);

    List<String> getWeatherVariables();
}
