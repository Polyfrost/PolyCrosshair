package org.polyfrost.crosshair.config

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.color
import org.polyfrost.oneconfig.api.config.v1.enableToggle
import org.polyfrost.oneconfig.api.config.v1.group
import org.polyfrost.oneconfig.api.config.v1.slider
import org.polyfrost.oneconfig.api.config.v1.switch

class VanillaCrosshair(root: Tree) {

    val enabled: Property<Boolean> = root.switch(
        id = "vanilla_enabled",
        title = "Customize Vanilla Crosshair",
        description = "Applies these settings to the vanilla crosshair whenever it's showing instead of your custom one.",
        category = CATEGORY,
        default = false,
    )

    val fixCentering: Property<Boolean> = root.switch(
        id = "vanilla_centered",
        title = "Fix Centering",
        description = "Centers the crosshair on the screen's exact middle pixel. Vanilla can sit half a pixel off depending on window size and GUI scale.",
        category = CATEGORY,
        default = true,
    )

    private val blending = root.group(
        id = "vanilla_blending",
        title = "Vanilla Blending",
        description = "Inverts the colors behind the crosshair so it shows up on any background.",
        category = CATEGORY,
        enableToggle = true,
        enabledByDefault = true,
    ) {}
    val blendingEnabled: Property<Boolean> = blending.enableToggle!!
    val blendingColor: Property<PolyColor> = blending.color(
        id = "color",
        title = "Blending Color",
        category = CATEGORY,
        default = PolyColor.rgba(255, 255, 255, 255),
    )

    val color: Property<PolyColor> = root.color(
        id = "vanilla_color",
        title = "Color",
        description = "The crosshair's color while Vanilla Blending is off.",
        category = CATEGORY,
        default = PolyColor.rgba(255, 255, 255, 255),
    )

    private val scale = root.group(
        id = "vanilla_scale",
        title = "Use Custom Scale",
        description = "Draws the crosshair at the scale you pick here, whatever the game's GUI scale is.",
        category = CATEGORY,
        enableToggle = true,
        enabledByDefault = false,
    ) {}
    val customScaleEnabled: Property<Boolean> = scale.enableToggle!!
    val customScale: Property<Float> = scale.slider(
        id = "scale",
        title = "Scale",
        description = "The GUI scale to draw the crosshair at. 2 matches the Normal GUI scale.",
        category = CATEGORY,
        min = 0.5f, max = 6f, default = 2f, step = 0.5f,
    )

    val showInThirdPerson: Property<Boolean> = root.switch(
        id = "vanilla_third_person",
        title = "Show in Third Person",
        description = "Shows the crosshair in third person too.",
        category = CATEGORY,
        //? if >=1.21 {
        default = false,
        //?} else {
        /*default = true,
        *///?}
    )

    val visibleInSpectator: Property<Boolean> = root.switch(
        id = "vanilla_spectator",
        title = "Always Visible in Spectator",
        description = "Shows the crosshair the whole time you're spectating. Vanilla only shows it when you look at something you can interact with.",
        category = CATEGORY,
        default = false,
    )
}

private const val CATEGORY = "General"
