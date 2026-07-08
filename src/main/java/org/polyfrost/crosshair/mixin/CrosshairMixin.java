package org.polyfrost.crosshair.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
//? if >=26.2 {
/*import net.minecraft.client.gui.Hud;
*///?} else {
import net.minecraft.client.gui.Gui;
//?}
import org.polyfrost.crosshair.render.CrosshairRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    //? if >=26.2 {
    /*Hud.class*/
    //?} else {
    Gui.class
    //?}
)
public abstract class CrosshairMixin {

    @Inject(
        method =
        //? if >=26 {
        /*"extractCrosshair"*/
        //?} else {
        "renderCrosshair"
        //?}
        , at = @At("HEAD"), cancellable = true)
    private void polycrosshair$replaceCrosshair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CrosshairRenderer.INSTANCE.render(graphics)) ci.cancel();
    }
}
