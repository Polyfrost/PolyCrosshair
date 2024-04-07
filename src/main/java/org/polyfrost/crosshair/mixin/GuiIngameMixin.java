package org.polyfrost.crosshair.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import org.polyfrost.crosshair.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiIngame.class)
public class GuiIngameMixin {
    @Inject(method = "showCrosshair", at = @At("HEAD"), cancellable = true)
    private void check(CallbackInfoReturnable<Boolean> cir) {
        ModConfig cfg = ModConfig.INSTANCE;
        Minecraft mc = Minecraft.getMinecraft();
        if (!cfg.getShowInThirdPerson() && mc.gameSettings.thirdPersonView != 0) cir.setReturnValue(false);
        if (cfg.getShowInSpectator() && mc.playerController.isSpectator()) cir.setReturnValue(true);
        if (cfg.getShowInDebug() && mc.gameSettings.showDebugInfo) cir.setReturnValue(true);
    }
}