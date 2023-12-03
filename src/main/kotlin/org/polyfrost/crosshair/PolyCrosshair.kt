package org.polyfrost.crosshair

import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.mixin.GuiIngameAccessor
import net.minecraft.client.renderer.GlStateManager as GL

@Mod(modid = PolyCrosshair.MODID, name = PolyCrosshair.NAME, version = PolyCrosshair.VERSION)
class PolyCrosshair {
    companion object {
        const val MODID = "@ID@"
        const val NAME = "@NAME@"
        const val VERSION = "@VER@"

        val texture by lazy {
            DynamicTexture(15, 15)
        }
        val resourceLocation by lazy {
            mc.textureManager.getDynamicTextureLocation("polycrosshair", texture)
        }


        fun updateTexture() {
            val profile = ModConfig.profiles.selectedProfile ?: return
            for (i in 0 until 225) {
                texture.textureData[i] = if (profile.image.get(i)) profile.mainColor.rgb else 0x00000000
            }
            texture.updateDynamicTexture()
        } // test now? ok
    }

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        ModConfig
        updateTexture()
    }

    @SubscribeEvent
    fun onRenderCrosshair(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return
        if (!ModConfig.enabled) return
        if ((mc.ingameGUI as? GuiIngameAccessor)?.shouldShowCrosshair() == false) return

        val profile = ModConfig.profiles.selectedProfile ?: return
        event.isCanceled = true


        GL.enableBlend()
        if (profile.invertColor) {
            GL.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0)
        }
        GL.enableAlpha()
        with(profile.mainColor) {
            GL.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
        }

//        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1)
//
//        GL11.glRasterPos2f(event.resolution.scaledWidth / 2f - 7.5f, event.resolution.scaledHeight / 2f - 7.5f)
//        GL11.glDrawPixels(15, 15, GL11.GL_COLOR_INDEX, GL11.GL_BITMAP, ByteBuffer.wrap(profile.image.toByteArray()))
//  test this first?\

        // draw
        mc.textureManager.bindTexture(resourceLocation)
        Gui.drawModalRectWithCustomSizedTexture(
            (event.resolution.scaledWidth - 15) / 2,
            (event.resolution.scaledHeight - 15) / 2,
            0f,
            0f,
            15,
            15,
            15f,
            15f
        )
//        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4)

        if (profile.invertColor) {
            GL.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        }
        GL.disableBlend()
    }
}