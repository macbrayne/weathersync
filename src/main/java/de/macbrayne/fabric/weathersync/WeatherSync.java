package de.macbrayne.fabric.weathersync;

import de.macbrayne.fabric.weathersync.commands.WeatherLocationCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;

public class WeatherSync implements ModInitializer {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    @Override
    public void onInitialize() {
        LOGGER.info("WeatherSync is initializing");
        ServerPlayConnectionEvents.JOIN.register(Events::onPlayReady);
        CommandRegistrationCallback.EVENT.register(WeatherLocationCommand::register);
    }
}
