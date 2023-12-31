package de.macbrayne.fabric.weathersync.components;

import de.macbrayne.fabric.weathersync.data.City;
import de.macbrayne.fabric.weathersync.data.LocationType;
import de.macbrayne.fabric.weathersync.data.WeatherData;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.server.level.ServerPlayer;

public interface LocationComponent extends Component {
    void setLocationType(LocationType locationType);
    LocationType getLocationType();
    void setCity(City city);
    City getCity();

    void setWeatherData(WeatherData data);
    WeatherData getWeatherData();
    void setEnabled(boolean enabled);
    boolean isEnabled();

    void send(ServerPlayer player);
}
