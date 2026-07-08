package org.polyfrost.crosshair.config

class CrosshairEntry {
    var img: String = EMPTY_IMG

    var scale: Int = 100
    var rotation: Int = 0
    var offsetX: Int = 0
    var offsetY: Int = 0
    var centered: Boolean = false

    fun copyFrom(other: CrosshairEntry) {
        img = other.img
        scale = other.scale
        rotation = other.rotation
        offsetX = other.offsetX
        offsetY = other.offsetY
        centered = other.centered
    }

    fun copy(): CrosshairEntry = CrosshairEntry().also { it.copyFrom(this) }

    companion object {
        const val EMPTY_IMG =
            "iVBORw0KGgoAAAANSUhEUgAAAA8AAAAPCAYAAAA71pVKAAAAEUlEQVR42mNgGAWjYBQMIgAAA5MAAecADfkAAAAASUVORK5CYII="
    }
}
