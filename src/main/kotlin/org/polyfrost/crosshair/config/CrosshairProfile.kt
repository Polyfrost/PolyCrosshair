package org.polyfrost.crosshair.config

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.crosshair.ui.CrosshairEditor
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class CrosshairProfile(
    root: Tree,
    val key: String,
    val category: String,
    val defaultImage: String = CrosshairPixels.DEFAULT,
    enabledDescription: String = "Shows this crosshair when you look at ${category.lowercase()} targets. Turn this off to use the Normal crosshair instead.",
    enabledDefault: Boolean = false,
    indicator: Boolean = false,
    copySource: (() -> CrosshairProfile)? = null,
) {
    @Volatile
    var revision: Int = 0
        private set

    val enabled: Property<Boolean>? = copySource?.let {
        root.switch(
            id = "${key}_enabled",
            title = "Use $category Crosshair",
            description = enabledDescription,
            category = category,
            def = enabledDefault,
        )
    }

    @Suppress("unused")
    private val copyButton: Property<Void>? = copySource?.let { source ->
        root.button(
            id = "${key}_copy",
            title = "Copy Normal and Paste Here",
            description = "Copies every setting from the Normal crosshair and pastes it over the $category one, replacing what's here.",
            category = category,
            text = "Copy & Paste",
        ) { copyFrom(source()) }
    }

    val image: Property<String> = root.custom(
        id = "${key}_image",
        title = "\u00A0",
        category = category,
        def = defaultImage,
        type = String::class.java,
        visualizer = Visualizer { prop -> CrosshairEditor(this, prop) },
    )

    val circles: Property<String> = root.hidden("${key}_circles", "", String::class.java)

    val offsetX: Property<Float>? = if (indicator) root.slider(
        id = "${key}_offset_x",
        title = "Offset X",
        description = "How far right of the crosshair this is drawn, in crosshair pixels.",
        category = category,
        min = -32f, max = 32f, def = 0f, step = 1f,
    ) else null

    val offsetY: Property<Float>? = if (indicator) root.slider(
        id = "${key}_offset_y",
        title = "Offset Y",
        description = "How far below the crosshair this is drawn, in crosshair pixels.",
        category = category,
        min = -32f, max = 32f, def = 11f, step = 1f,
    ) else null

    private val orbit = if (indicator) root.accordion(
        id = "${key}_orbit",
        title = "Follow Pitch",
        description = "Swings this around the right side of the crosshair as you look up and down: level puts it to the right, 30° down puts it above, 30° up puts it below. Replaces Offset X and Offset Y while on.",
        category = category,
        def = true,
    ) else null
    val orbitEnabled: Property<Boolean>? = orbit?.second
    val orbitRadius: Property<Float>? = orbit?.first?.slider(
        id = "radius",
        title = "Radius",
        description = "How far from the crosshair this circles, in crosshair pixels.",
        category = category,
        min = 0f, max = 32f, def = 11f, step = 1f,
    )

    val attackIndicatorGap: Property<Float>? = if (indicator) root.slider(
        id = "${key}_attack_indicator_gap",
        title = "Attack Indicator Gap",
        description = "When this overlaps the vanilla attack indicator, the indicator moves down to sit this far below it.",
        category = category,
        min = -16f, max = 16f, def = -2f, step = 1f,
    ) else null

    val gridSize: Property<Int> = root.dropdown(
        id = "${key}_grid_size",
        title = "Grid Size",
        description = "The size of the drawing canvas, and how large the crosshair is on screen.",
        category = category,
        options = GridSize.LABELS,
        def = GridSize.ONE.ordinal,
    )

    val resizeStretch: Property<Boolean> = root.switch(
        id = "${key}_resize_stretch",
        title = "Grid Resize Stretch",
        description = "Stretches the crosshair to fill the new canvas when the grid size changes. Turn this off to keep its size and re-center it.",
        category = category,
        def = true,
    )

    private val outline = root.accordion(
        id = "${key}_outline",
        title = "Outline",
        description = "Draws a border around the crosshair.",
        category = category,
    )
    val outlineEnabled: Property<Boolean> = outline.second
    val outlineThickness: Property<Float> = outline.first.slider(
        id = "thickness",
        title = "Outline Thickness",
        description = "How far the outline extends, in crosshair pixels.",
        category = category,
        min = 0f, max = 1f, def = 0.5f, step = 0.01f,
    )
    val outlineColor: Property<PolyColor> = outline.first.color(
        id = "color",
        title = "Outline Color",
        category = category,
        def = PolyColor.rgba(0, 0, 0, 255),
    )

    private val blending = root.accordion(
        id = "${key}_blending",
        title = "Vanilla Blending",
        description = "Inverts the colors behind the crosshair, like the vanilla one, so it shows up on any background.",
        category = category,
        def = true,
    )
    val blendingEnabled: Property<Boolean> = blending.second
    val blendingColor: Property<PolyColor> = blending.first.color(
        id = "color",
        title = "Blending Color",
        category = category,
        def = PolyColor.rgba(255, 255, 255, 255),
    )

    private val health = root.accordion(
        id = "${key}_health",
        title = "Health Color Shift",
        description = "Recolors the crosshair when your health is low.",
        category = category,
    )
    val healthEnabled: Property<Boolean> = health.second
    val healthThreshold: Property<Float> = health.first.slider(
        id = "threshold",
        title = "Health Threshold",
        description = "The health at or below which the crosshair changes color. 20 is full health.",
        category = category,
        min = 1f, max = 20f, def = 6f, step = 1f,
    )
    val healthColor: Property<PolyColor> = health.first.color(
        id = "color",
        title = "Color",
        category = category,
        def = PolyColor.rgba(255, 60, 60, 255),
    )

    private val bow = if (indicator) null else root.accordion(
        id = "${key}_bow",
        title = "Dynamic Bow",
        description = "Spreads the crosshair apart when you start drawing a bow and closes it as the shot charges.",
        category = category,
    )
    val bowEnabled: Property<Boolean>? = bow?.second
    val bowScale: Property<Float>? = bow?.first?.slider(
        id = "animation_scale",
        title = "Animation Scale",
        description = "How far the crosshair spreads apart.",
        category = category,
        min = 1f, max = 10f, def = 3f, step = 0.01f,
    )

    //? if >=1.21 {
    private val attack = if (indicator) null else root.accordion(
        id = "${key}_attack",
        title = "Dynamic Attack",
        description = "Spreads the crosshair apart after you attack and closes it as the cooldown recovers.",
        category = category,
    )
    val attackEnabled: Property<Boolean>? = attack?.second
    val attackScale: Property<Float>? = attack?.first?.slider(
        id = "animation_scale",
        title = "Animation Scale",
        description = "How far the crosshair spreads apart.",
        category = category,
        min = 1f, max = 10f, def = 3f, step = 0.01f,
    )
    //?}

    private val scale = root.accordion(
        id = "${key}_scale",
        title = "Use Custom Scale",
        description = "Draws the crosshair at the scale you pick here, whatever the game's GUI scale is.",
        category = category,
    )
    val customScaleEnabled: Property<Boolean> = scale.second
    val customScale: Property<Float> = scale.first.slider(
        id = "scale",
        title = "Scale",
        description = "The GUI scale to draw the crosshair at. 2 matches the Normal GUI scale.",
        category = category,
        min = 0.5f, max = 6f, def = 2f, step = 0.5f,
    )

    val showInThirdPerson: Property<Boolean> = root.switch(
        id = "${key}_third_person",
        title = "Show in Third Person",
        description = "Shows the crosshair in third person too.",
        category = category,
        //? if >=1.21 {
        def = false,
        //?} else {
        /*def = true,
        *///?}
    )

    val visibleInSpectator: Property<Boolean> = root.switch(
        id = "${key}_spectator",
        title = "Always Visible in Spectator",
        description = "Shows the crosshair the whole time you're spectating. Vanilla only shows it when you look at something you can interact with.",
        category = category,
        def = false,
    )

    val grid: GridSize get() = GridSize.of(gridSize.get() ?: GridSize.ONE.ordinal)

    fun pixels(): CrosshairPixels = CrosshairPixels.load(image.get().orEmpty(), grid.cells, resizeStretch.get() == true)

    fun circles(): CrosshairCircles = CrosshairCircles.load(circles.get().orEmpty(), grid.cells, resizeStretch.get() == true)

    fun write(pixels: CrosshairPixels, circles: CrosshairCircles) {
        image.set(pixels.toBase64())
        this.circles.set(circles.encode())
        revision++
    }

    private val shared: Map<String, Property<*>> = buildMap {
        fun add(name: String, property: Property<*>?) {
            if (property != null) put(name, property)
        }
        add("grid", gridSize)
        add("stretch", resizeStretch)
        add("image", image)
        add("circles", circles)
        add("offset_x", offsetX)
        add("offset_y", offsetY)
        add("orbit", orbitEnabled)
        add("orbit_radius", orbitRadius)
        add("attack_indicator_gap", attackIndicatorGap)
        add("outline", outlineEnabled)
        add("outline_thickness", outlineThickness)
        add("outline_color", outlineColor)
        add("blending", blendingEnabled)
        add("blending_color", blendingColor)
        add("health", healthEnabled)
        add("health_threshold", healthThreshold)
        add("health_color", healthColor)
        add("bow", bowEnabled)
        add("bow_scale", bowScale)
        //? if >=1.21 {
        add("attack", attackEnabled)
        add("attack_scale", attackScale)
        //?}
        add("scale", customScaleEnabled)
        add("scale_value", customScale)
        add("third_person", showInThirdPerson)
        add("spectator", visibleInSpectator)
    }

    fun copyFrom(other: CrosshairProfile) {
        if (other === this) return
        for ((name, property) in shared) {
            other.shared[name]?.let { property.setAny(it.get()) }
        }
        revision++
        ModConfig.save()
    }

    fun export(): String {
        val json = JsonObject()
        for ((name, property) in shared) {
            when (val value = property.get()) {
                is Boolean -> json.addProperty(name, value)
                is Number -> json.addProperty(name, value)
                is String -> json.addProperty(name, value)
                is PolyColor -> json.add(name, JsonArray().apply {
                    add(JsonPrimitive(value.rawArgb))
                    add(JsonPrimitive(value.chroma))
                    add(JsonPrimitive(value.chromaSpeed))
                })
            }
        }
        val bytes = ByteArrayOutputStream()
        GZIPOutputStream(bytes).use { it.write(json.toString().toByteArray(Charsets.UTF_8)) }
        return SHARE_PREFIX + Base64.getEncoder().encodeToString(bytes.toByteArray())
    }

    fun import(code: String): Boolean {
        val json = decodeShare(code) ?: return false
        val image = runCatching { json.get("image")?.asString }.getOrNull() ?: return false
        if (CrosshairPixels.fromBase64(image) == null) return false
        for ((name, property) in shared) {
            val element = json.get(name) ?: continue
            runCatching {
                when (property.get()) {
                    is Boolean -> element.asBoolean
                    is Int -> element.asInt
                    is Float -> element.asFloat
                    is String -> element.asString
                    is PolyColor -> element.asJsonArray.let { PolyColor(it[0].asInt, it[1].asBoolean, it[2].asFloat) }
                    else -> null
                }
            }.getOrNull()?.let(property::setAny)
        }
        revision++
        ModConfig.save()
        return true
    }
}

private const val SHARE_PREFIX = "PolyCrosshair-"

@Suppress("UNCHECKED_CAST")
private fun Property<*>.setAny(value: Any?) {
    (this as Property<Any?>).set(value)
}

@Suppress("DEPRECATION")
private fun decodeShare(code: String): JsonObject? = runCatching {
    val bytes = Base64.getDecoder().decode(code.trim().removePrefix(SHARE_PREFIX))
    val text = GZIPInputStream(bytes.inputStream()).use { String(it.readBytes(), Charsets.UTF_8) }
    JsonParser().parse(text).asJsonObject
}.getOrNull()
