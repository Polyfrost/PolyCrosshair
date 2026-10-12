package org.polyfrost.crosshair.config

import org.polyfrost.compose.render.PolyColor
import org.polyfrost.crosshair.PolyCrosshair
import org.polyfrost.oneconfig.api.config.v1.Properties
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.Visualizer

private const val DEFAULT_SUBCATEGORY = "General"

internal fun Tree.subtree(id: String, title: String, description: String? = null, category: String): Tree {
    val child = Tree.tree(id)
    child.setTitle(title)
    child.description = description
    child.addMetadata("category", category)
    child.addMetadata("subcategory", DEFAULT_SUBCATEGORY)
    put(child)
    return child
}

internal fun Tree.accordion(
    id: String,
    title: String,
    description: String? = null,
    category: String,
    def: Boolean = false,
): Pair<Tree, Property<Boolean>> {
    val tree = subtree(id, title, description, category)
    val toggle = Properties.simple("enabled", title, description, def, Boolean::class.java)
    tree.put(toggle)
    return tree to toggle
}

internal fun Tree.switch(
    id: String,
    title: String,
    description: String? = null,
    category: String,
    def: Boolean = false,
): Property<Boolean> =
    option(id, title, description, def, Boolean::class.java, Visualizer.SwitchVisualizer(), category)

internal fun Tree.slider(
    id: String,
    title: String,
    description: String? = null,
    category: String,
    min: Float,
    max: Float,
    def: Float,
    step: Float? = null,
    unit: String? = null,
): Property<Float> =
    option(id, title, description, def, Float::class.java, Visualizer.SliderVisualizer(), category) {
        addMetadata("min", min)
        addMetadata("max", max)
        if (step != null) addMetadata("step", step)
        if (unit != null) addMetadata("unit", unit)
    }

internal fun Tree.dropdown(
    id: String,
    title: String,
    description: String? = null,
    category: String,
    options: Array<String>,
    def: Int = 0,
): Property<Int> =
    option(id, title, description, def, Int::class.java, Visualizer.DropdownVisualizer(), category) {
        addMetadata("options", options)
    }

internal fun Tree.color(
    id: String,
    title: String,
    description: String? = null,
    category: String,
    def: PolyColor,
): Property<PolyColor> =
    option(id, title, description, def, PolyColor::class.java, Visualizer.ColorVisualizer(), category)

internal fun Tree.button(
    id: String,
    title: String,
    description: String? = null,
    category: String,
    text: String,
    action: () -> Unit,
): Property<Void> {
    val p = Properties.dummy(id, title, description)
    p.addMetadata("visualizer", Visualizer.ButtonVisualizer())
    p.addMetadata("category", category)
    p.addMetadata("subcategory", DEFAULT_SUBCATEGORY)
    p.addMetadata("text", text)
    p.addMetadata("runnable", Runnable {
        runCatching(action).onFailure { PolyCrosshair.LOGGER.error("button '{}' failed", id, it) }
    })
    put(p)
    return p
}

internal fun <T : Any> Tree.custom(
    id: String,
    title: String,
    description: String? = null,
    category: String,
    def: T,
    type: Class<T>,
    visualizer: Visualizer,
): Property<T> = option(id, title, description, def, type, visualizer, category)

internal fun <T : Any> Tree.hidden(id: String, def: T, type: Class<T>): Property<T> {
    val p = Properties.simple(id, null, null, def, type)
    p.addMetadata("hidden", true)
    put(p)
    return p
}

private fun <T : Any> Tree.option(
    id: String,
    title: String,
    description: String?,
    def: T,
    type: Class<T>,
    visualizer: Visualizer?,
    category: String,
    extra: Property<T>.() -> Unit = {},
): Property<T> {
    val p = Properties.simple(id, title, description, def, type)
    p.extra()
    if (visualizer != null) p.addMetadata("visualizer", visualizer)
    p.addMetadata("category", category)
    p.addMetadata("subcategory", DEFAULT_SUBCATEGORY)
    put(p)
    return p
}
