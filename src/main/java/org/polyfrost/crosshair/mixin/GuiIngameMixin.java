package org.polyfrost.crosshair.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import org.polyfrost.crosshair.CrosshairHUD;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiIngameForge.class)
public abstract class GuiIngameMixin {
    @Redirect(method = "renderCrosshairs", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;bind(Lnet/minecraft/util/ResourceLocation;)V"), remap = false)
    private void bindTexture(GuiIngameForge it, ResourceLocation res) {
        if (CrosshairHUD.INSTANCE.getUseVanilla()) bind(res);
        else GlStateManager.bindTexture(CrosshairHUD.INSTANCE.getId());
    }

    @Redirect(method = "renderCrosshairs", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void drawCrosshair(GuiIngameForge it, int x, int y, int u, int v, int w, int h) {
        final float tex = CrosshairHUD.INSTANCE.getUseVanilla() ? 256f : CrosshairHUD.INSTANCE.getWidth();
        // #optimize-fold-zeroes
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, w, h, tex, tex);
    }

    @Shadow(remap = false)
    protected abstract void bind(ResourceLocation res);
}