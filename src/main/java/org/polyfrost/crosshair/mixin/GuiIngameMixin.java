package org.polyfrost.crosshair.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import org.lwjgl.opengl.GL11;
import org.polyfrost.crosshair.CrosshairHUD;
import org.polyfrost.oneconfig.api.platform.v1.Platform;
import org.polyfrost.polyui.color.PolyColor;
import org.polyfrost.polyui.component.Drawable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiIngameForge.class)
public abstract class GuiIngameMixin {
    @Shadow
    private ScaledResolution res;

    @Redirect(method = "renderCrosshairs", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;bind(Lnet/minecraft/util/ResourceLocation;)V"), remap = false)
    private void bindTexture(GuiIngameForge it, ResourceLocation res) {
        if (CrosshairHUD.INSTANCE.getUseVanilla()) bind(res);
        else GlStateManager.bindTexture(CrosshairHUD.INSTANCE.getTexId());
    }

    @WrapWithCondition(method = "renderCrosshairs", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;tryBlendFuncSeparate(IIII)V", ordinal = 0))
    private boolean blendFunc(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
        return CrosshairHUD.INSTANCE.getUseVanillaBlending();
    }

    @Redirect(method = "renderCrosshairs", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;drawTexturedModalRect(IIIIII)V"))
    private void drawCrosshair(GuiIngameForge it, int xIn, int yIn, int u, int v, int wIn, int hIn) {
        Drawable wrapper = CrosshairHUD.INSTANCE.get();
        float scale = Platform.screen().pixelRatio() / res.getScaleFactor();
        double size = 15f * wrapper.getScaleX();
        double x = wrapper.getX() * scale;
        double y = wrapper.getY() * scale;
        double tex = CrosshairHUD.INSTANCE.getUseVanilla() ? 15d / 256d : 1d;

        PolyColor color = CrosshairHUD.INSTANCE.getColor();
        GL11.glColor4ub((byte) color.red(), (byte) color.green(), (byte) color.blue(), (byte) color.alpha());
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, y + size, 10d).tex(0d, tex).endVertex();
        worldRenderer.pos(x + size, y + size, 10d).tex(tex, tex).endVertex();
        worldRenderer.pos(x + size, y, 10d).tex(tex, 0d).endVertex();
        worldRenderer.pos(x, y, 10d).tex(0d, 0d).endVertex();
        tessellator.draw();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    @Shadow(remap = false)
    protected abstract void bind(ResourceLocation res);
}