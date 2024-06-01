@file:Suppress("UnstableAPIUsage")

package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.gui.animations.*
import cc.polyfrost.oneconfig.gui.elements.BasicButton
import cc.polyfrost.oneconfig.images.OneImage
import cc.polyfrost.oneconfig.libs.universal.UKeyboard
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.*
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.*
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.elements.*
import org.polyfrost.crosshair.render.CrosshairRenderer
import org.polyfrost.crosshair.utils.*
import java.awt.Image
import java.awt.image.BufferedImage
import kotlin.math.ceil


private fun notify(message: String) = Notifications.INSTANCE.send(PolyCrosshair.NAME, message)

object Drawer : BasicOption(null, null, "", "", "", "", 2) {

    val pixels: Array<Pixel> = Array(1024) { Pixel(it) }

    var elements = HashMap<CrosshairEntry, PresetElement>()

    var removeQueue = ArrayList<CrosshairEntry>()

    var moveQueue = ArrayList<MoveType>()

    private var scroll = 0f

    private var scrollTarget = 0f

    private var scrollAnimation: Animation = DummyAnimation(0f)

    private val resetButton = BasicButton(64, 32, "Reset", 2, ColorPalette.PRIMARY_DESTRUCTIVE)

    private val saveButton = BasicButton(64, 32, "Save", 2, ColorPalette.PRIMARY)

    private val importButton = BasicButton(64, 32, "Import", 2, ColorPalette.SECONDARY)

    private val exportButton = BasicButton(64, 32, "Export", 2, ColorPalette.SECONDARY)

    private val colorSelector = ColorSelector()

    init {
        Utils.toBufferedImage(ModConfig.newCurrentCrosshair.img)?.let { it ->
            loadImage(it, false, ModConfig.newCurrentCrosshair)?.let {
                CrosshairRenderer.updateTexture(it)
            }
        }
        resetButton.setClickAction {
            runAsync {
                reset()
            }
        }
        saveButton.setClickAction {
            runAsync {
                Utils.save(saveFromDrawer(false))
            }
        }
        exportButton.setClickAction {
            runAsync {
                saveFromDrawer(false)?.let { Utils.copy(it.image) }
            }

        }
        importButton.setClickAction {
            runAsync {
                IOUtils.getImageFromClipboard()?.let {
                    loadImage(it.toBufferedImage(), true)
                }
            }
        }
    }

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
//        var y = y
//        modeSwitch.draw(vg, x.toFloat(), y.toFloat(), inputHandler)
//        if (ModConfig.mode) return
//        y += 48
        if (moveQueue.isNotEmpty()) {
            var x = 0
            var y = 0
            for (i in moveQueue) {
                x += i.x
                y += i.y
            }
            move(x, y)
            moveQueue.clear()
        }

        for (posY in 0..<ModConfig.canvaSize) {
            for (posX in 0..<ModConfig.canvaSize) {
                pixels[Utils.posToIndex(posX, posY)].draw(vg, x.toFloat(), y.toFloat(), inputHandler)
            }
        }

        if (ModConfig.canvaSize % 2 == 0) {
            nanoVGHelper.drawLine(vg, (x + 128).toFloat(), (y + 108).toFloat(), (x + 128).toFloat(), (y + 148).toFloat(), 1f, OneColor("703A3AFF").rgb)
            nanoVGHelper.drawLine(vg, (x + 108).toFloat(), (y + 128).toFloat(), (x + 148).toFloat(), (y + 128).toFloat(), 1f, OneColor("703A3AFF").rgb)
        }

        importButton.draw(vg, (x + 270).toFloat(), (y + 48).toFloat(), inputHandler)
        resetButton.draw(vg, (x + 270).toFloat(), (y + 174).toFloat(), inputHandler)
        saveButton.draw(vg, (x + 270).toFloat(), (y + 222).toFloat(), inputHandler)
        colorSelector.draw(vg, (x + 270).toFloat(), (y + 126).toFloat(), inputHandler)
        exportButton.draw(vg, (x + 270).toFloat(), y.toFloat(), inputHandler)

        for (i in removeQueue) {
            ModConfig.newCrosshairs.remove(i)
            getElement(i).onRemove()
            elements.remove(i)
        }

        removeQueue.clear()

        val height = (149 + 16) * ceil(ModConfig.newCrosshairs.size / 4f) - 16

        if (height <= 256) scrollAnimation = DummyAnimation(0f)

        scroll = scrollAnimation.get()

        val scissor = ScissorHelper.INSTANCE.scissor(vg, (x + 349).toFloat(), y.toFloat(), 644f, 256f)

