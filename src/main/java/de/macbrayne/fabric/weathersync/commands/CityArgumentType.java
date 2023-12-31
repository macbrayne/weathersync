package de.macbrayne.fabric.weathersync.commands;

import com.mojang.brigadier.context.CommandContext;
import de.macbrayne.fabric.weathersync.data.City;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;

public class CityArgumentType extends StringRepresentableArgument<City> {
    private CityArgumentType() {
        super(City.CODEC, City::values);
    }

    public static StringRepresentableArgument<City> city() {
        return new CityArgumentType();
    }

    public static City getCity(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, City.class);
    }
}
