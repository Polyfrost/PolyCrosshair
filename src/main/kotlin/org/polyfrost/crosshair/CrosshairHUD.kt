package org.polyfrost.crosshair

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.polyfrost.crosshair.mixin.GuiIngameAccessor
import org.polyfrost.oneconfig.api.config.v1.annotations.Include
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.utils.cl1
import org.polyfrost.polyui.utils.getResourceStream
import org.polyfrost.universal.UMatrixStack
import org.polyfrost.universal.UResolution
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import net.minecraft.client.renderer.GlStateManager as GL

object CrosshairHUD : LegacyHud() {
    @Switch(title = "Show in F3")
    private var showInDebug = false

    @Switch(title = "Show in GUIs")
    private var showInGUIs = true

    @Switch(title = "Show in Spectator")
    private var showInSpectator = true

    @Switch(title = "Show in 3rd Person")
    private var showInThirdPerson = true

    @Include
    var currentCrosshair: String? = null

    private var id = GL.generateTexture()
    private var texSize = 15f

    override var width: Float
        get() = texSize
        set(value) {}

    override var height: Float
        get() = texSize
        set(value) {}

    private val target = Paths.get("polycrosshair.png")

    override fun category() = Category.COMBAT

    override fun initialize() {
        val img = if (currentCrosshair.isNullOrEmpty() || !target.exists()) ImageIO.read(getResourceStream("assets/polycrosshair/default.png"))
        else ImageIO.read(target.inputStream())
        setCrosshair(img.getRGB(0, 0, img.width, img.height, null, 0, img.width), img.width)
        DynamicTexture(img)
    }

    override fun hasBackground() = false

    override fun render(stack: UMatrixStack, x: Float, y: Float, scaleX: Float, scaleY: Float) {
        val mc = Minecraft.getMinecraft()
        if (!(mc.ingameGUI as GuiIngameAccessor).shouldShowCrosshair()) return

        GL.enableAlpha()
        GL.enableBlend()
        GL.bindTexture(id)
        GL.color(1f, 1f, 1f, 1f)
        val mcScale = UResolution.scaleFactor.toFloat()
        GL.tryBlendFuncSeparate(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR, 1, 0)

        val tesellator = Tessellator.getInstance()
        val renderer = tesellator.worldRenderer
        renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        val tex = texSize.toDouble()
        val left = (0).toDouble()
        val top = (0).toDouble()
        val right = (left + texSize * scaleX)
        val bottom = (top + texSize * scaleY)
        renderer.pos(left, bottom, 0.0).tex(0.0, tex).endVertex()
        renderer.pos(right, bottom, 0.0).tex(tex, tex).endVertex()
        renderer.pos(right, top, 0.0).tex(tex, 0.0).endVertex()
        renderer.pos(left, top, 0.0).tex(0.0, 0.0).endVertex()
        tesellator.draw()
//        Gui.drawScaledCustomSizeModalRect(x.toInt(), y.toInt(), 0f, 0f, texSize.toInt(), texSize.toInt(), texSize.toInt(), texSize.toInt(), texSize, texSize)
        GL.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GL.disableBlend()
    }


    fun setCrosshair(cdata: IntArray, size: Int) {
        texSize = size.toFloat()
        TextureUtil.allocateTexture(id, size, size)
        TextureUtil.uploadTexture(id, cdata, size, size)
    }

    override fun defaultPosition() = Vec2(1920f / 2f - 7f, 1080f / 2f - 7f)

    override fun multipleInstancesAllowed() = false

    override fun id() = "polycrosshair.json"

    override fun title() = "PolyCrosshair"

    override fun update() = false
}