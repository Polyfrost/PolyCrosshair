package org.polyfrost.crosshair.mixin;

import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiIngame.class)
public class GuiIngameMixin {
    @Inject(method = "showCrosshair", at = @At("HEAD"), cancellable = true)
    private void check(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}