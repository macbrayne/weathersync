package de.macbrayne.fabric.weathersync.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.state.SyncState;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class WeatherLocationCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        commandSourceStackCommandDispatcher.register(Commands.literal("weathersync")
                .then(Commands.literal("set")
                        .then(Commands.literal("location")
                                .then(Commands.argument("latitude", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("longitude", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    Double latitude = ctx.getArgument("latitude", Double.class);
                                                    Double longitude = ctx.getArgument("longitude", Double.class);
                                                    var locationComponent = Components.LOCATION.get(ctx.getSource().getPlayer());
                                                    locationComponent.setWeatherData(locationComponent.getWeatherData().withLocation(latitude.toString(), longitude.toString()));
                                                    ctx.getSource().sendSuccess(() -> Component.literal("Successfully set location to " + latitude + ", " + longitude), false);
                                                    return 1;
                                                })
                                        )
                                ))
                        .then(Commands.literal("enabled")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            Boolean enabled = ctx.getArgument("enabled", Boolean.class);
                                            Components.LOCATION.get(ctx.getSource().getPlayer()).setEnabled(enabled);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Successfully set enabled to " + enabled), false);
                                            return 1;
                                        })
                                )))
                .then(Commands.literal("get")
                        .then(Commands.literal("location")
                                .executes(ctx -> {
                                    String latitude = Components.LOCATION.get(ctx.getSource().getPlayer()).getWeatherData().latitude();
                                    String longitude = Components.LOCATION.get(ctx.getSource().getPlayer()).getWeatherData().longitude();
                                    ctx.getSource().sendSuccess(() -> Component.literal("Location: " + latitude + ", " + longitude), false);
                                    return 1;
                                }))
                        .then(Commands.literal("enabled")
                                .executes(ctx -> {
                                    boolean enabled = Components.LOCATION.get(ctx.getSource().getPlayer()).isEnabled();
                                    ctx.getSource().sendSuccess(() -> Component.literal("Enabled: " + enabled), false);
                                    return 1;
                                })
                        ))
                .then(Commands.literal("sync")
                        .requires(source -> {
                            var locationComponent = Components.LOCATION.get(source.getPlayer());
                            return locationComponent.isEnabled();
                        })
                        .executes(ctx -> {
                            var locationComponent = Components.LOCATION.get(ctx.getSource().getPlayer());
                            if(locationComponent.isEnabled()) {
                                locationComponent.getWeatherData().send(ctx.getSource().getPlayer());
                            }
                            return 1;
                        }))
                .then(Commands.literal("timer")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reset"))
                        .executes(ctx -> {
                            SyncState state = SyncState.getServerState(ctx.getSource().getServer());
                            state.lastSync = -1;
                            state.setDirty();
                            return 1;
                        })
                )
        );
    }
}
