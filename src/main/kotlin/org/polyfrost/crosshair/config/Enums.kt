package org.polyfrost.crosshair.config

enum class GridSize(val cells: Int, val label: String) {
    HALF(7, "0.5x"),
    ONE(15, "1x"),
    TWO(31, "2x"),
    THREE(63, "3x");

    companion object {
        val LABELS: Array<String> = entries.map { it.label }.toTypedArray()

        val MAX: Int = entries.maxOf { it.cells }

        fun of(ordinal: Int): GridSize = entries.getOrElse(ordinal) { TWO }

        fun fitting(cells: Int): GridSize = entries.firstOrNull { it.cells >= cells } ?: THREE
    }
}

enum class MirrorMode(val label: String) {
    NONE("None"),
    HORIZONTAL("Horizontal"),
    VERTICAL("Vertical"),
    QUADRANT("Quadrant");

    fun next(): MirrorMode = entries[(ordinal + 1) % entries.size]

    companion object {
        val LABELS: Array<String> = entries.map { it.label }.toTypedArray()

        fun of(ordinal: Int): MirrorMode = entries.getOrElse(ordinal) { NONE }
    }
}

enum class TargetKind {
    NORMAL,
    FRIENDLY,
    ENEMY,
}
