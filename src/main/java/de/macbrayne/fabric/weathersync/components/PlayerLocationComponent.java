package de.macbrayne.fabric.weathersync.components;

import net.minecraft.nbt.CompoundTag;

public class PlayerLocationComponent implements LocationComponent {
    private String latitude = "52.5162", longitude = "13.3777";
    private boolean enabled = false;

    @Override
    public void readFromNbt(CompoundTag tag) {
        latitude = tag.getString("latitude");
        longitude = tag.getString("longitude");
        enabled = tag.getBoolean("enabled");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putString("latitude", latitude);
        tag.putString("longitude", longitude);
        tag.putBoolean("enabled", enabled);
    }

    @Override
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @Override
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getLatitude() {
        return this.latitude;
    }

    @Override
    public String getLongitude() {
        return this.longitude;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
