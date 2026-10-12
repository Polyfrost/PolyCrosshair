package org.polyfrost.crosshair.render

import net.minecraft.client.Minecraft
//? if >=1.21 {
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.renderer.texture.DynamicTexture
//?} else {
/*import net.minecraft.client.render.texture.DynamicTexture
*///?}
//? if >=1.21.11 {
import net.minecraft.resources.Identifier as ResourceLocation
//?} else if >=1.21 {
/*import net.minecraft.resources.ResourceLocation
*///?} else {
/*import net.minecraft.resource.Identifier as ResourceLocation
*///?}
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.crosshair.config.CrosshairPixels
import org.polyfrost.crosshair.config.CrosshairProfile
import java.io.ByteArrayInputStream

class CrosshairTexture(
    val art: Layer,
    val silhouette: Layer,
    val outline: Layer?,
) {
    class Layer(val location: ResourceLocation, val texture: DynamicTexture, val width: Int, val height: Int) {
        fun close() {
            //? if >=1.21 {
            texture.close()
            //?} else {
            /*texture.clearGlId()
            *///?}
        }
    }

    fun close() {
        runCatching { art.close() }
        runCatching { silhouette.close() }
        outline?.let { runCatching { it.close() } }
    }
}

object CrosshairTextures {

    private class Entry(val key: Key, val texture: CrosshairTexture)

    private data class Key(
        val image: String,
        val cells: Int,
        val stretch: Boolean,
        val outlineEnabled: Boolean,
        val thickness: Float,
    )

    private val cache = HashMap<String, Entry>()

    fun get(profile: CrosshairProfile): CrosshairTexture? {
        val key = Key(
            profile.image.get().orEmpty(),
            profile.grid.cells,
            profile.resizeStretch.get() == true,
            profile.outlineEnabled.get() == true,
            profile.outlineThickness.get() ?: 0f,
        )
        cache[profile.key]?.let { if (it.key == key) return it.texture }

        val pixels = profile.pixels()
        if (pixels.isEmpty()) {
            cache.remove(profile.key)?.texture?.close()
            return null
        }

        val built = runCatching { build(profile.key, pixels, key) }.getOrElse {
            PolyCrosshair.LOGGER.error("failed to build crosshair textures for '{}'", profile.key, it)
            null
        } ?: return null

        cache.put(profile.key, Entry(key, built))?.texture?.close()
        return built
    }

    private fun build(id: String, pixels: CrosshairPixels, key: Key): CrosshairTexture {
        val art = upload("${id}_art", pixels)
        val silhouette = upload("${id}_mask", pixels.silhouette())
        val outline = if (key.outlineEnabled) {
            pixels.outline(key.thickness)?.let { upload("${id}_outline", it) }
        } else {
            null
        }
        return CrosshairTexture(art, silhouette, outline)
    }

    private fun upload(id: String, pixels: CrosshairPixels): CrosshairTexture.Layer {
        //? if >=1.21 {
        val native = NativeImage.read(ByteArrayInputStream(pixels.toPngBytes()))
        val texture = DynamicTexture(
            //? if >=1.21.5 {
            { id },
            //?}
            native
        )
        val location = ResourceLocation.fromNamespaceAndPath("polycrosshair", id)
        //?} else {
        /*val texture = DynamicTexture(pixels.toImage())
        val location = ResourceLocation("polycrosshair", id)
        *///?}
        Minecraft.getInstance().textureManager.register(location, texture)
        return CrosshairTexture.Layer(location, texture, pixels.size, pixels.size)
    }
}
