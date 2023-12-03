package org.polyfrost.crosshair.render

import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.mixin.GuiIngameAccessor

object CrosshairRenderer {
    private val texture = DynamicTexture(15, 15)
    private val textureLocation: ResourceLocation =
        mc.textureManager.getDynamicTextureLocation("polycrosshair", texture)

    fun updateTexture() {
        val profile = ModConfig.profiles.selectedProfile ?: return
        for (i in 0 until 225) {
            texture.textureData[i] = if (profile.image.get(i)) profile.mainColor.rgb else 0x00000000
        }
        texture.updateDynamicTexture()
    }

    @SubscribeEvent
    fun onRenderCrosshair(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return
        if (!ModConfig.enabled) return
        if ((mc.ingameGUI as? GuiIngameAccessor)?.shouldShowCrosshair() == false) return

        val profile = ModConfig.profiles.selectedProfile ?: return
        event.isCanceled = true


        GlStateManager.enableBlend()
        if (profile.invertColor) {
            GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0)
        }
        GlStateManager.enableAlpha()
        mc.textureManager.bindTexture(textureLocation)
        Gui.drawModalRectWithCustomSizedTexture(
            (event.resolution.scaledWidth - 15) / 2,
            (event.resolution.scaledHeight - 15) / 2,
            0f, 0f, 15, 15, 15f, 15f
        )
        if (profile.invertColor) {
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        }
        GlStateManager.disableBlend()
    }
}