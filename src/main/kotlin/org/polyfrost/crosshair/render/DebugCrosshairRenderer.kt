package org.polyfrost.crosshair.render

import net.minecraft.client.Minecraft
//? if >=1.21 {
import net.minecraft.client.gui.GuiGraphicsExtractor
//?} else {
/*import net.minecraft.client.gui.GuiElement
import net.minecraft.client.render.Window as GuiGraphicsExtractor
import net.minecraft.client.render.platform.GlStateManager
*///?}
//? if >=1.21.10 {
import net.minecraft.client.gui.components.debug.DebugScreenEntries
//?}
import org.polyfrost.crosshair.Compat
import org.polyfrost.crosshair.config.DebugAxis
import org.polyfrost.crosshair.config.DebugDisplayMode
import org.polyfrost.crosshair.config.ModConfig
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

object DebugCrosshairRenderer {

    private const val VANILLA_SCALE = 0.01f

    private const val VANILLA_HUD_FOV = 70.0

    private const val VANILLA_LENGTH = 10f

    private val mc: Minecraft get() = Minecraft.getInstance()

    private class ProjectedAxis(val axis: DebugAxis, val x: Float, val y: Float, val depth: Float)

    fun customized(): Boolean = !Compat.forcedOff && ModConfig.debugCrosshair.enabled.get() == true

    fun hidesCrosshair(): Boolean = visible() && ModConfig.debugCrosshair.hideCrosshair.get() == true

    fun render(graphics: GuiGraphicsExtractor, partialTick: Float) {
        if (!visible()) return
        //? if >=1.21 {
        val camera = mc.cameraEntity ?: return
        var yaw = Math.toRadians(camera.getViewYRot(partialTick).toDouble())
        var pitch = Math.toRadians(camera.getViewXRot(partialTick).toDouble())
        val windowWidth = mc.window.width
        val windowHeight = mc.window.height
        //?} else {
        /*if (mc.options.hideGui) return
        val camera = mc.camera ?: return
        var yaw = Math.toRadians((camera.lastYaw + (camera.yaw - camera.lastYaw) * partialTick).toDouble())
        var pitch = Math.toRadians((camera.lastPitch + (camera.pitch - camera.lastPitch) * partialTick).toDouble())
        val windowWidth = mc.width
        val windowHeight = mc.height
        *///?}
        if (mirrored()) {
            yaw += Math.PI
            pitch = -pitch
        }

        val sinYaw = sin(yaw).toFloat()
        val cosYaw = cos(yaw).toFloat()
        val sinPitch = sin(pitch).toFloat()
        val cosPitch = cos(pitch).toFloat()
        val options = ModConfig.debugCrosshair
        val axes = listOf(
            ProjectedAxis(options.xAxis, -cosYaw, sinYaw * sinPitch, sinYaw * cosPitch),
            ProjectedAxis(options.yAxis, 0f, -cosPitch, sinPitch),
            ProjectedAxis(options.zAxis, -sinYaw, -cosYaw * sinPitch, -cosYaw * cosPitch),
        ).filter { it.axis.enabled.get() == true }.sortedBy { it.depth }

        val guiScale = CrosshairRenderer.guiScale().toFloat()
        val pixelSize = 1f / guiScale
        val centerX = windowWidth * pixelSize / 2f
        val centerY = windowHeight * pixelSize / 2f
        val outlineWidth = options.outlineWidth.get()?.roundToInt() ?: 1
        val axisWidth = options.axisWidth.get()?.roundToInt() ?: 1
        for (projected in axes) {
            val reach = reach(projected, guiScale, windowHeight)
            if (projected.axis.outline.get() == true) {
                val color = projected.axis.outlineColor.get()?.argb ?: -0x1000000
                drawAxis(graphics, projected, reach, centerX, centerY, pixelSize, outlineWidth, color)
            }
            val color = projected.axis.color.get()?.argb ?: -0x1
            drawAxis(graphics, projected, reach, centerX, centerY, pixelSize, axisWidth, color)
        }
        //? if <1.21 {
        /*GlStateManager.enableBlend()
        GlStateManager.color4f(1f, 1f, 1f, 1f)
        *///?}
    }

    private fun reach(projected: ProjectedAxis, guiScale: Float, windowHeight: Int): Float {
        val length = projected.axis.length.get() ?: 0f
        if (ModConfig.debugCrosshair.perspective.get() != true) return length
        val size = length / VANILLA_LENGTH * VANILLA_SCALE * guiScale
        val pixelsPerUnit = windowHeight / (2.0 * tan(Math.toRadians(VANILLA_HUD_FOV / 2.0))).toFloat()
        return size * pixelsPerUnit / (1f - size * projected.depth)
    }

    private fun drawAxis(
        graphics: GuiGraphicsExtractor,
        projected: ProjectedAxis,
        reach: Float,
        centerX: Float,
        centerY: Float,
        pixelSize: Float,
        width: Int,
        argb: Int,
    ) {
        val length = (reach * hypot(projected.x, projected.y)).roundToInt()
        if (length <= 0) return
        val degrees = Math.toDegrees(atan2(projected.y, projected.x).toDouble()).toFloat()
        CrosshairRenderer.transformed(graphics, centerX, centerY, pixelSize, degrees, 0f) {
            //? if >=1.21 {
            graphics.fill(0, -width / 2, length, width - width / 2, argb)
            //?} else {
            /*GuiElement.fill(0, -width / 2, length, width - width / 2, argb)
            *///?}
        }
    }

    private fun mirrored(): Boolean =
        //? if >=1.21 {
        mc.options.cameraType.isMirrored
        //?} else {
        /*mc.options.perspective == 2
        *///?}

    private fun visible(): Boolean {
        if (!customized()) return false
        if (!CrosshairRenderer.firstPerson() && ModConfig.debugCrosshair.showInThirdPerson.get() != true) return false
        return when (DebugDisplayMode.of(ModConfig.debugCrosshair.displayMode.get() ?: 0)) {
            DebugDisplayMode.NEVER -> false
            DebugDisplayMode.OVERLAY -> debugScreenAllows()
            DebugDisplayMode.ALWAYS -> true
        }
    }

    private fun debugScreenAllows(): Boolean =
        //? if >=1.21.10 {
        mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR)
        //?} else if >=1.21 {
        /*mc.debugOverlay.showDebugScreen() && mc.player?.isReducedDebugInfo == false && !mc.options.reducedDebugInfo().get()
        *///?} else {
        /*mc.options.debugEnabled && mc.player?.hasReducedDebugInfo() == false && !mc.options.reducedDebugInfo
        *///?}
}
