package de.macbrayne.fabric.weathersync.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import de.macbrayne.fabric.weathersync.Util;
import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.data.DWDParser;
import de.macbrayne.fabric.weathersync.state.SyncState;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class WeatherLocationCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        commandSourceStackCommandDispatcher.register(Commands.literal("weathersync")
                .then(Commands.literal("location")
                        .then(Commands.literal("set")
                                .then(Commands.argument("latitude", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("longitude", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    Double latitude = ctx.getArgument("latitude", Double.class);
                                                    Double longitude = ctx.getArgument("longitude", Double.class);
                                                    var locationComponent = Components.LOCATION.get(ctx.getSource().getPlayer());
                                                    locationComponent.setWeatherData(locationComponent.getWeatherData().withLocation(latitude.toString(), longitude.toString()));
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.location.set", latitude, longitude), false);
                                                    return 1;
                                                })
                                        )
                                ))
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    String latitude = Components.LOCATION.get(ctx.getSource().getPlayer()).getWeatherData().latitude();
                                    String longitude = Components.LOCATION.get(ctx.getSource().getPlayer()).getWeatherData().longitude();
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.location.get", latitude, longitude), false);
                                    return 1;
                                })))
                .then(Commands.literal("sync")
                        .executes(ctx -> {
                            var locationComponent = Components.LOCATION.get(ctx.getSource().getPlayer());
                            if(locationComponent.isEnabled()) {
                                locationComponent.getWeatherData().send(ctx.getSource().getPlayer());
                            }
                            return 1;
                        }))
                .then(Commands.literal("timer")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reset")
                                .executes(ctx -> {
                                    SyncState state = SyncState.getServerState(ctx.getSource().getServer());
                                    state.lastSync = -1;
                                    state.setDirty();
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.timer.reset"), false);
                                    return 1;
                                }))
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    SyncState state = SyncState.getServerState(ctx.getSource().getServer());
                                    long timeLeft = (state.lastSync - System.currentTimeMillis() + 1_800_000) / 60 / 1000;
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.timer.get", state.lastSync == -1 ? state.lastSync : timeLeft), false);
                                    return 1;
                                })))
                .then(Commands.literal("enable")
                        .executes(ctx -> {
                            var locationComponent = Components.LOCATION.get(ctx.getSource().getPlayer());
                            if(locationComponent.isEnabled()) {
                                ctx.getSource().sendFailure(Component.translatable("commands.weathersync.weathersync.enable.failed"));
                                return 0;
                            }
                            locationComponent.setEnabled(true);
                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.enable"), false);
                            if(locationComponent.getWeatherData() == null) {
                                DWDParser parser = new DWDParser(ctx.getSource().getPlayer());
                                parser.request(ctx.getSource().getPlayer());
                            } else {
                                locationComponent.getWeatherData().send(ctx.getSource().getPlayer());
                            }
                            return 1;
                        }))
                .then(Commands.literal("disable")
                        .executes(ctx -> {
                            var locationComponent = Components.LOCATION.get(ctx.getSource().getPlayer());
                            if(!locationComponent.isEnabled()) {
                                ctx.getSource().sendFailure(Component.translatable("commands.weathersync.weathersync.disable.failed"));
                                return 0;
                            }
                            locationComponent.setEnabled(false);
                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.disable"), false);
                            Util.sendVanillaWeather(ctx.getSource().getPlayer());
                            return 1;
                        }))
                .then(Commands.literal("credits")
                        .executes(ctx -> {
                            var credit = Component.literal("Macbrayne").withStyle(ChatFormatting.GOLD);
                            var meteoCredit = Component.literal("Open-Meteo.com").withStyle(Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://open-meteo.com/")).applyFormat(ChatFormatting.AQUA));
                            var dbCredit = Component.literal("DB-IP").withStyle(Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://db-ip.com")).applyFormat(ChatFormatting.AQUA));
                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.credits",
                                    credit, meteoCredit, dbCredit), false);
                            return 1;
                        }))
        );
    }
}