        if (scissor.isInScissor(inputHandler.mouseX(), inputHandler.mouseY())) {
            inputHandler.unblockDWheel()

            val dWheel = inputHandler.dWheel.toFloat()

            inputHandler.blockDWheel()

            if (dWheel != 0f) {
                scrollTarget += dWheel

                if (scrollTarget > 0f) scrollTarget = 0f
                else if (scrollTarget < 256 - height) scrollTarget = (256 - height)

                scrollAnimation = EaseOutQuad(150, scroll, scrollTarget, false)
            }
        } else {
            inputHandler.unblockDWheel()
            inputHandler.stopBlockingInput()
        }

        val size = ModConfig.newCrosshairs.size

        for (i in 0..<size) {
            val posX = i % 4
            val posY = i / 4
            getElement(ModConfig.newCrosshairs[i]).draw(vg, x + 349 + posX * 165f, y + posY * 165f + scroll, inputHandler)
        }

        ScissorHelper.INSTANCE.resetScissor(vg, scissor)
    }

    fun Image.toBufferedImage(): BufferedImage {
        if (this is BufferedImage) {
            return this
        }
        val bufferedImage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        val graphics2D = bufferedImage.createGraphics()
        graphics2D.drawImage(this, 0, 0, null)
        graphics2D.dispose()

        return bufferedImage
    }

    fun loadImage(image: BufferedImage?, save: Boolean, entry: CrosshairEntry = CrosshairEntry()): OneImage? {
        val loadedImage = OneImage(image)
        if (loadedImage.width != loadedImage.height || loadedImage.width !in 15..32) {
            notify("Image size isn't supported.")
            return null
        }
        ModConfig.newCurrentCrosshair.loadFrom(entry)
        ModConfig.canvaSize = loadedImage.height
        for (posY in 0..<ModConfig.canvaSize) {
            for (posX in 0..<ModConfig.canvaSize) {
                val c = loadedImage.image.getRGB(posX, posY)
                pixels[Utils.posToIndex(posX, posY)].isToggled = c shr 24 != 0
                pixels[Utils.posToIndex(posX, posY)].color = c
            }
        }
        if (save) Utils.save(loadedImage)
        return loadedImage
    }

    fun saveFromDrawer(close: Boolean): OneImage? {
        val image = OneImage(ModConfig.canvaSize, ModConfig.canvaSize)
        if (ModConfig.drawer.isEmpty() && !close) {
            notify("Crosshair can't be empty.")
            return null
        }
        for (i in ModConfig.drawer) {
            val pos = Utils.indexToPos(i.key)
            if (pos.x >= ModConfig.canvaSize || pos.y >= ModConfig.canvaSize) {
                pixels[i.key].isToggled = false
                continue
            }
            val c = i.value
            image.setColorAtPos(pos.x, pos.y, c)
        }
        return image
    }

    fun reset() {
        val newEntry = CrosshairEntry()
        Utils.toBufferedImage(newEntry.img)?.let {
            loadImage(it, false, newEntry)
        }
    }

    fun move(x: Int, y: Int) {
        val newPositions = HashMap<Pos, Int>()
        for (i in ModConfig.drawer) {
            val pos = Utils.indexToPos(i.key)
            val posX = pos.x + x
            val posY = pos.y + y
            pixels[i.key].isToggled = false
            if (posX !in 0..<ModConfig.canvaSize || posY !in 0..<ModConfig.canvaSize) continue
            newPositions[Pos(posX, posY)] = i.value
        }
        for (i in newPositions) {
            val index = i.key.y * 32 + i.key.x
            pixels[index].isToggled = true
            pixels[index].color = i.value
        }
    }

    fun getElement(entry: CrosshairEntry): PresetElement {
        elements[entry] ?: elements.put(entry, PresetElement(entry))
        return elements[entry]!!
    }

    enum class MoveType(val x: Int, val y: Int) {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0)
    }

    override fun finishUpAndClose() {
        val image = saveFromDrawer(true) ?: return
        ModConfig.newCurrentCrosshair.img = Utils.toBase64(image.image)
        CrosshairRenderer.updateTexture(image)
    }

    override fun getHeight() = 256

    override fun keyTyped(key: Char, keyCode: Int) {

        if (keyCode == UKeyboard.KEY_W) moveQueue.add(MoveType.UP)
        if (keyCode == UKeyboard.KEY_S) {
            if (UKeyboard.isCtrlKeyDown()) {
                runAsync {
                    Utils.save(saveFromDrawer(false))
                }
            } else {
                moveQueue.add(MoveType.DOWN)
            }
        }
        if (keyCode == UKeyboard.KEY_A) moveQueue.add(MoveType.LEFT)
        if (keyCode == UKeyboard.KEY_D) moveQueue.add(MoveType.RIGHT)
    }

}