package org.polyfrost.crosshair.config

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.crosshair.ui.CrosshairEditor
import org.polyfrost.oneconfig.api.config.v1.MetaKeys
import org.polyfrost.oneconfig.api.config.v1.Properties
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.ShareCodeResult
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer
import org.polyfrost.oneconfig.api.config.v1.applyShareCode
import org.polyfrost.oneconfig.api.config.v1.color
import org.polyfrost.oneconfig.api.config.v1.button
import org.polyfrost.oneconfig.api.config.v1.dropdown
import org.polyfrost.oneconfig.api.config.v1.enableToggle
import org.polyfrost.oneconfig.api.config.v1.group
import org.polyfrost.oneconfig.api.config.v1.hidden
import org.polyfrost.oneconfig.api.config.v1.set
import org.polyfrost.oneconfig.api.config.v1.slider
import org.polyfrost.oneconfig.api.config.v1.switch
import org.polyfrost.oneconfig.api.config.v1.toShareCode
import java.util.Base64
import java.util.function.Predicate
import java.util.zip.GZIPInputStream

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

    val enabled: Property<Boolean> = root.switch(
        id = "${key}_enabled",
        title = "Use $category Crosshair",
        description = enabledDescription,
        category = category,
        default = enabledDefault,
    )

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

    val image: Property<String> = Properties.simple("${key}_image", "Crosshair Editor", null, defaultImage, String::class.java).also {
        it[MetaKeys.VISUALIZER] = Visualizer { prop -> CrosshairEditor(this, prop) }
        it[MetaKeys.CATEGORY] = category
        it[MetaKeys.HIDE_LABEL] = true
        root.put(it)
    }

    val circles: Property<String> = root.hidden("${key}_circles", "")

    val rotation: Property<Float> = root.slider(
        id = "${key}_rotation",
        title = "Rotation",
        description = "Rotates the crosshair clockwise around its center.",
        category = category,
        min = 0f, max = 360f, default = 0f, step = 1f, unit = "°",
    )

    val offsetX: Property<Float>? = if (indicator) root.slider(
        id = "${key}_offset_x",
        title = "Offset X",
        description = "How far right of the crosshair this is drawn, in crosshair pixels.",
        category = category,
        min = -32f, max = 32f, default = 0f, step = 1f,
    ) else null

    val offsetY: Property<Float>? = if (indicator) root.slider(
        id = "${key}_offset_y",
        title = "Offset Y",
        description = "How far below the crosshair this is drawn, in crosshair pixels.",
        category = category,
        min = -32f, max = 32f, default = 11f, step = 1f,
    ) else null

    private val orbit = if (indicator) root.group(
        id = "${key}_orbit",
        title = "Follow Pitch",
        description = "Swings this around the right side of the crosshair as you look up and down: level puts it to the right, 30° down puts it above, 30° up puts it below. Replaces Offset X and Offset Y while on.",
        category = category,
        enableToggle = true,
        enabledByDefault = true,
    ) {} else null
    val orbitEnabled: Property<Boolean>? = orbit?.enableToggle
    val orbitRadius: Property<Float>? = orbit?.slider(
        id = "radius",
        title = "Radius",
        description = "How far from the crosshair this circles, in crosshair pixels.",
        category = category,
        min = 0f, max = 32f, default = 11f, step = 1f,
    )

    val attackIndicatorGap: Property<Float>? = if (indicator) root.slider(
        id = "${key}_attack_indicator_gap",
        title = "Attack Indicator Gap",
        description = "When this overlaps the vanilla attack indicator, the indicator moves down to sit this far below it.",
        category = category,
        min = -16f, max = 16f, default = -2f, step = 1f,
    ) else null

    val gridSize: Property<Int> = root.dropdown(
        id = "${key}_grid_size",
        title = "Grid Size",
        description = "The size of the drawing canvas, and how large the crosshair is on screen.",
        category = category,
        options = GridSize.LABELS.asList(),
        default = GridSize.ONE.ordinal,
    )

    val resizeStretch: Property<Boolean> = root.switch(
        id = "${key}_resize_stretch",
        title = "Grid Resize Stretch",
        description = "Stretches the crosshair to fill the new canvas when the grid size changes. Turn this off to keep its size and re-center it.",
        category = category,
        default = true,
    )

    private val outline = root.group(
        id = "${key}_outline",
        title = "Outline",
        description = "Draws a border around the crosshair.",
        category = category,
        enableToggle = true,
        enabledByDefault = false,
    ) {}
    val outlineEnabled: Property<Boolean> = outline.enableToggle!!
    val outlineThickness: Property<Float> = outline.slider(
        id = "thickness",
        title = "Outline Thickness",
        description = "How far the outline extends, in crosshair pixels.",
        category = category,
        min = 0f, max = 1f, default = 0.5f, step = 0.01f,
    )
    val outlineColor: Property<PolyColor> = outline.color(
        id = "color",
        title = "Outline Color",
        category = category,
        default = PolyColor.rgba(0, 0, 0, 255),
    )

    private val blending = root.group(
        id = "${key}_blending",
        title = "Vanilla Blending",
        description = "Inverts the colors behind the crosshair, like the vanilla one, so it shows up on any background.",
        category = category,
        enableToggle = true,
        enabledByDefault = true,
    ) {}
    val blendingEnabled: Property<Boolean> = blending.enableToggle!!
    val blendingColor: Property<PolyColor> = blending.color(
        id = "color",
        title = "Blending Color",
        category = category,
        default = PolyColor.rgba(255, 255, 255, 255),
    )

    private val health = root.group(
        id = "${key}_health",
        title = "Health Color Shift",
        description = "Recolors the crosshair when your health is low.",
        category = category,
        enableToggle = true,
        enabledByDefault = false,
    ) {}
    val healthEnabled: Property<Boolean> = health.enableToggle!!
    val healthThreshold: Property<Float> = health.slider(
        id = "threshold",
        title = "Health Threshold",
        description = "The health at or below which the crosshair changes color. 20 is full health.",
        category = category,
        min = 1f, max = 20f, default = 6f, step = 1f,
    )
    val healthColor: Property<PolyColor> = health.color(
        id = "color",
        title = "Color",
        category = category,
        default = PolyColor.rgba(255, 60, 60, 255),
    )

    private val bow = if (indicator) null else root.group(
        id = "${key}_bow",
        title = "Dynamic Bow",
        description = "Spreads the crosshair apart when you start drawing a bow and closes it as the shot charges.",
        category = category,
        enableToggle = true,
        enabledByDefault = false,
    ) {}
    val bowEnabled: Property<Boolean>? = bow?.enableToggle
    val bowScale: Property<Float>? = bow?.slider(
        id = "animation_scale",
        title = "Animation Scale",
        description = "How far the crosshair spreads apart.",
        category = category,
        min = 1f, max = 10f, default = 3f, step = 0.01f,
    )

    //? if >=1.21 {
    private val attack = if (indicator) null else root.group(
        id = "${key}_attack",
        title = "Dynamic Attack",
        description = "Spreads the crosshair apart after you attack and closes it as the cooldown recovers.",
        category = category,
        enableToggle = true,
        enabledByDefault = false,
    ) {}
    val attackEnabled: Property<Boolean>? = attack?.enableToggle
    val attackScale: Property<Float>? = attack?.slider(
        id = "animation_scale",
        title = "Animation Scale",
        description = "How far the crosshair spreads apart.",
        category = category,
        min = 1f, max = 10f, default = 3f, step = 0.01f,
    )
    //?}

    private val scale = root.group(
        id = "${key}_scale",
        title = "Use Custom Scale",
        description = "Draws the crosshair at the scale you pick here, whatever the game's GUI scale is.",
        category = category,
        enableToggle = true,
        enabledByDefault = false,
    ) {}
    val customScaleEnabled: Property<Boolean> = scale.enableToggle!!
    val customScale: Property<Float> = scale.slider(
        id = "scale",
        title = "Scale",
        description = "The GUI scale to draw the crosshair at. 2 matches the Normal GUI scale.",
        category = category,
        min = 0.5f, max = 6f, default = 2f, step = 0.5f,
    )
    val smoothScale: Property<Boolean> = scale.switch(
        id = "smooth",
        title = "Smooth Scaling",
        description = "Draws the crosshair at exactly the scale you pick. Turn this off to round its size to whole GUI pixels.",
        category = category,
        default = false,
    )

    val showInThirdPerson: Property<Boolean> = root.switch(
        id = "${key}_third_person",
        title = "Show in Third Person",
        description = "Shows the crosshair in third person too.",
        category = category,
        //? if >=1.21 {
        default = false,
        //?} else {
        /*default = true,
        *///?}
    )

    val visibleInSpectator: Property<Boolean> = root.switch(
        id = "${key}_spectator",
        title = "Always Visible in Spectator",
        description = "Shows the crosshair the whole time you're spectating. Vanilla only shows it when you look at something you can interact with.",
        category = category,
        default = false,
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
        add("rotation", rotation)
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
        add("scale_smooth", smoothScale)
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

    private val shareTree: Tree = Tree.tree("${key}_share").also { tree ->
        for ((name, property) in shared) {
            tree.put(if (property === image) image.alias(name) { CrosshairPixels.fromBase64(it.orEmpty()) != null } else property.alias(name))
        }
    }

    fun export(): String = shareTree.toShareCode(SHARE_PREFIX)

    fun import(code: String): Boolean {
        val trimmed = code.trim()
        if (trimmed.startsWith(LUNAR_PREFIX)) return importLunar(trimmed)
        if (trimmed.startsWith(LEGACY_SHARE_PREFIX)) return importLegacy(trimmed)
        val checked = shareTree.applyShareCode(trimmed, SHARE_PREFIX) { it.id == "image" }
        if (checked !is ShareCodeResult.Success || "image" !in checked.applied) return false
        shareTree.applyShareCode(trimmed, SHARE_PREFIX)
        revision++
        ModConfig.save()
        return true
    }

    private fun importLegacy(code: String): Boolean {
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

    private fun importLunar(code: String): Boolean {
        val pixels = CrosshairPixels.fromLunar(code) ?: return false
        val grid = GridSize.fitting(pixels.size)
        gridSize.set(grid.ordinal)
        write(pixels.resized(grid.cells, stretch = false), CrosshairCircles(grid.cells))
        ModConfig.save()
        return true
    }
}

private const val SHARE_PREFIX = "PolyCrosshair2-"
private const val LEGACY_SHARE_PREFIX = "PolyCrosshair-"
private const val LUNAR_PREFIX = "LCCH-"

@Suppress("UNCHECKED_CAST")
private fun Property<*>.setAny(value: Any?) {
    (this as Property<Any?>).set(value)
}

private fun <T> Property<T>.alias(name: String, accept: (T?) -> Boolean = { true }): Property<T> {
    @Suppress("UNCHECKED_CAST")
    val alias = Properties.functional({ get() as T }, { if (accept(it)) set(it) }, name, null, null, type as Class<T>)
    alias.addCallback(Predicate<T> { !accept(it) })
    return alias
}

@Suppress("DEPRECATION")
private fun decodeShare(code: String): JsonObject? = runCatching {
    val bytes = Base64.getDecoder().decode(code.trim().removePrefix(LEGACY_SHARE_PREFIX))
    val text = GZIPInputStream(bytes.inputStream()).use { String(it.readBytes(), Charsets.UTF_8) }
    JsonParser().parse(text).asJsonObject
}.getOrNull()
