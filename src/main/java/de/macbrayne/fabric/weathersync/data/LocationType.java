package de.macbrayne.fabric.weathersync.data;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum LocationType {
    CITY("city"),
    CUSTOM("custom");

    public final String key;

    LocationType(String key) {
        this.key = key;
    }

    private final static Map<String, LocationType> LOOKUP = Arrays.stream(LocationType.values()).collect(Collectors.toMap(locationType -> locationType.key, locationType -> locationType));

    public static LocationType get(String key) {
        return LOOKUP.get(key);
    }
}
