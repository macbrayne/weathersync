package de.macbrayne.fabric.weathersync.data;

public record WeatherData(String latitude, String longitude, boolean isRaining, boolean isThundering) {
    public static WeatherData fromCode(String latitude, String longitude, int weatherCode) {
        boolean isRaining = weatherCode < 95 && weatherCode >= 50;
        boolean isThundering = weatherCode >= 95;
        return new WeatherData(latitude, longitude, isRaining, isThundering);
    }
}
