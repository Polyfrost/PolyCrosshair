package org.polyfrost.crosshair.config

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.color
import org.polyfrost.oneconfig.api.config.v1.dropdown
import org.polyfrost.oneconfig.api.config.v1.enableToggle
import org.polyfrost.oneconfig.api.config.v1.group
import org.polyfrost.oneconfig.api.config.v1.slider
import org.polyfrost.oneconfig.api.config.v1.switch

class DebugCrosshair(root: Tree) {

    val enabled: Property<Boolean> = root.switch(
        id = "debug_crosshair_enabled",
        title = "Customize Debug Crosshair",
        description = "Replaces the vanilla debug crosshair, the axis cross shown on the debug screen, with this one.",
        category = CATEGORY,
        default = false,
    )

    val displayMode: Property<Int> = root.dropdown(
        id = "debug_crosshair_display_mode",
        title = "Display Mode",
        description = "When to show the debug crosshair. Overlay shows it with the debug screen, Always shows it the whole time.",
        category = CATEGORY,
        options = DebugDisplayMode.LABELS.asList(),
        default = DebugDisplayMode.OVERLAY.ordinal,
    )

    val hideCrosshair: Property<Boolean> = root.switch(
        id = "debug_crosshair_hide_crosshair",
        title = "Hide Crosshair",
        description = "Hides the normal crosshair when the debug crosshair is visible.",
        category = CATEGORY,
        default = true,
    )

    val showInThirdPerson: Property<Boolean> = root.switch(
        id = "debug_crosshair_third_person",
        title = "Show in Third Person",
        description = "Shows the debug crosshair in third person too.",
        category = CATEGORY,
        default = false,
    )

    val perspective: Property<Boolean> = root.switch(
        id = "debug_crosshair_perspective",
        title = "3D Perspective",
        description = "Draws the axes like vanilla: they grow with your GUI scale and window height, and axes pointing at you look longer. Turn this off to measure Length in screen pixels.",
        category = CATEGORY,
        default = true,
    )

    val axisWidth: Property<Float> = root.slider(
        id = "debug_crosshair_axis_width",
        title = "Axis Width",
        description = "How thick each axis is, in screen pixels.",
        category = CATEGORY,
        min = 1f, max = 10f, default = 2f, step = 1f,
    )

    val outlineWidth: Property<Float> = root.slider(
        id = "debug_crosshair_outline_width",
        title = "Axis Outline Width",
        description = "How thick each axis outline is, in screen pixels.",
        category = CATEGORY,
        min = 1f, max = 20f, default = 4f, step = 1f,
    )

    val xAxis = DebugAxis(root, "x", "X", PolyColor.rgba(255, 0, 0, 255))

    val yAxis = DebugAxis(root, "y", "Y", PolyColor.rgba(0, 255, 0, 255))

    val zAxis = DebugAxis(root, "z", "Z", PolyColor.rgba(127, 127, 255, 255))
}

class DebugAxis(root: Tree, key: String, name: String, defaultColor: PolyColor) {

    private val axis = root.group(
        id = "debug_crosshair_$key",
        title = "$name Axis",
        description = "Shows the $name axis.",
        category = CATEGORY,
        enableToggle = true,
        enabledByDefault = true,
    ) {}
    val enabled: Property<Boolean> = axis.enableToggle!!
    val length: Property<Float> = axis.slider(
        id = "length",
        title = "Length",
        description = "How long this axis is. With 3D Perspective on, 10 matches vanilla. With it off, this is in screen pixels.",
        category = CATEGORY,
        min = 1f, max = 50f, default = 10f, step = 1f,
    )
    val color: Property<PolyColor> = axis.color(
        id = "color",
        title = "Color",
        category = CATEGORY,
        default = defaultColor,
    )
    val outline: Property<Boolean> = axis.switch(
        id = "outline",
        title = "Outline",
        category = CATEGORY,
        default = true,
    )
    val outlineColor: Property<PolyColor> = axis.color(
        id = "outline_color",
        title = "Outline Color",
        category = CATEGORY,
        default = PolyColor.rgba(0, 0, 0, 255),
    )
}

private const val CATEGORY = "Debug Crosshair"
