package org.polyfrost.crosshair.render

import net.minecraft.client.Minecraft
//? if >=1.21 {
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.Items
//?} else {
/*import net.minecraft.client.gui.GuiElement
import net.minecraft.client.render.Window as GuiGraphicsExtractor
import net.minecraft.client.render.platform.GlStateManager
import net.minecraft.item.BowItem
import org.lwjgl.opengl.GL11
*///?}
//? if >=1.21.11 {
import net.minecraft.resources.Identifier as ResourceLocation
//?} else if >=1.21 {
/*import net.minecraft.resources.ResourceLocation
*///?} else {
/*import net.minecraft.resource.Identifier as ResourceLocation
*///?}
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines
//?}
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType
//?} else if >=1.21.4 {
/*import net.minecraft.client.renderer.RenderType
*///?}
import org.polyfrost.crosshair.Compat
import org.polyfrost.crosshair.config.CrosshairProfile
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.max
import kotlin.math.roundToInt

object CrosshairRenderer {

    private const val MAX_TEXELS_PER_CELL = 16

    private const val VANILLA_ART = 15

    private const val FULL_SWING_PITCH = 30f

    //? if >=1.21 {
    private val VANILLA_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair")
    //?}

    private val mc: Minecraft get() = Minecraft.getInstance()

    private class Bounds(val left: Int, val top: Int, val size: Int)

    private var indicator: Bounds? = null
    private var indicatorGap = 0
    private var indicatorGuiWidth = 0

    fun render(graphics: GuiGraphicsExtractor, vanillaVisible: Boolean): Boolean {
        indicator = null
        if (Compat.forcedOff) return false
        if (ModConfig.enabled) {
            //? if >=1.21 {
            val elytra = ModConfig.elytra
            if (elytra.enabled?.get() == true && !Compat.addonsElytra && mc.player?.getItemBySlot(EquipmentSlot.CHEST)?.`is`(Items.ELYTRA) == true) {
                indicator = draw(graphics, elytra, vanillaVisible)
                indicatorGap = elytra.attackIndicatorGap?.get()?.roundToInt() ?: 0
                indicatorGuiWidth = graphics.guiWidth()
            }
            //?}
            val drew = draw(graphics, ModConfig.profile(TargetClassifier.current()), vanillaVisible) != null
            if (!drew && indicator == null) return true
        } else {
            if (ModConfig.vanilla.enabled.get() != true) return false
            if (!drawVanilla(graphics, vanillaVisible)) return true
        }

        //? if <1.21 {
        /*GlStateManager.color4f(1f, 1f, 1f, 1f)
        mc.textureManager.bind(GuiElement.ICONS_LOCATION)
        *///?} else if <1.21.4 {
        /*if (vanillaVisible) {
            invertingBlend()
        } else {
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableBlend()
        }
        *///?}

        return true
    }

    @JvmOverloads
    fun attackIndicatorY(y: Int, x: Int = indicatorGuiWidth / 2 - 8): Int {
        val bounds = indicator ?: return y
        val overlaps = bounds.left < x + 16 && bounds.left + bounds.size > x && bounds.top < y + 16 && bounds.top + bounds.size > y
        return if (overlaps) max(y, bounds.top + bounds.size + indicatorGap) else y
    }

    private fun draw(graphics: GuiGraphicsExtractor, profile: CrosshairProfile, vanillaVisible: Boolean): Bounds? {
        if (!visible(profile.showInThirdPerson, profile.visibleInSpectator, vanillaVisible)) return null

        val cells = profile.grid.cells
        val factor = scaleFactor(profile)
        val texels = ceil(guiScale() * factor).toInt().coerceIn(1, MAX_TEXELS_PER_CELL)
        val texture = CrosshairTextures.get(profile, texels) ?: return null

        val drawn = (cells * factor).roundToInt().coerceAtLeast(1)
        val spread = spread(profile, factor)

        val perCell = drawn.toFloat() / cells
        val (offsetX, offsetY) = offset(profile)
        val shiftX = (offsetX * perCell).roundToInt()
        val shiftY = (offsetY * perCell).roundToInt()
        //? if >=1.21 {
        val left = (graphics.guiWidth() - drawn) / 2 + shiftX
        val top = (graphics.guiHeight() - drawn) / 2 + shiftY
        //?} else {
        /*val left = graphics.width / 2 - drawn / 2 + shiftX
        val top = graphics.height / 2 - drawn / 2 + shiftY
        GlStateManager.enableAlphaTest()
        *///?}

        texture.outline?.let { outline ->
            drawLayer(graphics, outline, left, top, drawn, cells, spread, profile.outlineColor.get()?.argb ?: -0x1000000, inverted = false)
        }

        val blending = profile.blendingEnabled.get() == true
        val tint = if (blending) profile.blendingColor.get()?.argb ?: -0x1 else -0x1
        drawLayer(graphics, texture.art, left, top, drawn, cells, spread, tint, inverted = blending)

        healthTint(profile)?.let { color ->
            drawLayer(graphics, texture.silhouette, left, top, drawn, cells, spread, color, inverted = false)
        }

        return Bounds(left, top, drawn)
    }

