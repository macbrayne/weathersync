package de.macbrayne.fabric.weathersync.data;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum City implements StringRepresentable {
    BERLIN("Berlin", "52.52", "13.405"),
    MUNICH("Munich", "48.1375", "11.575"),
    FRANKFURT("Frankfurt", "50.110556", "8.682222"),
    LONDON("London", "51.507222", "-0.1275"),
    TOKYO("Tokyo", "35.689722", "139.692222"),
    NEW_YORK_CITY("NewYorkCity", "40.712778", "-74.006111"),
    NEW_DELHI("NewDelhi", "28.613895", "77.209006"),
    AUCKLAND("Auckland", "-36.840556", "174.74");

    public final String key, latitude, longitude;
    public static final Codec<City> CODEC = StringRepresentable.fromEnum(City::values);

    private static final Map<City, WeatherData> WEATHER = new HashMap<>();
    private static final Map<String, City> LOOKUP = Arrays.stream(values()).collect(Collectors.toMap(city -> city.key, city -> city));

    City(String key, String latitude, String longitude) {
        this.key = key;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static void updateWeather(City city, WeatherData weatherData) {
        WEATHER.put(city, weatherData);
    }

    public static WeatherData getWeather(City city) {
        return WEATHER.get(city);
    }

    public static City get(String city) {
        return LOOKUP.get(city);
    }

    @Override
    public String getSerializedName() {
        return key;
    }
}
