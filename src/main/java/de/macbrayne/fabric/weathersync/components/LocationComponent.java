package de.macbrayne.fabric.weathersync.components;

import de.macbrayne.fabric.weathersync.data.WeatherData;
import dev.onyxstudios.cca.api.v3.component.Component;

public interface LocationComponent extends Component {
    void setWeatherData(WeatherData data);
    WeatherData getWeatherData();
    void setEnabled(boolean enabled);
    boolean isEnabled();
}
