package de.macbrayne.fabric.weathersync.api;

import de.macbrayne.fabric.weathersync.data.WeatherData;

public interface WeatherDataListener {
    void onWeatherDataUpdate(float oldValue, float newValue);
}
