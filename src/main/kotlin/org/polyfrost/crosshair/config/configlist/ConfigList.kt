package org.polyfrost.crosshair.config.configlist

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.core.ConfigUtils
import cc.polyfrost.oneconfig.config.elements.BasicOption
import cc.polyfrost.oneconfig.config.elements.OptionPage
import java.util.*

abstract class ConfigList<T> : ArrayList<T>() {
    var selectedProfile: T? = null

    abstract fun newConfig(): T
    abstract fun getName(profile: T): String
    abstract fun onSelected(profile: T)
    open fun postInitOptions(entry: ConfigEntry<T>, options: List<BasicOption>) {}

    fun addOptionTo(config: Config, page: OptionPage, description: String = "", category: String = "General", subcategory: String = ""): ConfigListOption<T> {
        val option = ConfigListOption(this, config, description, category, subcategory)
        ConfigUtils.getSubCategory(page, category, subcategory).options.add(option)
        return option
    }
}