    private fun offset(profile: CrosshairProfile): Pair<Float, Float> {
        //? if >=1.21 {
        if (profile.orbitEnabled?.get() == true) {
            val pitch = Math.toRadians((-(mc.player?.xRot ?: 0f) * 90f / FULL_SWING_PITCH).coerceIn(-90f, 90f).toDouble())
            val radius = profile.orbitRadius?.get() ?: 0f
            return (cos(pitch) * radius).toFloat() to (sin(pitch) * radius).toFloat()
        }
        //?}
        return (profile.offsetX?.get() ?: 0f) to (profile.offsetY?.get() ?: 0f)
    }

    private fun drawVanilla(graphics: GuiGraphicsExtractor, vanillaVisible: Boolean): Boolean {
        val options = ModConfig.vanilla
        if (!visible(options.showInThirdPerson, options.visibleInSpectator, vanillaVisible)) return false

        val guiScale = guiScale()
        val texelSize = if (options.customScaleEnabled.get() == true) options.customScale.get()?.toDouble() ?: guiScale else guiScale
        val factor = (texelSize / guiScale).toFloat()
        //? if >=1.21 {
        val guiWidth = graphics.guiWidth()
        val guiHeight = graphics.guiHeight()
        val screenWidth = mc.window.width
        val screenHeight = mc.window.height
        val vanillaLeft = (guiWidth - VANILLA_ART) / 2
        val vanillaTop = (guiHeight - VANILLA_ART) / 2
        //?} else {
        /*val screenWidth = mc.width
        val screenHeight = mc.height
        val vanillaLeft = graphics.width / 2 - 7
        val vanillaTop = graphics.height / 2 - 7
        *///?}

        val left: Float
        val top: Float
        if (options.fixCentering.get() == true) {
            val art = VANILLA_ART * texelSize
            left = (floor((screenWidth - art) / 2) / guiScale).toFloat()
            top = (floor((screenHeight - art) / 2) / guiScale).toFloat()
        } else {
            val shift = VANILLA_ART / 2f * (1f - factor)
            left = vanillaLeft + shift
            top = vanillaTop + shift
        }

        val blending = options.blendingEnabled.get() == true
        val argb = (if (blending) options.blendingColor else options.color).get()?.argb ?: -0x1

        //? if >=1.21 {
        val pose = graphics.pose()
        pose.pushMatrix()
        //? if >=1.21.6 {
        pose.translate(left, top)
        pose.scale(factor, factor)
        //?} else {
        /*pose.translate(left, top, 0f)
        pose.scale(factor, factor, 1f)
        *///?}
        //? if >=1.21.6 {
        val pipeline = if (blending) RenderPipelines.CROSSHAIR else RenderPipelines.GUI_TEXTURED
        graphics.blitSprite(pipeline, VANILLA_SPRITE, 0, 0, VANILLA_ART, VANILLA_ART, argb)
        //?} else if >=1.21.4 {
        /*val type = if (blending) RenderType::crosshair else RenderType::guiTextured
        graphics.blitSprite(type, VANILLA_SPRITE, 0, 0, VANILLA_ART, VANILLA_ART, argb)
        *///?} else {
        /*RenderSystem.enableBlend()
        if (blending) invertingBlend() else RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(
            (argb ushr 16 and 0xFF) / 255f,
            (argb ushr 8 and 0xFF) / 255f,
            (argb and 0xFF) / 255f,
            (argb ushr 24 and 0xFF) / 255f,
        )
        graphics.blitSprite(VANILLA_SPRITE, 0, 0, VANILLA_ART, VANILLA_ART)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        *///?}
        pose.popMatrix()
        //?} else {
        /*GlStateManager.enableAlphaTest()
        GL11.glPushMatrix()
        GL11.glTranslatef(left, top, 0f)
        GL11.glScalef(factor, factor, 1f)
        blit(graphics, GuiElement.ICONS_LOCATION, 0, 0, 16, 16, 0f, 0f, 16, 16, 256, 256, argb, blending)
        GL11.glPopMatrix()
        *///?}
        return true
    }

    private fun visible(showInThirdPerson: Property<Boolean>, visibleInSpectator: Property<Boolean>, vanillaVisible: Boolean): Boolean {
        //? if >=1.21 {
        val firstPerson = mc.options.cameraType.isFirstPerson
        //?} else {
        /*val firstPerson = mc.options.perspective == 0
        *///?}
        if (!firstPerson && showInThirdPerson.get() != true) return false
        if (vanillaVisible) return true
        if (HudManager.isDebugScreenVisible) return false
        if (mc.player?.isSpectator == true) return visibleInSpectator.get() == true
        return !firstPerson
    }

