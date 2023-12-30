package de.macbrayne.fabric.weathersync.data;

import java.util.Map;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * WMO Codes according to <a href="https://www.nodc.noaa.gov/archive/arc0021/0002199/1.1/data/0-data/HTML/WMO-CODE/WMO4677.HTM">nodc.noaa.gov</a>
 */
public enum WeatherCode {
    CLEAR(code -> code <= 50 && code != 17 && code != 29, 0F, 0F),
    LIGHT_DRIZZLE(code -> code == 50 || code == 51 || code == 56 || code == 78, 0.1F, 0F),
    MODERATE_DRIZZLE(code -> code == 52 || code == 53 || code == 76, 0.2F, 0F),
    HEAVY_DRIZZLE(code -> code == 54 || code == 55 || code == 57 || code == 58 || code == 77, 0.3F, 0F),
    HEAVY_DRIZZLE_AND_RAIN(code -> code == 59 || code == 79, 0.5F, 0F),
    LIGHT_RAIN(code -> code == 60 || code == 61 || code == 70 || code == 71 || code == 87, 0.5F, 0F),
    MODERATE_RAIN(code -> code == 62 || code == 63 || code == 66 || code == 68 || code == 72 || code == 73 || code == 80 || code == 83 || code == 85 || code == 89, 0.6F, 0F),
    HEAVY_RAIN(code -> code == 64 || code == 65 || code == 67 || code == 69 || code == 74 || code == 75 || code == 81 || code == 86, 0.75F, 0F),
    VIOLENT_RAIN(code -> code == 82 || code == 84 || code == 90, 1F, 0F),
    PREVIOUSLY_THUNDERSTORM_LIGHT_RAIN(code -> code == 29 || code == 91 || code == 93, 0.3F, 0.1F),
    PREVIOUSLY_THUNDERSTORM_MODERATE_RAIN(code -> code == 92 || code == 94, 0.8F, 0.1F),
    THUNDERSTORM_NO_RAIN(code -> code == 17, 0F, 0.3F),
    LIGHT_THUNDERSTORM(code -> code == 95, 0.3F, 0.4F),
    MODERATE_THUNDERSTORM(code -> code == 96, 0.6F, 0.5F),
    HEAVY_THUNDERSTORM_NO_HAIL(code -> code == 97, 0.8F, 1F),
    HEAVY_THUNDERSTORM(code -> code >= 98, 1.F, 1F);


    private final IntPredicate values;
    private final float rainLevel, thunderLevel;

    private final static Map<Integer, WeatherCode> LOOKUP = IntStream.range(0, 100).boxed().collect(Collectors.toMap(i -> i, WeatherCode::fromCode));
    WeatherCode(IntPredicate values, float rainLevel, float thunderLevel) {
        this.values = values;
        this.rainLevel = rainLevel;
        this.thunderLevel = thunderLevel;
    }

    public static WeatherCode fromCode(int code) {
        if(LOOKUP != null) {
            return LOOKUP.get(code);
        }
        for(WeatherCode weatherCode : WeatherCode.values()) {
            if(weatherCode.values.test(code)) {
                return weatherCode;
            }
        }
        return CLEAR;
    }

    public float rainLevel() {
        return rainLevel;
    }


    public float thunderLevel() {
        return thunderLevel;
    }
}
