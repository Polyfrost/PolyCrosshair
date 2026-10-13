package org.polyfrost.crosshair.ui

import org.polyfrost.crosshair.config.Circle
import org.polyfrost.crosshair.config.CrosshairCircles
import org.polyfrost.crosshair.config.CrosshairPixels
import org.polyfrost.crosshair.config.Line
import org.polyfrost.crosshair.config.MirrorMode
import org.polyfrost.crosshair.config.VectorShape
import kotlin.math.roundToInt

internal class EditorState(var pixels: CrosshairPixels, var circles: CrosshairCircles) {

    private class Snapshot(val argb: IntArray, val circles: List<VectorShape>)

    private val undoStack = ArrayDeque<Snapshot>()
    private val redoStack = ArrayDeque<Snapshot>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    private fun current() = Snapshot(pixels.argb.copyOf(), circles.list.toList())

    private fun restore(snapshot: Snapshot) {
        snapshot.argb.copyInto(pixels.argb)
        circles.list.clear()
        circles.list.addAll(snapshot.circles)
    }

    fun snapshot() {
        undoStack.addLast(current())
        if (undoStack.size > MAX_HISTORY) undoStack.removeFirst()
        redoStack.clear()
    }

    fun undo(): Boolean {
        val previous = undoStack.removeLastOrNull() ?: return false
        redoStack.addLast(current())
        restore(previous)
        return true
    }

    fun redo(): Boolean {
        val next = redoStack.removeLastOrNull() ?: return false
        undoStack.addLast(current())
        restore(next)
        return true
    }

    fun clear() {
        pixels.clear()
        circles.list.clear()
    }

    fun isEmpty(): Boolean = pixels.isEmpty() && circles.isEmpty()

    fun paint(x: Int, y: Int, color: Int, mirror: MirrorMode) {
        val last = pixels.size - 1
        pixels[x, y] = color
        when (mirror) {
            MirrorMode.NONE -> Unit
            MirrorMode.HORIZONTAL -> pixels[last - x, y] = color
            MirrorMode.VERTICAL -> pixels[x, last - y] = color
            MirrorMode.QUADRANT -> {
                pixels[last - x, y] = color
                pixels[x, last - y] = color
                pixels[last - x, last - y] = color
            }
        }
    }

    private companion object {
        const val MAX_HISTORY = 128
    }
}

internal fun mirrorShape(shape: VectorShape, mirror: MirrorMode, size: Int): List<VectorShape> {
    val edge = size.toFloat()
    val all = when (mirror) {
        MirrorMode.NONE -> listOf(shape)
        MirrorMode.HORIZONTAL -> listOf(shape, shape.flippedX(edge))
        MirrorMode.VERTICAL -> listOf(shape, shape.flippedY(edge))
        MirrorMode.QUADRANT -> listOf(shape, shape.flippedX(edge), shape.flippedY(edge), shape.flippedX(edge).flippedY(edge))
    }
    return all.distinctBy(::shapeKey)
}

internal fun removeMirrored(list: MutableList<VectorShape>, shape: VectorShape, mirror: MirrorMode, size: Int) {
    val keys = mirrorShape(shape, mirror, size).mapTo(HashSet(), ::shapeKey)
    list.removeAll { shapeKey(it) in keys }
}

private fun shapeKey(shape: VectorShape): Any {
    fun q(v: Float) = (v * 1000f).roundToInt()
    return when (shape) {
        is Circle -> listOf(q(shape.x), q(shape.y), q(shape.radius), q(shape.thickness), shape.color)
        is Line -> listOf(setOf(q(shape.x1) to q(shape.y1), q(shape.x2) to q(shape.y2)), q(shape.thickness), shape.color)
    }
}
