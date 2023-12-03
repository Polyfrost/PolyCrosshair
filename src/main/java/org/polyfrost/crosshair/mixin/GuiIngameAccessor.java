package org.polyfrost.crosshair.mixin;

import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiIngame.class)
public interface GuiIngameAccessor {
    @Invoker("showCrosshair")
    boolean shouldShowCrosshair();
}
