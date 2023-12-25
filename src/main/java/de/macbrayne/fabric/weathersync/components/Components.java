package de.macbrayne.fabric.weathersync.components;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.resources.ResourceLocation;

public class Components implements EntityComponentInitializer {
    public static final ComponentKey<LocationComponent> LOCATION =
            ComponentRegistry.getOrCreate(new ResourceLocation("weathersync", "location"), LocationComponent.class);
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(LOCATION, player -> new PlayerLocationComponent(), RespawnCopyStrategy.ALWAYS_COPY);
    }
}
