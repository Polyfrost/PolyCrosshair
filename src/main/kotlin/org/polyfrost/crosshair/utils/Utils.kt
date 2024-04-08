@file:Suppress("UnstableAPIUsage")

package org.polyfrost.crosshair.utils

import cc.polyfrost.oneconfig.images.OneImage
import cc.polyfrost.oneconfig.utils.IOUtils
import cc.polyfrost.oneconfig.utils.Notifications
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.config.ModConfig
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

private fun notify(message: String) = Notifications.INSTANCE.send(PolyCrosshair.NAME, message)

object Utils {

    fun export(image: BufferedImage?, name: String): String {
        image ?: return ""
        val path = PolyCrosshair.path + name + ".png"
        OneImage(image).save(path)
        return path
    }

    fun save(image: OneImage?) {
        image ?: return
        val base64 = toBase64(image.image)
        if (ModConfig.crosshairs.contains(base64)) {
            notify("Duplicated crosshair.")
            return
        }
        ModConfig.crosshairs.add(base64)
    }

    fun toBufferedImage(string: String): BufferedImage? {
        val bytes = Base64.getDecoder().decode(string)
        return ImageIO.read(ByteArrayInputStream(bytes))
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
        IOUtils.copyImageToClipboard(image)
        notify("Crosshair has been copied to clipboard.")
    }

}