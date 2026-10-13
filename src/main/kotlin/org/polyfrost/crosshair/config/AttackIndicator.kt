package org.polyfrost.crosshair.config

//? if >=1.21 {
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree

class AttackIndicator(root: Tree) {

    val enabled: Property<Boolean> = root.switch(
        id = "attack_indicator_enabled",
        title = "Customize Attack Indicator",
        description = "Replaces the vanilla crosshair attack indicator with this one. The attack cooldown still follows the vanilla Attack Indicator setting.",
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
