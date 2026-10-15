package org.polyfrost.crosshair.config

//? if >=1.21 {
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree

class AttackIndicator(root: Tree) {

    val enabled: Property<Boolean> = root.switch(
        id = "attack_indicator_enabled",
        title = "Customize Attack Indicator",
        description = "Replaces the vanilla crosshair attack indicator with this one. The attack cooldown still follows the vanilla Attack Indicator setting.",
        category = CATEGORY,
    )

    private val blending = root.accordion(
        id = "attack_indicator_blending",
        title = "Vanilla Blending",
        description = "Inverts the colors behind the indicator so it shows up on any background.",
        category = CATEGORY,
        def = true,
    )
    val blendingEnabled: Property<Boolean> = blending.second
    val blendingColor: Property<PolyColor> = blending.first.color(
        id = "color",
        title = "Blending Color",
        category = CATEGORY,
        def = PolyColor.rgba(255, 255, 255, 255),
    )

    val color: Property<PolyColor> = root.color(
        id = "attack_indicator_color",
        title = "Color",
        description = "The indicator's color while Vanilla Blending and Charge Colors are off.",
        category = CATEGORY,
        def = PolyColor.rgba(255, 255, 255, 255),
    )

    private val chargeColors = root.accordion(
        id = "attack_indicator_charge_colors",
        title = "Charge Colors",
        description = "Colors the indicator one way while it fills up and another once it's full.",
        category = CATEGORY,
    )
    val chargeColorsEnabled: Property<Boolean> = chargeColors.second
    val chargingColor: Property<PolyColor> = chargeColors.first.color(
        id = "charging",
        title = "Charging Color",
        category = CATEGORY,
        def = PolyColor.rgba(255, 85, 85, 255),
    )
    val readyColor: Property<PolyColor> = chargeColors.first.color(
        id = "ready",
        title = "Ready Color",
        category = CATEGORY,
        def = PolyColor.rgba(85, 255, 85, 255),
    )
    val chargeColorsWithBlending: Property<Boolean> = chargeColors.first.switch(
        "blending", "Use With Vanilla Blending",
        description = "Uses these colors instead of the Blending Color while Vanilla Blending is on.",
        category = CATEGORY,
    )

    val offsetX: Property<Float> = root.slider(
        id = "attack_indicator_offset_x",
        title = "Offset X",
        description = "How far right of its vanilla spot the indicator is drawn, in GUI pixels.",
        category = CATEGORY,
        min = -64f, max = 64f, def = 0f, step = 1f,
    )

    val offsetY: Property<Float> = root.slider(
        id = "attack_indicator_offset_y",
        title = "Offset Y",
        description = "How far below its vanilla spot the indicator is drawn, in GUI pixels.",
        category = CATEGORY,
        min = -64f, max = 64f, def = 0f, step = 1f,
    )

    private val attack = root.accordion(
        id = "attack_indicator_attack",
        title = "Attack Cooldown",
        description = "Shows the attack cooldown for the item types picked here.",
        category = CATEGORY,
        def = true,
    )
    val attackEnabled: Property<Boolean> = attack.second
    val attackSwords: Property<Boolean> = attack.first.switch("swords", "Swords", category = CATEGORY, def = true)
    val attackAxes: Property<Boolean> = attack.first.switch("axes", "Axes", category = CATEGORY, def = true)
    val attackTools: Property<Boolean> = attack.first.switch(
        "tools", "Tools",
        description = "Pickaxes, shovels and hoes.",
        category = CATEGORY, def = true,
    )
    val attackHeavy: Property<Boolean> = attack.first.switch(
        "heavy", "Tridents, Maces and Spears",
        category = CATEGORY, def = true,
    )
    val attackOther: Property<Boolean> = attack.first.switch(
        "other", "Other Items",
        description = "Everything else, including an empty hand.",
        category = CATEGORY, def = true,
    )

    val sleep: Property<Boolean> = root.switch(
        id = "attack_indicator_sleep",
        title = "Sleep Progress",
        description = "Fills up while you fall asleep in a bed.",
        category = CATEGORY,
    )

    val blockBreaking: Property<Boolean> = root.switch(
        id = "attack_indicator_block_breaking",
        title = "Block Breaking",
        description = "Fills up as the block you're mining breaks.",
        category = CATEGORY,
    )

    val itemCooldown: Property<Boolean> = root.switch(
        id = "attack_indicator_item_cooldown",
        title = "Item Cooldown",
        description = "Shows the cooldown of held items like ender pearls, chorus fruit and wind charges.",
        category = CATEGORY,
    )

    val bowCharge: Property<Boolean> = root.switch(
        id = "attack_indicator_bow_charge",
        title = "Bow Charge",
        description = "Fills up as you draw a bow or load a crossbow.",
        category = CATEGORY,
    )

    val shieldCooldown: Property<Boolean> = root.switch(
        id = "attack_indicator_shield_cooldown",
        title = "Shield Cooldown",
        description = "Shows how long your shield stays disabled after an axe hit.",
        category = CATEGORY,
    )

    val consumables: Property<Boolean> = root.switch(
        id = "attack_indicator_consumables",
        title = "Consumables",
        description = "Fills up while you eat or drink.",
        category = CATEGORY,
    )

    val storage: Property<Boolean> = root.switch(
        id = "attack_indicator_storage",
        title = "Storage Capacity",
        description = "Shows how full the bundle or shulker box you're holding is.",
        category = CATEGORY,
    )
}

private const val CATEGORY = "Attack Indicator"
//?}
