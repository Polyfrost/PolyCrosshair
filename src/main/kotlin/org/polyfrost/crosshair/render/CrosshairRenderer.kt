@file:Suppress("UnstableAPIUsage")
package org.polyfrost.crosshair.render

import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.images.OneImage
import cc.polyfrost.oneconfig.libs.universal.UResolution
import cc.polyfrost.oneconfig.utils.dsl.mc
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.renderer.GlStateManager as GL
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.passive.EntityAmbientCreature
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.passive.EntityWaterMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.mixin.GuiIngameAccessor
import java.awt.image.BufferedImage
import kotlin.math.ceil

object CrosshairRenderer {
    private var drawingImage = BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)
    private var texture = DynamicTexture(15, 15)
    private var textureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", texture)
    private var whiteTexture = DynamicTexture(15, 15)
    private var whiteTextureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", whiteTexture)
    private var vanilla = DynamicTexture(15, 15)
    private var vanillaLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", vanilla)

    fun updateTexture(image: OneImage) {
        drawingImage = image.image
        texture = DynamicTexture(drawingImage)
        textureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", texture)
        whiteTexture = DynamicTexture(drawingImage.width, drawingImage.height)
        for (posY in 0..<drawingImage.height) {
            for (posX in 0..<drawingImage.width) {
                val color = drawingImage.getRGB(posX, posY)
                if (color shr 24 == 0) continue
                whiteTexture.textureData[posX + posY * drawingImage.width] = -1
            }
        }
        whiteTexture.updateDynamicTexture()
        whiteTextureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", whiteTexture)
    }

    fun updateVanilla() {
        val icon = TextureUtil.readBufferedImage(mc.resourceManager.getResource(Gui.icons).inputStream)
        vanilla = DynamicTexture(15, 15)
        for (y in 0 until  15) {
            for (x in 0 until 15) {
                if (icon.getRGB(x, y) == -1) {
                    vanilla.textureData[x + y * 15] = -1
                }
            }
        }
        vanilla.updateDynamicTexture()
        vanillaLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", vanilla)
    }

    @SubscribeEvent
    fun cancel(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS || !ModConfig.enabled) return
        GL.enableAlpha()
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onPackSwitch(event: TextureStitchEvent) {
        updateVanilla()
    }

    fun drawCrosshair(entityRenderer: EntityRenderer) {
        if (!ModConfig.enabled) return
        if ((mc.ingameGUI as? GuiIngameAccessor)?.shouldShowCrosshair() == false) return

        entityRenderer.setupOverlayRendering()
        GL.pushMatrix()
        GL.tryBlendFuncSeparate(770, 771, 1, 0)
        GL.enableBlend()
        val renderConfig = ModConfig.renderConfig
        if (renderConfig.invertColor) {
            GL.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0)
        }
        GL.enableAlpha()

        GL11.glColor4f(1f, 1f, 1f, 1f)

        (if (ModConfig.mode) textureLocation else vanillaLocation).let { mc.textureManager.bindTexture(it) }
        val mcScale = UResolution.scaleFactor.toFloat()
        GL.scale(1 / mcScale, 1 / mcScale, 1f)
        val crosshair = ModConfig.newCurrentCrosshair
        GL.translate(crosshair.offsetX.toFloat(), crosshair.offsetY.toFloat(), 0f)
        GL.translate((UResolution.windowWidth / 2).toFloat(), (UResolution.windowHeight / 2).toFloat(), 0f)
        GL.rotate(crosshair.rotation.toFloat(), 0f, 0f, 1f)
        val scale = ModConfig.newCurrentCrosshair.scale / 100f
        val textureSize = if (ModConfig.mode) drawingImage.width else 15
        val size = ceil(textureSize * mcScale * scale).toInt()
        val translation = if (crosshair.centered) (-size / 2).toFloat() else (-(size - mcScale) / 2).toInt().toFloat()
        GL.translate(translation, translation, 0f)
        Gui.drawScaledCustomSizeModalRect(0, 0, 0f, 0f, textureSize, textureSize, size, size, textureSize.toFloat(), textureSize.toFloat())
        val c = getColor()
        if (c.rgb != -1) {
            if (ModConfig.mode) mc.textureManager.bindTexture(whiteTextureLocation)
            GL11.glColor4f(c.red / 255f, c.green / 255f, c.blue / 255f, renderConfig.dynamicOpacity / 100f)
            Gui.drawScaledCustomSizeModalRect(0, 0, 0f, 0f, textureSize, textureSize, size, size, textureSize.toFloat(), textureSize.toFloat())
        }
        if (renderConfig.invertColor) {
            GL.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        }
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GL.disableBlend()
        GL.popMatrix()
    }

    val WHITE = OneColor(-1)

    private fun getColor(): OneColor {
        with(ModConfig.renderConfig) {
            val entity = mc.objectMouseOver?.entityHit ?: return WHITE
            if (entity.isInvisible) return WHITE
            if (dynamicColor) {
                if (hostile && entity is IMob) return hostileColor
                if (passive && (entity is EntityVillager || entity is EntityAnimal || entity is EntityAmbientCreature || entity is EntityWaterMob)) return passiveColor
                if (player && entity is EntityPlayer) return playerColor
            }
        }
        return WHITE
    }

}