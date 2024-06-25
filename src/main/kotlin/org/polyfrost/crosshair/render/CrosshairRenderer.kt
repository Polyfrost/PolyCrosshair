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
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.passive.EntityAmbientCreature
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.passive.EntityWaterMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.mixin.GuiIngameAccessor

object CrosshairRenderer {
    private var texture = DynamicTexture(15, 15)
    private var textureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", texture)
    private var whiteTexture = DynamicTexture(15, 15)
    private var whiteTextureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", whiteTexture)

    fun updateTexture(image: OneImage) {
        val bufferedImage = image.image
        texture = DynamicTexture(bufferedImage)
        textureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", texture)
        whiteTexture = DynamicTexture(bufferedImage.width, bufferedImage.height)
        for (posY in 0..<bufferedImage.height) {
            for (posX in 0..<bufferedImage.width) {
                val color = bufferedImage.getRGB(posX, posY)
                if (color shr 24 == 0) continue
                whiteTexture.textureData[posX + posY * bufferedImage.width] = -1
            }
        }
        whiteTexture.updateDynamicTexture()
        whiteTextureLocation = mc.textureManager.getDynamicTextureLocation("polycrosshair", whiteTexture)
    }

    @SubscribeEvent
    fun cancel(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS || !ModConfig.enabled) return
        GL.enableAlpha()
        event.isCanceled = true
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

        (if (ModConfig.mode) textureLocation else Gui.icons).let { mc.textureManager.bindTexture(it) }
        val mcScale = UResolution.scaleFactor.toFloat()
        GL.scale(1 / mcScale, 1 / mcScale, 1f)
        val crosshair = ModConfig.newCurrentCrosshair
        GL.translate(crosshair.offsetX / mcScale, crosshair.offsetY / mcScale, 0f)
        GL.scale(mcScale, mcScale, 1f)
        GL.translate((UResolution.scaledWidth / 2).toDouble(), (UResolution.scaledHeight / 2).toDouble(), 0.0)
        GL.rotate(crosshair.rotation.toFloat(), 0f, 0f, 1f)
        val size = if (ModConfig.mode) ModConfig.canvaSize else 15
        val textureSize = if (ModConfig.mode) size.toFloat() else 256f
        val scale = (crosshair.scale + crosshair.scale % 2) / 100f
        GL.scale(scale, scale, 1f)
        GL.translate(-size / 2f, -size / 2f, 0f)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, size, size, textureSize, textureSize)
        val c = getColor()
        if (c.rgb != -1) {
            if (ModConfig.mode) mc.textureManager.bindTexture(whiteTextureLocation)
            GL11.glColor4f(c.red / 255f, c.green / 255f, c.blue / 255f, renderConfig.dynamicOpacity / 100f)
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, size, size, textureSize, textureSize)
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