    private fun guiScale(): Double =
        //? if >=1.21 {
        mc.window.guiScale.toDouble()
        //?} else {
        /*GuiGraphicsExtractor(mc).scale.toDouble()
        *///?}

    private fun scaleFactor(profile: CrosshairProfile): Float {
        if (profile.customScaleEnabled.get() != true) return 1f
        val scale = profile.customScale.get() ?: return 1f
        val current = guiScale()
        if (current <= 0.0) return 1f
        return (scale / current).toFloat()
    }

    private fun spread(profile: CrosshairProfile, factor: Float): Int {
        val player = mc.player ?: return 0
        var spread = 0f

        if (profile.bowEnabled?.get() == true) {
            //? if >=1.21 {
            val stack = if (player.isUsingItem) player.useItem else null
            val item = stack?.item
            val drawing = item is BowItem || item is CrossbowItem
            val ticks = player.ticksUsingItem
            //?} else {
            /*val drawing = player.itemInUse?.item is BowItem
            val ticks = player.remainingItemUseDuration
            *///?}
            if (drawing) {
                val charge = (ticks / 20f).coerceIn(0f, 1f)
                spread = max(spread, (1f - charge) * (profile.bowScale?.get() ?: 1f))
            }
        }

        //? if >=1.21 {
        if (profile.attackEnabled?.get() == true) {
            val recovered = player.getAttackStrengthScale(0f).coerceIn(0f, 1f)
            spread = max(spread, (1f - recovered) * (profile.attackScale?.get() ?: 1f))
        }
        //?}

        return (spread * factor).roundToInt()
    }

    private fun healthTint(profile: CrosshairProfile): Int? {
        if (profile.healthEnabled.get() != true) return null
        val player = mc.player ?: return null
        val threshold = profile.healthThreshold.get() ?: return null
        if (player.health > threshold) return null
        return profile.healthColor.get()?.argb
    }

    private fun drawLayer(
        graphics: GuiGraphicsExtractor,
        layer: CrosshairTexture.Layer,
        left: Int,
        top: Int,
        drawn: Int,
        cells: Int,
        spread: Int,
        argb: Int,
        inverted: Boolean,
    ) {
        if (spread <= 0) {
            blit(graphics, layer.location, left, top, drawn, drawn, 0f, 0f, layer.width, layer.height, layer.width, layer.height, argb, inverted)
            return
        }

        val span = (cells + 1) / 2
        val texels = layer.width / cells
        val pixels = drawn.toFloat() / cells
        val size = (span * pixels).roundToInt()

        for (qy in 0..1) {
            for (qx in 0..1) {
                val cx = qx * (cells - span)
                val cy = qy * (cells - span)
                val dx = left + (cx * pixels).roundToInt() + if (qx == 0) -spread else spread
                val dy = top + (cy * pixels).roundToInt() + if (qy == 0) -spread else spread
                blit(
                    graphics, layer.location, dx, dy, size, size,
                    (cx * texels).toFloat(), (cy * texels).toFloat(), span * texels, span * texels,
                    layer.width, layer.height, argb, inverted,
                )
            }
        }
    }

    //? if >=1.21 && <1.21.4 {
    /*internal fun invertingBlend() {
        RenderSystem.blendFuncSeparate(
            com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
            com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO,
        )
    }
    *///?}

    @Suppress("LongParameterList")
    private fun blit(
        graphics: GuiGraphicsExtractor,
        location: ResourceLocation,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        textureWidth: Int,
        textureHeight: Int,
        argb: Int,
        inverted: Boolean,
    ) {
        //? if <1.21 {
        /*mc.textureManager.bind(location)
        if (inverted) {
            GlStateManager.blendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO)
        } else {
            GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        }
        GlStateManager.color4f(
            (argb ushr 16 and 0xFF) / 255f,
            (argb ushr 8 and 0xFF) / 255f,
            (argb and 0xFF) / 255f,
            (argb ushr 24 and 0xFF) / 255f,
        )
        GuiElement.drawTexture(x, y, u, v, uWidth, vHeight, width, height, textureWidth.toFloat(), textureHeight.toFloat())
        *///?} else if <1.21.4 {
        /*RenderSystem.enableBlend()
        if (inverted) invertingBlend() else RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(
            (argb ushr 16 and 0xFF) / 255f,
            (argb ushr 8 and 0xFF) / 255f,
            (argb and 0xFF) / 255f,
            (argb ushr 24 and 0xFF) / 255f,
        )
        graphics.blit(location, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        *///?} else if <1.21.6 {
        /*val type = if (inverted) RenderType::crosshair else RenderType::guiTextured
        graphics.blit(type, location, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, argb)
        *///?} else {
        val pipeline = if (inverted) RenderPipelines.CROSSHAIR else RenderPipelines.GUI_TEXTURED
        graphics.blit(pipeline, location, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, argb)
        //?}
    }
}
