package org.polyfrost.crosshair

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.texture.TextureUtil
import org.lwjgl.opengl.GL11
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
    }

    override fun hasBackground() = false

    override fun render(stack: UMatrixStack, x: Float, y: Float, scaleX: Float, scaleY: Float) {
        //val mc = Minecraft.getMinecraft()
        //if ((mc.ingameGUI as? GuiIngameAccessor)?.shouldShowCrosshair() == false) return

        GL.pushMatrix()
        GL.enableBlend()
        GL.enableAlpha()
        GL.bindTexture(id)
        GL.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GL.color(1f, 1f, 1f, 1f)
        val mcScale = UResolution.scaleFactor.toFloat()
        GL.scale(1f / mcScale, 1f / mcScale, 1f)
        val texSizeI = texSize.toInt()
        val size = (texSize * cl1(scaleX, scaleY) * mcScale).toInt()
        Gui.drawScaledCustomSizeModalRect((x / mcScale).toInt(), (y / mcScale).toInt(), 0f, 0f, texSizeI, texSizeI, size, size, texSize, texSize)
        GL.popMatrix()
    }


    fun setCrosshair(cdata: IntArray, size: Int) {
        texSize = size.toFloat()
        TextureUtil.uploadTexture(id, cdata, size, size)
    }

    override fun defaultPosition() = Vec2(1920f / 2f - 7f, 1080f / 2f - 7f)

    override fun multipleInstancesAllowed() = false

    override fun id() = "polycrosshair.json"

    override fun title() = "PolyCrosshair"

    override fun update() = false
}