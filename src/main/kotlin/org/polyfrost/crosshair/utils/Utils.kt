@file:Suppress("UnstableAPIUsage")

package org.polyfrost.crosshair.utils

import dev.deftu.clipboard.BufferedClipboardImage
import dev.deftu.clipboard.Clipboard
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.config.CrosshairEntry
import org.polyfrost.crosshair.config.PolyCrosshairConfig
import org.polyfrost.oneconfig.api.ui.v1.Notifications
import org.polyfrost.oneconfig.utils.v1.OneImage
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.io.path.Path

fun notify(message: String) = Notifications.enqueue(Notifications.Type.Info, PolyCrosshair.NAME, message)

fun posToIndex(x: Int, y: Int): Int =
    x + y * 32

fun indexToPos(index: Int): Pos =
    Pos(index % 32, index / 32)

fun export(image: BufferedImage?, name: String): String {
    image ?: return ""
    val path = PolyCrosshair.path + name + ".png"
    OneImage(image).save(Path(path))
    return path
}

fun save(image: OneImage?) {
    image ?: return
    val base64 = toBase64(image.image)
    PolyCrosshairConfig.crosshairs.forEach {
        if (it.base64 == base64) {
            it.loadFrom(PolyCrosshairConfig.currentCustomCrosshair)
            return
        }
    }
    val entry = CrosshairEntry(CrosshairEntry.DEFAULT)
    entry.loadFrom(PolyCrosshairConfig.currentCustomCrosshair)
    entry.base64 = base64
    PolyCrosshairConfig.crosshairs.add(entry)
}

fun toBufferedImage(string: String): BufferedImage? {
    val bytes = Base64.getDecoder().decode(string)
    return ImageIO.read(ByteArrayInputStream(bytes))
}

fun toBufferedImage(image: Image): BufferedImage {
    if (image is BufferedImage) return image

    val bufferedImage = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)
    val graphics = bufferedImage.createGraphics()
    graphics.drawImage(image, 0, 0, null)
    graphics.dispose()

    return bufferedImage
}

fun toBase64(image: BufferedImage): String {
    val byteOut = ByteArrayOutputStream()
    ImageIO.write(image, "png", byteOut)
    val encoded = Base64.getEncoder().encodeToString(byteOut.toByteArray())
    byteOut.close()
    return encoded
}

fun copy(image: Image?) {
    image ?: return
    Clipboard.getInstance().image = BufferedClipboardImage.toClipboardImage(toBufferedImage(image))
    notify("Crosshair has been copied to clipboard.")
}
