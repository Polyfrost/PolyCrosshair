@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.render

import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.images.OneImage
import cc.polyfrost.oneconfig.libs.universal.UResolution
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.mixin.GuiIngameAccessor
import java.awt.Color

object CrosshairRenderer {
    private var texture = DynamicTexture(15, 15)
    private var textureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", texture)
    private var whiteTexture = DynamicTexture(15, 15)
    private var whiteTextureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", whiteTexture)

    fun updateTexture(image: OneImage) {
        texture = DynamicTexture(image.image)
        textureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", texture)
        val size = ModConfig.canvaSize
        whiteTexture = DynamicTexture(size, size)
        for (posY in 0..<size) {
            for (posX in 0..<size) {
                if (image.image.getRGB(posX, posY) shr 24 == 0) continue
                whiteTexture.textureData[posX + posY * size] = -1
            }
        }
        whiteTexture.updateDynamicTexture()
        whiteTextureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", whiteTexture)
    }

    @SubscribeEvent
    fun cancel(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return
        event.isCanceled = true
    }

    fun drawCrosshair(entityRenderer: EntityRenderer) {
        if (!ModConfig.enabled) return
        if ((mc.ingameGUI as? GuiIngameAccessor)?.shouldShowCrosshair() == false) return

        entityRenderer.setupOverlayRendering()
        GlStateManager.pushMatrix()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.enableBlend()
        if (ModConfig.invertColor) {
            GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0)
        }
        GlStateManager.enableAlpha()

        GL11.glColor4f(1f, 1f, 1f, 1f)

        mc.textureManager.bindTexture(textureLocation)
        GlStateManager.translate(ModConfig.offsetX.toFloat(), ModConfig.offsetY.toFloat(), 0f)
        GlStateManager.translate(UResolution.scaledWidth / 2f, UResolution.scaledHeight / 2f, 0f)
        GlStateManager.rotate(ModConfig.rotation.toFloat(), 0f, 0f, 1f)
        GlStateManager.scale(ModConfig.scale / 100f, ModConfig.scale / 100f, 1f)
        val size = ModConfig.canvaSize
        GlStateManager.translate(-size / 2f, -size / 2f, 0f)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, size, size, size.toFloat(), size.toFloat())
        val c = getColor()
        if (c.toJavaColor() != Color(-1)) {
            mc.textureManager.bindTexture(whiteTextureLocation)
            GL11.glColor4f(c.red / 255f, c.green / 255f, c.blue / 255f, ModConfig.dynamicOpacity / 100f)
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, size, size, size.toFloat(), size.toFloat())
        }
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