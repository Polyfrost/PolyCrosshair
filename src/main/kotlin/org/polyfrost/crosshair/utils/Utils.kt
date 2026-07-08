package org.polyfrost.crosshair.utils

import org.polyfrost.oneconfig.api.notifications.v1.Notifications
import org.polyfrost.oneconfig.utils.v1.ClipboardHelper
import org.polyfrost.crosshair.PolyCrosshair
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import javax.imageio.ImageIO

fun notify(message: String) = Notifications.send(PolyCrosshair.NAME, message)

fun posToIndex(x: Int, y: Int): Int = x + y * 32

fun indexToPos(index: Int): Pos = Pos(index % 32, index / 32)

fun toBufferedImage(base64: String): BufferedImage? = try {
    ImageIO.read(ByteArrayInputStream(Base64.getDecoder().decode(base64)))
} catch (_: Exception) {
    null
}

fun toBase64(image: BufferedImage): String {
    val out = ByteArrayOutputStream()
    ImageIO.write(image, "png", out)
    return Base64.getEncoder().encodeToString(out.toByteArray())
}

fun copyToClipboard(image: BufferedImage) {
    ClipboardHelper.setImage(image)
    notify("Crosshair has been copied to clipboard.")
}

fun imageFromClipboard(): BufferedImage? {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    return try {
        if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            (clipboard.getData(DataFlavor.imageFlavor) as? Image)?.toBufferedImage()
        } else if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
            val files = clipboard.getData(DataFlavor.javaFileListFlavor) as? List<*>
            (files?.firstOrNull() as? File)?.let { ImageIO.read(it) }
        } else null
    } catch (_: Exception) {
        null
    }
}

fun Image.toBufferedImage(): BufferedImage {
    if (this is BufferedImage) return this
    val buffered = BufferedImage(getWidth(null), getHeight(null), BufferedImage.TYPE_INT_ARGB)
    val g = buffered.createGraphics()
    g.drawImage(this, 0, 0, null)
    g.dispose()
    return buffered
}
