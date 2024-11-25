package org.polyfrost.crosshair.config

import org.polyfrost.oneconfig.api.config.v1.annotations.Option

@Option(display = CrosshairVisualizer::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@MustBeDocumented
annotation class Crosshairs(
    val category: String = "General",
    val subcategory: String = "General",
)
