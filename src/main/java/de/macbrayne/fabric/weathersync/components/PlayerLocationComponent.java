package de.macbrayne.fabric.weathersync.components;

import de.macbrayne.fabric.weathersync.data.City;
import de.macbrayne.fabric.weathersync.data.LocationType;
import de.macbrayne.fabric.weathersync.data.WeatherData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class PlayerLocationComponent implements LocationComponent {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    private WeatherData weatherData = null;
    private boolean enabled = false;
    private LocationType locationType = LocationType.CITY;
    private City city = City.BERLIN;

    @Override
    public void readFromNbt(CompoundTag tag) {
        if(!tag.getCompound("weatherData").isEmpty()) {
            setWeatherData(WeatherData.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("weatherData")).getOrThrow(true, LOGGER::error));
        }
        setLocationType(LocationType.get(tag.getString("locationType")));
        setCity(City.get(tag.getString("city")));
        setEnabled(tag.getBoolean("enabled"));
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        if(getWeatherData() != null) {
            tag.put("weatherData", WeatherData.CODEC.encodeStart(NbtOps.INSTANCE, getWeatherData()).getOrThrow(false, LOGGER::error));
        }
        tag.putString("locationType", getLocationType().key);
        tag.putString("city", city.key);
        tag.putBoolean("enabled", isEnabled());
    }


    @Override
    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    @Override
    public LocationType getLocationType() {
        return locationType;
    }
    @Override
    public void setCity(City city) {
        this.city = city;
    }

    @Override
    public City getCity() {
        return city;
    }

    @Override
    public void setWeatherData(WeatherData weatherData) {
        this.weatherData = weatherData;
    }

    @Override
    public WeatherData getWeatherData() {
        return weatherData;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void send(ServerPlayer player) {
        if(!isEnabled()) {
            return;
        }
        switch (getLocationType()) {
            case CITY -> {
                var city = City.getWeather(getCity());
                city.send(player);
            }
            case CUSTOM -> {
                getWeatherData().send(player);
            }
        }
    }
}
