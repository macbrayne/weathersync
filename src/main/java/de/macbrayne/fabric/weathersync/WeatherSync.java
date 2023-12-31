package de.macbrayne.fabric.weathersync;

import de.macbrayne.fabric.weathersync.commands.CityArgumentType;
import de.macbrayne.fabric.weathersync.commands.WeatherLocationCommand;
import de.macbrayne.fabric.weathersync.data.DWDParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.List;

public class WeatherSync implements ModInitializer {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("weathersync");
    @Override
    public void onInitialize() {
        LOGGER.info("WeatherSync is initializing");
        ArgumentTypeRegistry.registerArgumentType(new ResourceLocation("weathersync", "city"), CityArgumentType.class, SingletonArgumentInfo.contextFree(CityArgumentType::city));
        DWDParser.requestCities(List.of());
        CommandRegistrationCallback.EVENT.register(WeatherLocationCommand::register);
    }
}
