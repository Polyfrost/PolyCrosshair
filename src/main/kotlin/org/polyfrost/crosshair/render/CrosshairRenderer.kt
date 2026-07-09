package org.polyfrost.crosshair.render

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.texture.DynamicTexture
//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier as ResourceLocation
*///?} else {
import net.minecraft.resources.ResourceLocation
//?}
//? if >=1.21.5
//import net.minecraft.client.renderer.RenderPipelines
//? if >= 1.21.11 {
//import net.minecraft.client.renderer.rendertype.RenderType
//?} else if >=1.21.4 {
import net.minecraft.client.renderer.RenderType
//?}
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.EntityHitResult
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.utils.toBufferedImage
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO
import kotlin.math.ceil

object CrosshairRenderer {

    private val mc: Minecraft get() = Minecraft.getInstance()

    private const val TEX_SIZE = 16

    private var lastImg: String? = null
    private var texture: DynamicTexture? = null
    private var textureLoc: ResourceLocation? = null
    private var maskTexture: DynamicTexture? = null
    private var maskLoc: ResourceLocation? = null
    private var texW = TEX_SIZE
    private var texH = TEX_SIZE

    private val WHITE = PolyColor(-1)

    fun render(graphics: GuiGraphics): Boolean {
        if (!ModConfig.enabled) return false
        if (!shouldShow()) return true

        ensureTexture()
        val loc = textureLoc ?: return true

        val scale = ModConfig.scale / 100f
        val autoSize = ModConfig.canvas
        val drawn = ceil((if (ModConfig.custom) autoSize else TEX_SIZE).toFloat() * scale).toInt()
        val translation = ceil((if (ModConfig.custom && ModConfig.centered) -autoSize / 2f else -7f) * scale).toFloat()

        val cx = graphics.guiWidth() / 2f + ModConfig.offsetX
        val cy = graphics.guiHeight() / 2f + ModConfig.offsetY

        val pose = graphics.pose()
        //? if <1.21.6 {
        pose.pushPose()
        pose.translate(cx, cy, 0f)
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(ModConfig.rotation))
        pose.translate(translation, translation, 0f)
        //?} else {
        /*pose.pushPose()
        pose.translate(cx, cy)
        pose.rotate(Math.toRadians(ModConfig.rotation.toDouble()).toFloat())
        pose.translate(translation, translation)
        *///?}

        blit(graphics, loc, drawn, drawn, -1)
        val color = dynamicColor()
        if (color.argb != -1) {
            val tint = (color.argb and 0x00FFFFFF) or ((ModConfig.dynamicOpacity / 100f * 255f).toInt() shl 24)
            blit(graphics, if (ModConfig.custom) (maskLoc ?: loc) else loc, drawn, drawn, tint)
        }

        //? if <1.21.6 {
        pose.popPose()
        //?} else {
        /*pose.popPose();*///?}
        return true
    }

    private fun blit(graphics: GuiGraphics, loc: ResourceLocation, w: Int, h: Int, argb: Int) {
        //? if <1.21.4 {
        /*if (argb != -1) RenderSystem.setShaderColor(
            (argb ushr 16 and 0xFF) / 255f, (argb ushr 8 and 0xFF) / 255f, (argb and 0xFF) / 255f, (argb ushr 24 and 0xFF) / 255f
        )
        graphics.blit(loc, 0, 0, 0f, 0f, w, h, texW, texH)
        if (argb != -1) RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        *///?} else if <1.21.6 {
        val type = if (ModConfig.invertColor) RenderType::crosshair else RenderType::guiTextured
        graphics.blit(type, loc, 0, 0, 0f, 0f, w, h, texW, texH, argb)
        //?} else {
        /*val pipeline = if (ModConfig.invertColor) RenderPipelines.CROSSHAIR else RenderPipelines.GUI_TEXTURED
        graphics.blit(pipeline, loc, 0, 0, 0f, 0f, w, h, texW, texH, argb)
        *///?}
    }

    private fun ensureTexture() {
        val img = ModConfig.data.current.img
        if (img == lastImg && texture != null) return
        lastImg = img
        val buf: BufferedImage = toBufferedImage(img) ?: return
        texW = buf.width
        texH = buf.height
        texture?.close()
        maskTexture?.close()
        val (tex, tLoc) = upload("polycrosshair_tex", Base64.getDecoder().decode(img))
        val (mask, mLoc) = upload("polycrosshair_mask", maskPng(buf))
        texture = tex; textureLoc = tLoc
        maskTexture = mask; maskLoc = mLoc
    }

    private fun upload(id: String, bytes: ByteArray): Pair<DynamicTexture, ResourceLocation> {
        val native = NativeImage.read(ByteArrayInputStream(bytes))
        val tex = DynamicTexture(
            //? if >=1.21.5 {
            /*{ id },
            *///?}
            native
        )
        val loc = ResourceLocation.fromNamespaceAndPath("polycrosshair", id)
        mc.textureManager.register(loc, tex)
        return tex to loc
    }

    private fun maskPng(buf: BufferedImage): ByteArray {
        val mask = BufferedImage(buf.width, buf.height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until buf.height) for (x in 0 until buf.width) {
            if (buf.getRGB(x, y) ushr 24 != 0) mask.setRGB(x, y, -1)
        }
        val out = ByteArrayOutputStream()
        ImageIO.write(mask, "png", out)
        return out.toByteArray()
    }

    private fun shouldShow(): Boolean {
        if (!ModConfig.showInDebug && HudManager.isDebugScreenVisible) return false
        if (!ModConfig.showInGuis && HudManager.isGuiScreenOpen) return false
        if (!ModConfig.showInThirdPerson && !mc.options.cameraType.isFirstPerson) return false
        return !(mc.player != null && mc.player!!.isSpectator && !ModConfig.showInSpectator)
    }

    private fun dynamicColor(): PolyColor {
        if (!ModConfig.dynamicColor) return WHITE
        val entity = (mc.hitResult as? EntityHitResult)?.entity ?: return WHITE
        if (entity.isInvisible) return WHITE
        val cat: MobCategory = entity.type.category
        if (ModConfig.hostile && cat == MobCategory.MONSTER) return ModConfig.hostileColor
        if (ModConfig.passive && (cat == MobCategory.CREATURE || cat == MobCategory.WATER_CREATURE ||
                cat == MobCategory.WATER_AMBIENT || cat == MobCategory.AMBIENT)) return ModConfig.passiveColor
        if (ModConfig.player && entity is Player) return ModConfig.playerColor
        return WHITE
    }
}
