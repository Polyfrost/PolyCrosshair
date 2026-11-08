package org.polyfrost.crosshair.mixin;

//? if >=1.21.8 && <1.21.10 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.Gui;
*///?} else if >=1.21.10 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.GameRenderer;
//?} else if >=1.21 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.Gui;
*///?} else {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.GameRenderer;
*///?}
import org.polyfrost.crosshair.render.DebugCrosshairRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=1.21.10 || <1.21 {
@Mixin(GameRenderer.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class DebugCrosshairMixin {

    //? if >=26.3 {
    @ModifyExpressionValue(method = "render3dHud", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;render3dCrosshair:Z"))
    //?} else if >=26.2 {
    /*@ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;render3dCrosshair:Z"))
    *///?} else if >=1.21.11 {
    /*@ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;isCurrentlyEnabled(Lnet/minecraft/resources/Identifier;)Z"))
    *///?} else if >=1.21.10 {
    /*@ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;isCurrentlyEnabled(Lnet/minecraft/resources/ResourceLocation;)Z"))
    *///?} else if >=1.21.8 {
    /*@ModifyReturnValue(method = "shouldRenderDebugCrosshair", at = @At("RETURN"))
    *///?} else if >=1.21 {
    /*@ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showDebugScreen()Z"))
    *///?} else {
    /*@ModifyExpressionValue(method = "renderAxisIndicators", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;debugEnabled:Z"))
    *///?}
    private boolean polycrosshair$hideVanillaDebugCrosshair(boolean shown) {
        return shown && !DebugCrosshairRenderer.INSTANCE.customized();
    }
}
