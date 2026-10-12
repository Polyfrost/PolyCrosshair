package org.polyfrost.crosshair.ui

import org.polyfrost.crosshair.config.CrosshairPixels
import org.polyfrost.crosshair.config.MirrorMode

internal class EditorState(var pixels: CrosshairPixels) {

    private val undoStack = ArrayDeque<IntArray>()
    private val redoStack = ArrayDeque<IntArray>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun snapshot() {
        undoStack.addLast(pixels.argb.copyOf())
        if (undoStack.size > MAX_HISTORY) undoStack.removeFirst()
        redoStack.clear()
    }

    fun undo(): Boolean {
        val previous = undoStack.removeLastOrNull() ?: return false
        redoStack.addLast(pixels.argb.copyOf())
        previous.copyInto(pixels.argb)
        return true
    }

    fun redo(): Boolean {
        val next = redoStack.removeLastOrNull() ?: return false
        undoStack.addLast(pixels.argb.copyOf())
        next.copyInto(pixels.argb)
        return true
    }

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
