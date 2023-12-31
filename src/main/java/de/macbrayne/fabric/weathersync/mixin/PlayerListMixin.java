package de.macbrayne.fabric.weathersync.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.macbrayne.fabric.weathersync.components.Components;
import de.macbrayne.fabric.weathersync.components.LocationComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @ModifyExpressionValue(method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isRaining()Z"))
    boolean modifyLevelData(boolean original, @Local ServerPlayer serverPlayer) {
        LocationComponent location = Components.LOCATION.get(serverPlayer);
        if(location.isEnabled()) {
            location.send(serverPlayer);
            return false;
        }
        return original;
    }
}
