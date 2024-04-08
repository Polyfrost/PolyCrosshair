package org.polyfrost.crosshair.render

import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.libs.universal.UResolution
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.mixin.GuiIngameAccessor
import java.awt.Color

object CrosshairRenderer {
    private val texture = DynamicTexture(15, 15)
    private val textureLocation: ResourceLocation =
        mc.textureManager.getDynamicTextureLocation("polycrosshair", texture)
    private val whiteTexture = DynamicTexture(15, 15)
    private val whiteTextureLocation: ResourceLocation =
        mc.textureManager.getDynamicTextureLocation("polycrosshair", whiteTexture)

    fun updateTexture() {
        for (i in 0 until 225) {
            whiteTexture.textureData[i] = 0x00000000
            texture.textureData[i] = 0x00000000
            ModConfig.crosshair[i] ?: continue
            whiteTexture.textureData[i] = Color(-1).rgb
            texture.textureData[i] = ModConfig.crosshair[i]!!.color
        }
        texture.updateDynamicTexture()
        whiteTexture.updateDynamicTexture()
    }

    @SubscribeEvent
    fun onRenderCrosshair(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return
        if (!ModConfig.enabled) return
        if ((mc.ingameGUI as? GuiIngameAccessor)?.shouldShowCrosshair() == false) return

        event.isCanceled = true
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        if (ModConfig.invertColor) {
            GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0)
        }
        GlStateManager.enableAlpha()
        val c = getColor()
        GL11.glColor4f(c.red / 255f, c.green / 255f, c.blue / 255f, c.alpha / 255f)

        mc.textureManager.bindTexture(if (c.toJavaColor() == Color(-1)) textureLocation else whiteTextureLocation)

        val scale = UResolution.scaleFactor.toFloat()
        GlStateManager.translate(ModConfig.offsetX / scale, ModConfig.offsetY / scale, 0f)
        GlStateManager.translate(UResolution.scaledWidth / 2f, UResolution.scaledHeight / 2f, 0f)
        GlStateManager.rotate(ModConfig.rotation.toFloat(), 0f, 0f, 1f)
        GlStateManager.scale(ModConfig.scale / 100f, ModConfig.scale / 100f, 1f)
        GlStateManager.translate(-7.5f, -7.5f, 0f)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, 15, 15, 15f, 15f)
        if (ModConfig.invertColor) {
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        }
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    val WHITE = OneColor(-1)

    fun getColor(): OneColor {
        with(ModConfig) {
            val entity = mc.pointedEntity ?: return WHITE
            if (dynamicColor) {
                if (hostile && entity is IMob) return hostileColor
                if (passive && (entity is EntityAnimal || entity is EntityAmbientCreature || entity is EntityWaterMob)) return passiveColor
                if (player && entity is EntityPlayer) return playerColor
            }
        }
        return WHITE
    }
}