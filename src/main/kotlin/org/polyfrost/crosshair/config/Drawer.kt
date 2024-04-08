@file:Suppress("UnstableAPIUsage")

package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.gui.elements.BasicButton
import cc.polyfrost.oneconfig.images.OneImage
import cc.polyfrost.oneconfig.libs.universal.*
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.*
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.runAsync
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.elements.*
import org.polyfrost.crosshair.render.CrosshairRenderer
import org.polyfrost.crosshair.utils.*
import java.awt.Image
import java.awt.image.BufferedImage
import java.util.*
import kotlin.collections.HashMap


private fun notify(message: String) = Notifications.INSTANCE.send(PolyCrosshair.NAME, message)

object Drawer : BasicOption(null, null, "", "", "", "", 2) {

    val pixels: Array<Pixel> = Array(225) { Pixel(it) }

    var elements = HashMap<String, PresetElement>()

    var removeQueue = ArrayList<String>()

    var moveQueue = ArrayList<MoveType>()

    private val clearButton = BasicButton(64, 32, "Clear", 2, ColorPalette.PRIMARY_DESTRUCTIVE)

    private val saveButton = BasicButton(64, 32, "Save", 2, ColorPalette.PRIMARY)

    private val importButton = BasicButton(64, 32, "Import", 2, ColorPalette.SECONDARY)

    private val exportButton = BasicButton(64, 32, "Export", 2, ColorPalette.SECONDARY)

    private val colorSelector = ColorSelector()

    init {
        clearButton.setClickAction {
            for (pixel in pixels) {
                pixel.isToggled = false
            }
        }
        saveButton.setClickAction {
            runAsync {
                save(saveFromDrawer())
            }
        }
        exportButton.setClickAction {
            runAsync {
                saveFromDrawer()?.let { Utils.copy(it.image) }
            }

        }
        importButton.setClickAction {
            runAsync {
                IOUtils.getImageFromClipboard()?.let {
                    notify("Importing crosshair from your clipboard.")
                    loadImage(it.toBufferedImage(), true)
                }
            }
        }
        EventManager.INSTANCE.register(this)
    }

    override fun draw(vg: Long, x: Int, y: Int, inputHandler: InputHandler) {
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

        for (pixel in pixels) {
            pixel.draw(vg, x.toFloat(), y.toFloat(), inputHandler)
        }
        importButton.draw(vg, (x + 270).toFloat(), (y + 48).toFloat(), inputHandler)
        clearButton.draw(vg, (x + 270).toFloat(), (y + 174).toFloat(), inputHandler)
        saveButton.draw(vg, (x + 270).toFloat(), (y + 222).toFloat(), inputHandler)
        colorSelector.draw(vg, (x + 270).toFloat(), (y + 126).toFloat(), inputHandler)
        exportButton.draw(vg, (x + 270).toFloat(), y.toFloat(), inputHandler)

        for (i in removeQueue) {
            ModConfig.presets.remove(i)
            getElement(i).onRemove()
            elements.remove(i)
        }

        removeQueue.clear()

        val scissor = ScissorHelper.INSTANCE.scissor(vg, (x + 349).toFloat(), y.toFloat(), 644f, 254f)

        for (i in 0..<ModConfig.presets.size) {
            val posX = i % 4
            val posY = i / 4
            getElement(ModConfig.presets[i]).draw(vg, x + 349 + posX * 165f, y + posY * 165f, inputHandler)
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

    fun loadImage(image: BufferedImage?, save: Boolean) {
        val loadedImage = OneImage(image)
        if (loadedImage.width != 15 || loadedImage.height != 15) {
            notify("Image must be 15 x 15.")
            return
        }
        for (i in 0..224) {
            val pos = indexToPos(i)
            val c = loadedImage.image.getRGB(pos.x, pos.y)
            pixels[i].isToggled = c shr 24 != 0
            pixels[i].color = c
        }
        if (save) save(loadedImage)
    }

    fun saveFromDrawer(): OneImage? {
        val image = OneImage(15, 15)
        if (ModConfig.crosshair.isEmpty()) {
            notify("Crosshair cant be empty.")
            return null
        }
        for (i in ModConfig.crosshair) {
            val pos = indexToPos(i.key)
            val c = i.value.color
            image.setColorAtPos(pos.x, pos.y, c)
        }
        return image
    }

    fun save(image: OneImage?) {
        image ?: return
        val base64 = Utils.toBase64(image.image)
        if (ModConfig.presets.contains(base64)) {
            notify("Duplicated crosshair.")
            return
        }
        ModConfig.presets.add(base64)
    }

    fun move(x: Int, y: Int) {
        val newPositions = HashMap<Pos, Int>()
        for (i in ModConfig.crosshair) {
            val pos = indexToPos(i.key)
            val posX = pos.x + x
            val posY = pos.y + y
            pixels[i.key].isToggled = false
            if (posX !in 0..14 || posY !in 0..14) continue
            newPositions[Pos(posX, posY)] = i.value.color
        }
        for (i in newPositions) {
            val index = i.key.y * 15 + i.key.x
            if (index !in 0..224) continue
            pixels[index].isToggled = true
            pixels[index].color = i.value
        }
    }

    fun getElement(base64: String): PresetElement {
        elements[base64] ?: elements.put(base64, PresetElement(base64))
        return elements[base64]!!
    }

    fun indexToPos(index: Int): Pos =
        Pos(index % 15, index / 15)

    enum class MoveType(val x: Int, val y: Int) {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0)
    }

    override fun finishUpAndClose() {
        CrosshairRenderer.updateTexture()
    }

    override fun getHeight() = 254

    override fun keyTyped(key: Char, keyCode: Int) {
        if (keyCode == UKeyboard.KEY_W) moveQueue.add(MoveType.UP)
        if (keyCode == UKeyboard.KEY_S) moveQueue.add(MoveType.DOWN)
        if (keyCode == UKeyboard.KEY_A) moveQueue.add(MoveType.LEFT)
        if (keyCode == UKeyboard.KEY_D) moveQueue.add(MoveType.RIGHT)
    }

}