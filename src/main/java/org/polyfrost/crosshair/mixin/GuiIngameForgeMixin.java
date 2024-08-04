package org.polyfrost.crosshair.mixin;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.crosshair.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameForge.class)
public class GuiIngameForgeMixin {

    @Inject(method = "renderCrosshairs", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelVanillaCrosshair(int width, int height, CallbackInfo ci) {
        if (ModConfig.INSTANCE.enabled) {
            ci.cancel();
            GlStateManager.enableAlpha();
        }
    }
}