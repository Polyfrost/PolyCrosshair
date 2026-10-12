package org.polyfrost.crosshair.ui

import org.polyfrost.crosshair.config.Circle
import org.polyfrost.crosshair.config.CrosshairCircles
import org.polyfrost.crosshair.config.CrosshairPixels
import org.polyfrost.crosshair.config.MirrorMode

internal class EditorState(var pixels: CrosshairPixels, var circles: CrosshairCircles) {

    private class Snapshot(val argb: IntArray, val circles: List<Circle>)

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
