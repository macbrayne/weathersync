package de.macbrayne.fabric.weathersync.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import de.macbrayne.fabric.weathersync.Util;
import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.data.City;
import de.macbrayne.fabric.weathersync.data.DWDParser;
import de.macbrayne.fabric.weathersync.data.LocationType;
import de.macbrayne.fabric.weathersync.data.WeatherData;
import de.macbrayne.fabric.weathersync.state.SyncState;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class WeatherLocationCommand {
    private static final int MAX_SYNC_FREQUENCY = Integer.parseInt(System.getProperty("weathersync.max-sync-frequency", "30"));

    public static void register(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        commandSourceStackCommandDispatcher.register(Commands.literal("weathersync")
                .then(Commands.literal("location")
                        .then(Commands.literal("set")
                                .then(Commands.literal("auto")
                                        .executes(context -> {
                                            var locationComponent = Components.LOCATION.get(context.getSource().getPlayer());
                                            locationComponent.setWeatherData(null);
                                            DWDParser dwdParser = new DWDParser();
                                            WeatherData maybeData = dwdParser.doGeoLocationIfPossible(context.getSource().getPlayer(), locationComponent);
                                            if(maybeData == null) {
                                                context.getSource().sendFailure(Component.translatable("chat.weathersync.geoIpFailed"));
                                                return 0;
                                            }
                                            locationComponent.setWeatherData(maybeData);
                                            locationComponent.setLocationType(LocationType.CUSTOM);
                                            dwdParser.request(context.getSource().getPlayer(), maybeData.latitude(), maybeData.longitude());
                                            context.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.location.set.auto"), false);
                                            return 1;
                                        }))
                                .then(Commands.literal("city")
                                        .then(Commands.argument("name", CityArgumentType.city())
                                                .executes(ctx -> {
                                                    City city = CityArgumentType.getCity(ctx, "name");
                                                    var cityComponent = Component.translatable("commands.weathersync.weathersync.location.city." + city.key);
                                                    var locationComponent = Components.LOCATION.get(ctx.getSource().getPlayer());
                                                    locationComponent.setLocationType(LocationType.CITY);
                                                    locationComponent.setCity(city);
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.location.set.city", city.latitude, city.longitude, cityComponent), false);
                                                    locationComponent.send(ctx.getSource().getPlayer());
                                                    return 1;
                                                })))
                                .then(Commands.literal("custom")
                                        .then(Commands.argument("latitude", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("longitude", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            String latitude = ctx.getArgument("latitude", Double.class).toString();
                                                            String longitude = ctx.getArgument("longitude", Double.class).toString();
                                                            var locationComponent = Components.LOCATION.get(ctx.getSource().getPlayer());
                                                            locationComponent.setLocationType(LocationType.CUSTOM);
                                                            locationComponent.setWeatherData(WeatherData.fromLocation(latitude, longitude));
                                                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.location.set", latitude, longitude), false);
                                                            DWDParser dwdParser = new DWDParser();
                                                            dwdParser.request(ctx.getSource().getPlayer(), latitude, longitude);
                                                            return 1;
                                                        })))))
                        .then(Commands.literal("get")
                                .executes(ctx -> {
                                    var location = Components.LOCATION.get(ctx.getSource().getPlayer());
                                    switch (location.getLocationType()) {
                                        case CITY -> {
                                            var city = Component.translatable("commands.weathersync.weathersync.location.city." + location.getCity().key);
                                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.location.get.city", location.getCity().latitude, location.getCity().longitude, city), false);
                                        }
                                        case CUSTOM -> {
                                            String latitude = location.getWeatherData().latitude();
                                            String longitude = location.getWeatherData().longitude();
                                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.location.get.custom", latitude, longitude), false);
                                        }
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("sync")
                        .executes(ctx -> {
                            var locationComponent = Components.LOCATION.get(ctx.getSource().getPlayer());
                            if(locationComponent.isEnabled()) {
                                locationComponent.send(ctx.getSource().getPlayer());
                            }
                            return 1;
                        }))
                .then(Commands.literal("timer")
                        .then(Commands.literal("reset")
                                .requires(source -> source.hasPermission(2))
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
                                    long timeLeft = ((state.lastSync - System.currentTimeMillis()) / 60 / 1000 + MAX_SYNC_FREQUENCY);
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
                            locationComponent.send(ctx.getSource().getPlayer());
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
                .then(Commands.literal("quota")
                        .requires(source -> source.hasPermission(1))
                        .then(Commands.literal("get")
                                .requires(source -> source.hasPermission(1))
                                .executes(ctx -> {
                                    SyncState state = SyncState.getServerState(ctx.getSource().getServer());
                                    String date = state.nextReset.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.ENGLISH));
                                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.weathersync.weathersync.quota.get", state.apiRequests.get(), date), false);
                                    return 1;
                                })))
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
