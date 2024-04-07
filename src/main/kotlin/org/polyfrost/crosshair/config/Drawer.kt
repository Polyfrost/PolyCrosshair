@file:Suppress("UnstableAPIUsage")

package org.polyfrost.crosshair.config

import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.gui.elements.BasicButton
import cc.polyfrost.oneconfig.images.OneImage
import cc.polyfrost.oneconfig.libs.universal.*
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.*
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.runAsync
import com.google.gson.*
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.elements.*
import org.polyfrost.crosshair.render.CrosshairRenderer
import java.awt.image.BufferedImage
import java.io.*
import java.net.*
import java.util.*
import javax.imageio.ImageIO


private fun notify(message: String) = Notifications.INSTANCE.send(PolyCrosshair.NAME, message)

object Drawer : BasicOption(null, null, "", "", "", "", 2) {

    val pixels: Array<Pixel> = Array(225) { Pixel(it) }

    private var presets = ArrayList<PresetElement>()

    var removeQueue = ArrayList<PresetElement>()

    var moveQueue = ArrayList<MoveType>()

    private val clearButton = BasicButton(64, 32, "Clear", 2, ColorPalette.PRIMARY_DESTRUCTIVE)

    private val saveButton = BasicButton(64, 32, "Save", 2, ColorPalette.PRIMARY)

    private val importButton = BasicButton(64, 32, "Import", 2, ColorPalette.PRIMARY)

    private val exportButton = BasicButton(64, 32, "Export", 2, ColorPalette.PRIMARY)

    private val colorSelector = ColorSelector()

    private val path = "${ConfigUtils.getProfileDir().absolutePath}/${PolyCrosshair.MODID}/Custom Crosshairs/"

    private val dir = File(path)

    init {
        if (!dir.exists()) {
            dir.mkdirs()
        }
        clearButton.setClickAction {
            for (pixel in pixels) {
                pixel.isToggled = false
            }
        }
        saveButton.setClickAction {
            runAsync {
                save(saveImage())
            }
        }
        exportButton.setClickAction {
            runAsync {
                saveImage()?.let { export(it.image) }
            }

        }
        importButton.setClickAction {
            runAsync {
                IOUtils.getStringFromClipboard()?.let {
                    notify("Importing crosshair from your clipboard.")
                    loadImage(ImageIO.read(URL("https://iili.io/$it.png")), true)
                }
            }
        }
        refreshPresets()
        EventManager.INSTANCE.register(this)
    }

    fun refreshPresets() {
        presets.clear()
        val list = dir.listFiles() ?: return
        for (i in list) {
            val path = i.absolutePath
            if (isImage(i)) {
                presets.add(PresetElement(path))
            }
        }
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

        presets.removeAll(removeQueue.toSet())

        removeQueue.clear()

        val scissor = ScissorHelper.INSTANCE.scissor(vg, (x + 349).toFloat(), y.toFloat(), 644f, 254f)

//        val hovering = scissor.isInScissor(inputHandler.mouseX(), inputHandler.mouseY())

        for (i in 0..<presets.size) {
            val posX = i % 4
            val posY = i / 4
            presets[i].draw(vg, x + 349 + posX * 165f, y + posY * 165f, inputHandler)
        }

        ScissorHelper.INSTANCE.resetScissor(vg, scissor)
    }

    fun loadFromFile(path: String, save: Boolean) {
        if (path.isBlank()) return
        val file = File(path)
        if (!file.exists() || !file.isFile) return

        loadImage(ImageIO.read(file), save)
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

    fun saveImage(): OneImage? {
        val image = OneImage(15, 15)
        if (ModConfig.crosshair.isEmpty()) return null
        for (i in ModConfig.crosshair) {
            val pos = indexToPos(i.key)
            val c = i.value.color
            image.setColorAtPos(pos.x, pos.y, c)
        }
        return image
    }

    fun save(image: OneImage?): File? {
        image ?: return null
        val path = path + UUID.randomUUID() + ".png"
        image.save(path)
        presets.add(PresetElement(path))
        return File(path)
    }

    fun export(image: BufferedImage) {
        val `object` = upload(image) ?: return
        val link = `object`["image"].asJsonObject["id_encoded"].asString
        IOUtils.copyStringToClipboard(link)
        notify("Crosshair ID has been copied to clipboard.")
    }

    fun upload(image: BufferedImage): JsonObject? {
        try {
            val byteOut = ByteArrayOutputStream()
            ImageIO.write(image, "png", byteOut)
            val encoded = Base64.getEncoder().encodeToString(byteOut.toByteArray())
            byteOut.close()
            val url = URL("https://freeimage.host/api/1/upload?key=6d207e02198a847aa98d0a2a901485a5&source=$encoded&format=json")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.doInput = true
            con.doOutput = true
            con.connect()

            if (con.responseCode != 200) notify("Failed")

            val rd = BufferedReader(InputStreamReader(con.inputStream))
            val `object` = JsonParser().parse(rd).asJsonObject
            rd.close()
            return `object`
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun isImage(file: File): Boolean = file.name.endsWith(".png")

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

    fun indexToPos(index: Int): Pos =
        Pos(index % 15, index / 15)

    enum class MoveType(val x: Int, val y: Int) {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0)
    }

    data class Pos(val x: Int, val y: Int)

    override fun finishUpAndClose() {
        CrosshairRenderer.updateTexture()
    }

    override fun getHeight() = 254

    override fun keyTyped(key: Char, keyCode: Int) {
        if (keyCode == UKeyboard.KEY_F5) refreshPresets()
        if (keyCode == UKeyboard.KEY_W) moveQueue.add(MoveType.UP)
        if (keyCode == UKeyboard.KEY_S) moveQueue.add(MoveType.DOWN)
        if (keyCode == UKeyboard.KEY_A) moveQueue.add(MoveType.LEFT)
        if (keyCode == UKeyboard.KEY_D) moveQueue.add(MoveType.RIGHT)
    }

}