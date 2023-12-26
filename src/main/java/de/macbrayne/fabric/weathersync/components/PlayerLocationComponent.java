package de.macbrayne.fabric.weathersync.components;

import de.macbrayne.fabric.weathersync.data.WeatherData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.slf4j.Logger;

public class PlayerLocationComponent implements LocationComponent {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    private WeatherData weatherData = null;
    private boolean enabled = false;

    @Override
    public void readFromNbt(CompoundTag tag) {
        if(!tag.getCompound("weatherData").isEmpty()) {
            setWeatherData(WeatherData.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("weatherData")).getOrThrow(true, LOGGER::error));
        }
        setEnabled(tag.getBoolean("enabled"));
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        if(getWeatherData() != null) {
            tag.put("weatherData", WeatherData.CODEC.encodeStart(NbtOps.INSTANCE, getWeatherData()).getOrThrow(false, LOGGER::error));
        }
        tag.putBoolean("enabled", isEnabled());
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
}
