package org.polyfrost.crosshair.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import org.polyfrost.crosshair.CrosshairHUD;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiIngame.class)
public abstract class GuiIngameShowCrosshairMixin {
    @Inject(method = "showCrosshair", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private void showWhenDebug(CallbackInfoReturnable<Boolean> cir) {
        if (CrosshairHUD.INSTANCE.getShowInDebug()) cir.setReturnValue(true);
    }

    @Inject(method = "showCrosshair", at = @At("HEAD"), cancellable = true)
    private void showWhenPerspective(CallbackInfoReturnable<Boolean> cir) {
        if (!CrosshairHUD.INSTANCE.getShowInThirdPerson() && Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "showCrosshair", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;pointedEntity:Lnet/minecraft/entity/Entity;"), cancellable = true)
    private void showWhenSpectator(CallbackInfoReturnable<Boolean> cir) {
        if (CrosshairHUD.INSTANCE.getShowInSpectator()) cir.setReturnValue(true);
    }
}
