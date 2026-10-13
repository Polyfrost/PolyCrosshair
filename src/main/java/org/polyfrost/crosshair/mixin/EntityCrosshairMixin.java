package org.polyfrost.crosshair.mixin;

import org.polyfrost.crosshair.Compat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "tomeko.entitycrosshair.config.CrosshairRenderer", remap = false)
public abstract class EntityCrosshairMixin {

    @Inject(
        method =
        //? if >=1.21 {
        "render"
        //?} else {
        /*"drawCrosshair"
        *///?}
        , at = @At("HEAD"), cancellable = true)
    private void polycrosshair$override(CallbackInfo ci) {
        if (Compat.overriding()) ci.cancel();
    }
}
