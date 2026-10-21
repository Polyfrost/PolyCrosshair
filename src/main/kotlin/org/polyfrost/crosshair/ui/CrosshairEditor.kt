@file:OptIn(ExperimentalOneConfigUiApi::class)

package org.polyfrost.crosshair.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.crosshair.config.Circle
import org.polyfrost.crosshair.config.CrosshairCircles
import org.polyfrost.crosshair.config.CrosshairPixels
import org.polyfrost.crosshair.config.CrosshairProfile
import org.polyfrost.crosshair.config.Line
import org.polyfrost.crosshair.config.MirrorMode
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.crosshair.config.VectorShape
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.components.Chip
import org.polyfrost.oneconfig.api.ui.v1.components.ColorPickerModel
import org.polyfrost.oneconfig.api.ui.v1.components.ColorPickerPopup
import org.polyfrost.oneconfig.api.ui.v1.components.Icon
import org.polyfrost.oneconfig.api.ui.v1.components.Text
import org.polyfrost.oneconfig.api.ui.v1.components.colorToHex
import org.polyfrost.oneconfig.api.ui.v1.components.onClick
import org.polyfrost.oneconfig.api.ui.v1.components.rememberInteractionSource
import org.polyfrost.oneconfig.api.ui.v1.keybind.KeybindManager
import org.polyfrost.oneconfig.api.ui.v1.theme.OneConfigTheme
import java.util.function.BooleanSupplier
import java.util.function.Predicate
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private val CANVAS_SIZE = 304.dp
private val FAVORITE_SIZE = 96.dp
private val FAVORITE_GAP = 8.dp
private const val TRASH_ICON = "/assets/polycrosshair/trashcan.svg"

object CanvasHover {
    @JvmField
    @Volatile
    var owner: Any? = null
}

private const val DISABLED_ALPHA = 0.65f

