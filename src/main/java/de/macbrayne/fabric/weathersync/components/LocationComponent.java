package de.macbrayne.fabric.weathersync.components;

import dev.onyxstudios.cca.api.v3.component.Component;

public interface LocationComponent extends Component {
    void setLatitude(String latitude);
    void setLongitude(String longitude);
    void setEnabled(boolean enabled);
    String getLatitude();
    String getLongitude();
    boolean isEnabled();
}
