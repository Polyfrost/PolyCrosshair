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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.crosshair.config.CrosshairPixels
import org.polyfrost.crosshair.config.CrosshairProfile
import org.polyfrost.crosshair.config.MirrorMode
import org.polyfrost.crosshair.config.ModConfig
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.internal.ui.api.settings.ColorOptionData
import org.polyfrost.oneconfig.internal.ui.components.Chip
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.onClick
import org.polyfrost.oneconfig.internal.ui.components.rememberInteractionSource
import org.polyfrost.oneconfig.internal.ui.components.settings.ColorOption
import org.polyfrost.oneconfig.internal.ui.themes.Accent
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import java.util.function.Predicate
import kotlin.math.abs

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
    val theme = LocalTheme.current

    var reloads by remember(prop) { mutableIntStateOf(0) }
    val writing = remember(prop) { booleanArrayOf(false) }
    DisposableEffect(prop) {
        val gridListener = Predicate<Int> { reloads++; false }
        val imageListener = Predicate<String> { if (!writing[0]) reloads++; false }
        profile.gridSize.addCallback(gridListener)
        profile.image.addCallback(imageListener)
        onDispose {
            profile.gridSize.removeCallback(gridListener)
            profile.image.removeCallback(imageListener)
        }
    }
    val cells = remember(reloads) { profile.grid.cells }

    val state = remember(prop, reloads) { EditorState(profile.pixels()) }
    var version by remember(state) { mutableIntStateOf(0) }
    var mirror by remember(prop) { mutableStateOf(MirrorMode.NONE) }
    var guides by remember(prop) { mutableStateOf(true) }
    val pen = ModConfig.penColor.observe() ?: -0x1
    val favorites = remember(ModConfig.favorites.observe()) { ModConfig.favoriteList() }
    var helpOpen by remember(prop) { mutableStateOf(false) }

    val interaction = rememberInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val focus = remember(prop) { FocusRequester() }

    fun persist() {
        writing[0] = true
        try {
            profile.writePixels(state.pixels)
        } finally {
            writing[0] = false
        }
        ModConfig.save()
    }

    fun mutate(block: () -> Unit) {
        block()
        version++
    }

    fun replace(base64: String) {
        mutate {
            state.snapshot()
            state.pixels.copyFrom(CrosshairPixels.load(base64, cells, profile.resizeStretch.get() == true))
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
        onDispose { if (CanvasHover.owner === prop) CanvasHover.owner = null }
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
                                mutate { state.snapshot(); state.pixels.clear() }
                                persist(); true
                            }
                            Key.S -> { guides = !guides; true }
                            Key.Z -> { if (state.undo()) { version++; persist() }; true }
                            Key.Y -> { if (state.redo()) { version++; persist() }; true }
                            Key.W -> { mirror = mirror.next(); true }
                            else -> false
                        }
                    }
                    .focusRequester(focus)
                    .focusable()
                    .pointerInput(state, mirror, pen) {
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
                val guideColor = Accent.copy(alpha = 0.35f)
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
                    mutate { state.snapshot(); state.pixels.clear() }
                    persist()
                }
                EditorButton("Reset") { replace(CrosshairPixels.DEFAULT) }
                ColorOption(ColorOptionData(ModConfig.penColor))
                EditorButton("Save") {
                    val current = state.pixels.toBase64()
                    if (!state.pixels.isEmpty() && current !in favorites) ModConfig.setFavorites(favorites + current)
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (helpOpen) Accent else theme.popupBackground, CircleShape)
                        .border(1.dp, theme.borderColor, CircleShape)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .onClick(rememberInteractionSource()) { helpOpen = !helpOpen },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("i", color = if (helpOpen) theme.accentTextColor else theme.textColor, fontSize = 12.sp)
                }
            }

            BoxWithConstraints(Modifier.weight(1f)) {
                val perRow = ((maxWidth + FAVORITE_GAP) / (FAVORITE_SIZE + FAVORITE_GAP)).toInt().coerceAtLeast(1)
                Column(verticalArrangement = Arrangement.spacedBy(FAVORITE_GAP)) {
                    if (favorites.isEmpty()) {
                        Text("Saved crosshairs show up here.", color = theme.textColorSecondary, fontSize = 12.sp)
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
            Text("Mirror", color = theme.textColorSecondary, fontSize = 12.sp)
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
private fun Favorite(base64: String, onLoad: () -> Unit, onRemove: () -> Unit) {
    val theme = LocalTheme.current
    val pixels = remember(base64) { CrosshairPixels.fromBase64(base64) } ?: return
    val interaction = rememberInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    Box(
        Modifier
            .size(FAVORITE_SIZE)
            .clip(theme.backgroundShape)
            .background(theme.componentBackground, theme.backgroundShape)
            .border(1.dp, if (hovered) Accent else theme.borderColor, theme.backgroundShape)
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
    val theme = LocalTheme.current
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
                Text(keys, color = theme.textColor, fontSize = 12.sp)
                Text(what, color = theme.textColorSecondary, fontSize = 12.sp)
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
)

@Composable
private fun EditorButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val theme = LocalTheme.current
    val interactionSource = rememberInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bgColor by animateColorAsState(if (isHovered && enabled) Accent.copy(alpha = 0.75f) else Accent)
    Box(
        Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .pointerHoverIcon(PointerIcon.Hand)
            .background(bgColor, theme.buttonShape)
            .onClick(interactionSource, enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = theme.accentTextColor, fontSize = 13.sp)
    }
}