@Composable
fun CrosshairEditor(profile: CrosshairProfile, prop: Property<*>) {
    val theme = OneConfigTheme.current

    var reloads by remember(prop) { mutableIntStateOf(0) }
    val writing = remember(prop) { booleanArrayOf(false) }
    DisposableEffect(prop) {
        val gridListener = Predicate<Int> { reloads++; false }
        val imageListener = Predicate<String> { if (!writing[0]) reloads++; false }
        profile.gridSize.addCallback(gridListener)
        profile.image.addCallback(imageListener)
        profile.circles.addCallback(imageListener)
        onDispose {
            profile.gridSize.removeCallback(gridListener)
            profile.image.removeCallback(imageListener)
            profile.circles.removeCallback(imageListener)
        }
    }
    val cells = remember(reloads) { profile.grid.cells }

    val state = remember(prop, reloads) { EditorState(profile.pixels(), profile.circles()) }
    var version by remember(state) { mutableIntStateOf(0) }
    var mirror by remember(prop) { mutableStateOf(MirrorMode.NONE) }
    var tool by remember(prop) { mutableStateOf(Tool.PEN) }
    var thickness by remember(prop) { mutableStateOf(1f) }
    var draft by remember(state) { mutableStateOf<VectorShape?>(null) }
    var guides by remember(prop) { mutableStateOf(true) }
    val pen = ModConfig.penColor.observe() ?: -0x1
    val favorites = remember(ModConfig.favorites.observe()) { ModConfig.favoriteList() }
    var helpOpen by remember(prop) { mutableStateOf(false) }
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    var shareStatus by remember(prop) { mutableStateOf<String?>(null) }
    LaunchedEffect(shareStatus) {
        if (shareStatus != null) {
            delay(SHARE_STATUS_MILLIS)
            shareStatus = null
        }
    }

    val interaction = rememberInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val focus = remember(prop) { FocusRequester() }

    fun persist() {
        writing[0] = true
        try {
            profile.write(state.pixels, state.circles)
        } finally {
            writing[0] = false
        }
        ModConfig.save()
    }

    fun mutate(block: () -> Unit) {
        block()
        version++
    }

    fun replace(entry: String) {
        val stretch = profile.resizeStretch.get() == true
        val (image, circles) = splitFavorite(entry)
        mutate {
            state.snapshot()
            state.pixels.copyFrom(CrosshairPixels.load(image, cells, stretch))
            state.circles.list.clear()
            state.circles.list.addAll(CrosshairCircles.load(circles, cells, stretch).list)
        }
        persist()
    }

    LaunchedEffect(hovered) {
        if (hovered) {
            CanvasHover.owner = prop
            runCatching { focus.requestFocus() }
        } else if (CanvasHover.owner === prop) {
            CanvasHover.owner = null
        }
    }
    DisposableEffect(prop) {
        val capture = BooleanSupplier { CanvasHover.owner === prop }
        KeybindManager.addInputCapture(capture)
        onDispose {
            KeybindManager.removeInputCapture(capture)
            if (CanvasHover.owner === prop) CanvasHover.owner = null
        }
    }

    Column(
        Modifier.fillMaxWidth().hoverable(interaction).padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(CANVAS_SIZE)
                    .clip(theme.backgroundShape)
                    .border(1.dp, theme.borderColor, theme.backgroundShape)
                    .pointerHoverIcon(PointerIcon.Crosshair)
                    .onKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                        when (event.key) {
                            Key.Delete -> {
                                mutate { state.snapshot(); state.clear() }
                                persist(); true
                            }
                            Key.S -> { guides = !guides; true }
                            Key.Z -> { if (state.undo()) { version++; persist() }; true }
                            Key.Y -> { if (state.redo()) { version++; persist() }; true }
                            Key.W -> { mirror = mirror.next(); true }
                            Key.C -> { tool = tool.next(); true }
                            else -> false
                        }
                    }
                    .focusRequester(focus)
                    .focusable()
                    .pointerInput(state, mirror, pen, tool, thickness) {
                        awaitEachGesture {
                            val cell = size.width / cells.toFloat()
                            var origin: Pair<Int, Int>? = null
                            var painted = false

                            fun paint(position: Offset, erase: Boolean, lockAxis: Boolean) {
                                var x = (position.x / cell).toInt()
                                var y = (position.y / cell).toInt()
                                val start = origin
                                if (lockAxis && start != null) {
                                    if (abs(x - start.first) >= abs(y - start.second)) y = start.second else x = start.first
                                }
                                if (x !in 0 until cells || y !in 0 until cells) return
                                if (origin == null) origin = x to y
                                if (!painted) {
                                    state.snapshot()
                                    painted = true
                                }
                                state.paint(x, y, if (erase) 0 else pen, mirror)
                            }

                            var press: PointerEvent
                            do {
                                press = awaitPointerEvent()
                            } while (press.type != PointerEventType.Press)
                            val down = press.changes.first()
                            val erasing = press.buttons.isSecondaryPressed

                            if (tool != Tool.PEN) {
                                down.consume()
                                val x = down.position.x / cell
                                val y = down.position.y / cell
                                if (erasing) {
                                    val hit = state.circles.list.lastOrNull { it.distanceTo(x, y) <= it.thickness / 2f + 0.5f }
                                    if (hit != null) {
                                        mutate { state.snapshot(); removeMirrored(state.circles.list, hit, mirror, cells) }
                                        persist()
                                    }
                                    do {
                                        val event = awaitPointerEvent()
                                        event.changes.forEach { it.consume() }
                                    } while (event.changes.any { it.pressed })
                                    return@awaitEachGesture
                                }
                                val cx = (x * 2f).roundToInt() / 2f
                                val cy = (y * 2f).roundToInt() / 2f
                                do {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.first()
                                    if (change.pressed) {
                                        var dx = change.position.x / cell - cx
                                        var dy = change.position.y / cell - cy
                                        draft = if (tool == Tool.CIRCLE) {
                                            Circle(cx, cy, sqrt(dx * dx + dy * dy), thickness, pen)
                                        } else {
                                            if (event.keyboardModifiers.isShiftPressed) {
                                                val angle = (atan2(dy, dx) / SNAP_ANGLE).roundToInt() * SNAP_ANGLE
                                                val length = sqrt(dx * dx + dy * dy)
                                                dx = cos(angle) * length
                                                dy = sin(angle) * length
                                            }
                                            Line(cx, cy, ((cx + dx) * 2f).roundToInt() / 2f, ((cy + dy) * 2f).roundToInt() / 2f, thickness, pen)
                                        }
                                        change.consume()
                                    }
                                } while (event.changes.any { it.pressed })
                                val made = draft
                                draft = null
                                val big = when (made) {
                                    is Circle -> made.radius >= MIN_SHAPE_SIZE
                                    is Line -> made.length >= MIN_SHAPE_SIZE
                                    null -> false
                                }
                                if (made != null && big) {
                                    mutate {
                                        state.snapshot()
                                        state.circles.list.addAll(mirrorShape(made, mirror, cells))
                                    }
                                    persist()
                                }
                                return@awaitEachGesture
                            }

                            mutate { paint(down.position, erasing, lockAxis = false) }
                            down.consume()

                            do {
                                val event = awaitPointerEvent()
                                val shift = event.keyboardModifiers.isShiftPressed
                                val erase = erasing || event.buttons.isSecondaryPressed
                                mutate {
                                    event.changes.forEach { change ->
                                        if (change.pressed) {
                                            paint(change.position, erase, shift)
                                            change.consume()
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            if (painted) persist()
                        }
                    }
            ) {
                val light = theme.chipBackground
                val dark = theme.componentBackground
                val guideColor = OneConfigTheme.accent.copy(alpha = 0.35f)
                val gridColor = theme.borderColor.copy(alpha = 0.25f)

                Canvas(Modifier.size(CANVAS_SIZE)) {
                    version
                    val cell = this.size.width / cells

                    for (y in 0 until cells) {
                        for (x in 0 until cells) {
                            val checker = if ((x + y) % 2 == 0) dark else light
                            drawRect(checker, Offset(x * cell, y * cell), Size(cell, cell))
                            val argb = state.pixels[x, y]
                            if (argb ushr 24 != 0) {
                                drawRect(Color(argb), Offset(x * cell, y * cell), Size(cell, cell))
                            }
                        }
                    }

                    drawShapes(state.circles.list, cell)
                    draft?.let { drawShapes(mirrorShape(it, mirror, cells), cell) }

                    if (cells <= 32) {
                        for (i in 1 until cells) {
                            val at = i * cell
                            drawLine(gridColor, Offset(at, 0f), Offset(at, this.size.height), 1f)
                            drawLine(gridColor, Offset(0f, at), Offset(this.size.width, at), 1f)
                        }
                    }

                    if (guides) {
                        val mid = this.size.width / 2f
                        drawLine(guideColor, Offset(mid, 0f), Offset(mid, this.size.height), 1.5f)
                        drawLine(guideColor, Offset(0f, mid), Offset(this.size.width, mid), 1.5f)
                        drawLine(guideColor, Offset.Zero, Offset(this.size.width, this.size.height), 1f)
                        drawLine(guideColor, Offset(this.size.width, 0f), Offset(0f, this.size.height), 1f)
                    }
                }

                if (helpOpen) {
                    ShortcutOverlay(Modifier.align(Alignment.BottomStart).padding(8.dp))
                }
            }


            Column(Modifier.width(IntrinsicSize.Max).height(CANVAS_SIZE), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EditorButton("Undo", enabled = state.canUndo) { if (state.undo()) { version++; persist() } }
                EditorButton("Redo", enabled = state.canRedo) { if (state.redo()) { version++; persist() } }
                EditorButton("Clear") {
                    mutate { state.snapshot(); state.clear() }
                    persist()
                }
                EditorButton("Reset") { replace(profile.defaultImage) }
                PenColor()
                EditorButton("Save") {
                    val current = favoriteOf(state.pixels, state.circles)
                    if (!state.isEmpty() && current !in favorites) ModConfig.setFavorites(favorites + current)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditorButton(if (shareStatus == COPIED) COPIED else "Copy", Modifier.weight(1f)) {
                        clipboard.setText(AnnotatedString(profile.export()))
                        shareStatus = COPIED
                    }
                    EditorButton(if (shareStatus == INVALID) INVALID else "Paste", Modifier.weight(1f)) {
                        val code = clipboard.getText()?.text
                        shareStatus = if (code != null && profile.import(code)) null else INVALID
                    }
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (helpOpen) OneConfigTheme.accent else theme.popupBackground, CircleShape)
                        .border(1.dp, theme.borderColor, CircleShape)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .onClick(rememberInteractionSource()) { helpOpen = !helpOpen },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("i", modifier = Modifier, color = if (helpOpen) theme.accentTextColor else theme.textColor, fontSize = 12.sp)
                }
            }

            BoxWithConstraints(Modifier.weight(1f)) {
                val perRow = ((maxWidth + FAVORITE_GAP) / (FAVORITE_SIZE + FAVORITE_GAP)).toInt().coerceAtLeast(1)
                Column(verticalArrangement = Arrangement.spacedBy(FAVORITE_GAP)) {
                    if (favorites.isEmpty()) {
                        Text("Saved crosshairs show up here.", modifier = Modifier, color = theme.textColorSecondary, fontSize = 12.sp)
                    }
                    favorites.chunked(perRow).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(FAVORITE_GAP)) {
                            row.forEach { favorite ->
                                key(favorite) {
                                    Favorite(
                                        favorite,
                                        onLoad = { replace(favorite) },
                                        onRemove = { ModConfig.setFavorites(favorites - favorite) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Tool", modifier = Modifier, color = theme.textColorSecondary, fontSize = 12.sp)
            Tool.entries.forEach { entry ->
                Chip(entry.label, selected = tool == entry) { tool = entry }
            }
            if (tool != Tool.PEN) {
                Spacer(Modifier.width(12.dp))
                Text("Thickness", modifier = Modifier, color = theme.textColorSecondary, fontSize = 12.sp)
                SHAPE_THICKNESSES.forEach { value ->
                    val label = if (value % 1f == 0f) value.toInt().toString() else value.toString()
                    Chip(label, selected = thickness == value) { thickness = value }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Mirror", modifier = Modifier, color = theme.textColorSecondary, fontSize = 12.sp)
            MirrorMode.entries.forEach { mode ->
                Chip(mode.label, selected = mirror == mode) { mirror = mode }
            }
        }
    }
}

@Composable
private fun <T> Property<T>.observe(): T? {
    var value by remember(this) { mutableStateOf(get()) }
    DisposableEffect(this) {
        val listener = Predicate<T> { value = it; false }
        addCallback(listener)
        onDispose { removeCallback(listener) }
    }
    return value
}

@Composable
private fun Favorite(entry: String, onLoad: () -> Unit, onRemove: () -> Unit) {
    val theme = OneConfigTheme.current
    val parts = remember(entry) { splitFavorite(entry) }
    val pixels = remember(entry) { CrosshairPixels.fromBase64(parts.first) } ?: return
    val circles = remember(entry) { CrosshairCircles.decode(parts.second) }
    val interaction = rememberInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    Box(
        Modifier
            .size(FAVORITE_SIZE)
            .clip(theme.backgroundShape)
            .background(theme.componentBackground, theme.backgroundShape)
            .border(1.dp, if (hovered) OneConfigTheme.accent else theme.borderColor, theme.backgroundShape)
            .pointerHoverIcon(PointerIcon.Hand)
            .onClick(interaction) { onLoad() }
    ) {
        Canvas(Modifier.fillMaxSize().padding(14.dp)) {
            val cell = size.width / pixels.size
            for (y in 0 until pixels.size) {
                for (x in 0 until pixels.size) {
                    val argb = pixels[x, y]
                    if (argb ushr 24 != 0) drawRect(Color(argb), Offset(x * cell, y * cell), Size(cell, cell))
                }
            }
            circles?.let { drawShapes(it.list, size.width / it.size) }
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .pointerHoverIcon(PointerIcon.Hand)
                .onClick(rememberInteractionSource()) { onRemove() }
                .padding(4.dp)
        ) {
            Icon(TRASH_ICON, color = theme.textColorSecondary, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun ShortcutOverlay(modifier: Modifier = Modifier) {
    val theme = OneConfigTheme.current
    Column(
        modifier
            .clip(theme.popupShape)
            .background(theme.popupBackground, theme.popupShape)
            .border(1.dp, theme.borderColor, theme.popupShape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SHORTCUTS.forEach { (keys, what) ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(keys, modifier = Modifier, color = theme.textColor, fontSize = 12.sp)
                Text(what, modifier = Modifier, color = theme.textColorSecondary, fontSize = 12.sp)
            }
        }
    }
}

private val SHORTCUTS = listOf(
    "Left click" to "Draw a pixel",
    "Right click" to "Erase a pixel",
    "Delete" to "Clear the canvas",
    "S" to "Toggle guides",
    "Shift (hold)" to "Lock to one direction",
    "Z" to "Undo",
    "Y" to "Redo",
    "W" to "Cycle mirror mode",
    "C" to "Cycle tools",
    "Circle: drag" to "Draw a circle from its center",
    "Line: drag" to "Draw a line",
    "Line: shift (hold)" to "Snap to 45°",
    "Circle/Line: right click" to "Remove a shape",
)

private enum class Tool(val label: String) {
    PEN("Pen"),
    CIRCLE("Circle"),
    LINE("Line");

    fun next(): Tool = entries[(ordinal + 1) % entries.size]
}

private val SHAPE_THICKNESSES = listOf(0.5f, 1f, 1.5f, 2f, 3f)
private const val MIN_SHAPE_SIZE = 0.25f
private const val SNAP_ANGLE = (PI / 4).toFloat()
private const val FAVORITE_CIRCLES = "|"
private const val SHARE_STATUS_MILLIS = 1500L
private const val COPIED = "Copied!"
private const val INVALID = "Invalid"

private fun favoriteOf(pixels: CrosshairPixels, circles: CrosshairCircles): String {
    val base64 = pixels.toBase64()
    return if (circles.isEmpty()) base64 else base64 + FAVORITE_CIRCLES + circles.encode()
}

private fun splitFavorite(entry: String): Pair<String, String> =
    entry.substringBefore(FAVORITE_CIRCLES) to entry.substringAfter(FAVORITE_CIRCLES, "")

private fun DrawScope.drawShapes(shapes: List<VectorShape>, cell: Float) {
    shapes.forEach {
        when (it) {
            is Circle -> drawCircle(Color(it.color), it.radius * cell, Offset(it.x * cell, it.y * cell), style = Stroke(it.thickness * cell))
            is Line -> drawLine(Color(it.color), Offset(it.x1 * cell, it.y1 * cell), Offset(it.x2 * cell, it.y2 * cell), it.thickness * cell)
        }
    }
}

// OneConfig's ColorOption wraps its content and takes no modifier, so this rebuilds it at full width around the same picker popup.
@Composable
private fun PenColor() {
    val theme = OneConfigTheme.current
    val color = Color(ModConfig.penColor.observe() ?: -0x1)
    val model = remember { ColorPickerModel(color, true) }
    var expanded by remember { mutableStateOf(false) }
    val content by animateColorAsState(if (color.luminance() > 0.6f) Color.Black else Color.White)
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand)
                .onClick(rememberInteractionSource()) { expanded = !expanded }
                .background(color, theme.sideBarNavigationEntryShape)
                .border(1.dp, theme.borderColor, theme.sideBarNavigationEntryShape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon("paintbrush", color = content, modifier = Modifier.size(14.dp))
            Text("#${colorToHex(color)}", modifier = Modifier, color = content, fontSize = 13.sp)
        }
        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, PICKER_OFFSET),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                ColorPickerPopup(model, { ModConfig.penColor.set(it.toArgb()) }, { expanded = false }, false)
            }
        }
    }
}

private const val PICKER_OFFSET = 44

@Composable
private fun EditorButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val theme = OneConfigTheme.current
    val interactionSource = rememberInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bgColor by animateColorAsState(if (isHovered && enabled) OneConfigTheme.accent.copy(alpha = 0.75f) else OneConfigTheme.accent)
    Box(
        modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .pointerHoverIcon(PointerIcon.Hand)
            .background(bgColor, theme.buttonShape)
            .onClick(interactionSource, enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, modifier = Modifier, color = theme.accentTextColor, fontSize = 13.sp)
    }
}
