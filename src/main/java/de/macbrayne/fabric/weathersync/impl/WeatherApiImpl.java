package de.macbrayne.fabric.weathersync.impl;

import de.macbrayne.fabric.weathersync.api.PriorityUrl;
import de.macbrayne.fabric.weathersync.api.WeatherApi;

import java.util.ArrayList;
import java.util.List;

public class WeatherApiImpl implements WeatherApi {
    public static final WeatherApiImpl INSTANCE = new WeatherApiImpl();
    private final List<String> weatherVariables = new ArrayList<>();
    private final List<PriorityUrl> backends = new ArrayList<>();
    private PriorityUrl currentBackend;

    @Override
    public boolean registerBackend(PriorityUrl backend) {
        if(backend.isAvailable()) {
            backends.add(backend);
            if(currentBackend == null || backend.priority() > currentBackend.priority()) {
                currentBackend = backend;
            }
            return true;
        }
        return false;
    }

    @Override
    public PriorityUrl getCurrentBackend() {
        return currentBackend;
    }

    @Override
    public void registerWeatherVariable(String variable) {
        weatherVariables.add(variable);
    }

    @Override
    public List<String> getWeatherVariables() {
        return weatherVariables;
    }

    public String getWeatherVariableQueryString() {
        return String.join(",", weatherVariables);
    }
}
