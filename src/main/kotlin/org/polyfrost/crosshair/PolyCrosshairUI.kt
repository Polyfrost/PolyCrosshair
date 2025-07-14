package org.polyfrost.crosshair

import org.polyfrost.oneconfig.api.platform.v1.Platform
import org.polyfrost.oneconfig.api.ui.v1.OCPolyUIBuilder
import org.polyfrost.polyui.animate.Animations
import org.polyfrost.polyui.color.PolyColor
import org.polyfrost.polyui.color.argb
import org.polyfrost.polyui.color.mutable
import org.polyfrost.polyui.color.rgba
import org.polyfrost.polyui.component.Component
import org.polyfrost.polyui.component.Drawable
import org.polyfrost.polyui.component.extensions.*
import org.polyfrost.polyui.component.impl.*
import org.polyfrost.polyui.event.Event
import org.polyfrost.polyui.operations.Recolor
import org.polyfrost.polyui.unit.Align
import org.polyfrost.polyui.unit.Align.Wrap
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.unit.by
import org.polyfrost.polyui.unit.seconds
import org.polyfrost.polyui.utils.*
import java.awt.image.BufferedImage
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.math.abs
import kotlin.math.roundToInt

object PolyCrosshairUI {
    private var penColor = rgba(255, 255, 255).mutable().ref()
    private var vertical = false
    private var horizontal = false
    private var canvasSize = 15

    private lateinit var canvasContainer: Component
    private lateinit var crosshairName: Text
    private lateinit var library: Group
    private lateinit var canvasSizeDrawable: Text

    private var ignored = false
    private var needsToSave = false
    private var currentCard: Group? = null
        set(value) {
            if (field === value) return
            val old = field
            if (old != null) {
                if (needsToSave) {
                    val name = (old[1] as Text).text
                    val path = Paths.get(name.toFileName())
                    val data = genColorData(canvasContainer[0])
                    ImageIO.write(BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_ARGB).apply {
                        setRGB(0, 0, canvasSize, canvasSize, data, 0, canvasSize)
                    }, "png", Files.newOutputStream(path))
                    CrosshairHUD.currentCrosshair = path.toUri().toString()
                    CrosshairHUD.setCrosshair(data, canvasSize)
                    old.renderer.delete((old[0][0] as Image).image)
                    needsToSave = false
                }
                old[0].palette = old.polyUI.colors.component.bg
            }
            if (value == null) return
            val name = (value[1] as Text).text
            val path = Paths.get(name.toFileName())
            val img = try {
                ImageIO.read(Files.newInputStream(path))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            val size = img?.width ?: canvasSize
            val cdata = img?.getRGB(0, 0, size, size, null, 0, size) ?: intArrayOf(size * size)
            canvasContainer[0] = genCanvas(size, colorData = cdata)
            crosshairName.text = name
            canvasSize = size
            ignored = true
            canvasSizeDrawable.text = size.toString()
            value[0].setPalette { brand.fg }
            field = value
        }

    fun open() {
        val currentCrosshairName = CrosshairHUD.currentCrosshair.fromFileName()
        var currentCrosshairToSet: Group? = null
        val builder = OCPolyUIBuilder.create()
        builder.blurs().atResolution(1920f, 1080f).backgroundColor(rgba(17, 23, 28)).size(800f, 500f)
        builder.onClose { _ ->
            needsToSave = true
            currentCard = null
        }
        builder.makeAndOpen(
            Block(
                Group(
                    Image("assets/polycrosshair/icon.svg").onInit { size = 48f by 48f },
                    Text(" Poly", fontSize = 30f).setPalette { brand.fg }.setFont { semiBold },
                    Text("Crosshair", fontSize = 30f).setFont { regular },
                    alignment = Align(pad = Vec2(-1f, 0f))
                ),
                Group(
                    Text("v2.0.0").setPalette { text.secondary },
                    Image("assets/oneconfig/ico/close.svg").onInit { size = Vec2(24f, 24f) }
                        .setDestructivePalette().withHoverStates().onClick {
                            // will save the crosshair
                            needsToSave = true
                            currentCard = null
                            Platform.screen().close()
                        }
                ),
                size = Vec2(800f, 52f),
                radii = null,
                alignment = Align(main = Align.Main.SpaceBetween)
            ).named("Header").onInit { color = polyUI.colors.page.fg.disabled },

            Block(Group(size = Vec2(400f, 400f)), color = rgba(24, 24, 24), alignment = Align(pad = Vec2(8f, 8f)))
                .padded(24f, 12f, 12f, 12f).named("CanvasContainer").also { canvasContainer = it },

            Group(
                Block(
                    Group(
                        TextInput("Crosshair ?", placeholder = "Crosshair 1", fontSize = 24f).setFont { semiBold }.also { crosshairName = it },
                        Group(
                            Button("assets/polycrosshair/copy.svg".image(), padding = Vec2(7f, 6f)).onInit {
                                this[0].size = Vec2(13.25f, 16f)
                            }.onClick {
//                                Clipboard.getInstance().image = ClipboardImage(canvasSize, canvasSize, genColorData(canvasContainer[0]).toByteArray())
                            },
                            Image("assets/polycrosshair/trashcan.svg").onInit { this.size = Vec2(14.75f, 16f) }.withHoverStates().setDestructivePalette().onClick {
                                // generate a new canvas to effectively clear it
                                canvasContainer[0] = genCanvas(canvasSize)
                                true
                            }
                        ),
                        alignment = Align(main = Align.Main.SpaceBetween),
                        size = Vec2(330f, 36f)
                    ).named("CrosshairHeader").padded(0f, 0f, 0f, 8f),
                    Group(
                        Text("Canvas Size", fontSize = 18f),
                        BoxedNumericInput(min = 15f, max = 32f, initialValue = canvasSize.toFloat(), integral = true).also {
                            it[0].onChange { value: Int ->
                                // ignore flag to stop recursive loop when canvas size is set while loading a new one
                                if (!ignored) {
                                    canvasContainer[0] = genCanvas(value, colorData = genColorData(canvasContainer[0]).morphToSize(value, canvasSize))
                                    canvasSize = value
                                    needsToSave = true
                                }
                                ignored = false
                                false
                            }
                            canvasSizeDrawable = it[0][0][0] as Text
                        },
                        alignment = Align(main = Align.Main.SpaceBetween, pad = Vec2(6f, 12f)),
                        size = Vec2(326f, 40f)
                    ).named("CanvasSizeControl"),
                    Group(
                        Text("Pen Color", fontSize = 18f),
                        Block(color = penColor.deref(), size = Vec2(56f, 28f)).withBorder(3f, color = { page.border20 })
                            .onClick { ColorPicker(penColor, null, null, polyUI); true },
                        alignment = Align(main = Align.Main.SpaceBetween, pad = Vec2(6f, 12f)),
                        size = Vec2(326f, 40f)
                    ).named("PenColorControl"),
                    Group(
                        Text("Mirror", fontSize = 18f),
                        Block(
                            Block(
                                Image("assets/oneconfig/ico/refresh.svg".image()).onInit { size = size.coerceAtMost(Vec2(14f, 14f)) },
                                alignment = Align(pad = Vec2(4f, 4f))
                            ).toggleable(horizontal).onToggle { horizontal = it },
                            Block(
                                Image("assets/oneconfig/ico/settings.svg".image()).onInit { size = size.coerceAtMost(Vec2(14f, 14f)) },
                                alignment = Align(pad = Vec2(4f, 4f))
                            ).toggleable(vertical).onToggle { vertical = it },
                            alignment = Align(pad = Vec2(4f, 4f))
                        ),
                        alignment = Align(main = Align.Main.SpaceBetween, pad = Vec2(6f, 12f)),
                        size = Vec2(326f, 40f)
                    ).named("MirrorControl"),
                    alignment = Align(pad = Vec2(6f, 0f)),
                    size = Vec2(335f, 0f)
                ).padded(0f, 12f, 0f, 12f),
                Block(
                    Group(
                        Text("Library", fontSize = 24f).setFont { semiBold }.padded(6f, 0f),
                        Button("assets/oneconfig/ico/plus.svg".image(), padding = Vec2(6f, 6f)).onClick {
                            val n = library.children!!.size + 1
                            copyMakeAndSet("Crosshair $n")
                        },
                        alignment = Align(main = Align.Main.SpaceBetween),
                        size = Vec2(335f, 42f)
                    ).named("LibraryHeader"),
                    Group(
                        *getCrosshairs().use { s ->
                            s.map {
                                val fn = it.toUri().toString()
                                val fName = fn.fromFileName()
                                val c = makeLibraryCard(fName, fn)
                                if (fName == currentCrosshairName) currentCrosshairToSet = c
                                c
                            }.toTypedArray()
                        },
                        size = Vec2(335f, 174f),
                        visibleSize = Vec2(335f, 174f),
                        alignment = Align(cross = Align.Cross.Start)
                    ).also { library = it },
                    size = Vec2(335f, 214f),
                    alignment = Align(cross = Align.Cross.Start, pad = Vec2.ZERO)
                ).named("Library"),
                alignment = Align(wrap = Wrap.ALWAYS, pad = Vec2(0f, 24f)),
            ),
        )
        if (library.children.isNullOrEmpty()) {
            copyMakeAndSet("Crosshair 1")
        } else if (currentCrosshairToSet != null) currentCard = currentCrosshairToSet
//    polyUI.keyBinder?.add(KeyBinder.Bind(key = Keys.UP) { if (it) move(0, -1, canvasContainer[0], canvasSize); true })
//    polyUI.keyBinder?.add(KeyBinder.Bind(key = Keys.DOWN) { if (it) move(0, 1, canvasContainer[0], canvasSize); true })
//    polyUI.keyBinder?.add(KeyBinder.Bind(key = Keys.LEFT) { if (it) move(-1, 0, canvasContainer[0], canvasSize); true })
//    polyUI.keyBinder?.add(KeyBinder.Bind(key = Keys.RIGHT) { if (it) move(1, 0, canvasContainer[0], canvasSize); true })
//    window.open(polyUI)
    }

    private fun copyMakeAndSet(name: String) {
        val fn = Paths.get(name.toFileName())
        Files.copy(getResourceStream("assets/polycrosshair/default.png"), fn)
        val c = makeLibraryCard(name, fn.toUri().toString())
        currentCard = c
        library.addChild(c)
    }

    private fun getCrosshairs(path: Path = Paths.get("crosshairs")): DirectoryStream<Path> {
        if (!path.exists()) path.createDirectories()
        return Files.newDirectoryStream(path, "*.png")
    }

    /**
     * Make a library card for the given crosshair.
     */
    private fun makeLibraryCard(name: String, fileName: String) = Group(
        Block(
            Image(fileName),
            size = Vec2(48f, 48f),
            alignment = Align(main = Align.Main.Center, pad = Vec2.ZERO)
        ).withBorder(2f) { page.border10 },
        Text(name, fontSize = 8f).setPalette { text.secondary }.padded(0f, 6f, 0f, 0f),
        alignment = Align(main = Align.Main.Center, wrap = Wrap.ALWAYS, pad = Vec2.ZERO)
    ).onClick {
        currentCard = this
    }.onRightClick {
        PopupMenu(
            Text("Delete").withHoverStates().setDestructivePalette().onClick {
                library.removeChild(this@onRightClick)
                Paths.get((this@onRightClick[1] as Text).text.toFileName()).deleteIfExists()
                polyUI.unfocus()
            },
            Text("Copy").onClick {
                // todo copy
                polyUI.unfocus()
            },
            polyUI = polyUI
        )
    }


    /**
     * Generate a new canvas, of size [cells]x[cells], optionally with the given color data.
     */
    private fun genCanvas(cells: Int, colorData: IntArray? = null): Component {
        val canvasSize = 400f
        val sqSize = (canvasSize - 1f) / cells

        return Group(
            *Array(cells * cells) {
                val defColor = rgba(255, 255, 255, 0f).mutable()
                val cl = colorData?.get(it) ?: 0
                val color = if (cl != 0) argb(cl).mutable() else defColor
                var toggled = !color.transparent
                Block(radii = null, size = Vec2(sqSize, sqSize), color = color).withBorder(1f) { page.border10 }
                    .events {
                        Event.Mouse.Entered then { ev ->
                            (this.color as PolyColor.Mutable).alpha += 0.05f
                            mirrorDispatches(ev, sqSize, cells)
                        }
                        Event.Mouse.Exited then { ev ->
                            (this.color as PolyColor.Mutable).alpha -= 0.05f
                            mirrorDispatches(ev, sqSize, cells)
                        }
                        Event.Mouse.Companion.Clicked then { ev ->
                            toggled = !toggled
                            Recolor(this, if (toggled) penColor.deref() else defColor, Animations.Default.create(0.05.seconds)).add()
                            mirrorDispatches(ev, sqSize, cells)
                            needsToSave = true
                        }
                    }
            },
            alignment = Align(pad = Vec2(0f, 0f)),
            size = Vec2(canvasSize, canvasSize)
        )
    }

    // recursive loop prevention
    private var ran = false

    /**
     * mirror the event dispatch to the mirrored cells.
     */
    private fun Drawable.mirrorDispatches(it: Event.Mouse, sqSize: Float, size: Int) {
        if (ran) return
        if (!horizontal && !vertical) return
        ran = true
        val thisX = ((this.x - parent.x) / sqSize).roundToInt()
        val thisY = ((this.y - parent.y) / sqSize).roundToInt()
        val mirrorX = size - thisX - 1
        val mirrorY = size - thisY - 1

        if (horizontal && mirrorX != thisX) (parent[thisY * size + mirrorX] as Drawable).accept(it)
        if (vertical && mirrorY != thisY) (parent[mirrorY * size + thisX] as Drawable).accept(it)
        // diagonal
        if (horizontal && vertical && mirrorX != thisX && mirrorY != thisY) {
            (parent[mirrorY * size + mirrorX] as Drawable).accept(it)
        }
        ran = false
    }

    /**
     * Move the canvas by the given amount.
     */
    private fun move(x: Int, y: Int, canvas: Component, size: Int) {
        val cells = canvas.children ?: return
        cells.fastEachIndexed { i, it ->
            val cellSize = it.width
            val tX = i % size
            val tY = i / size
            val newX = (tX + x + size) % size
            val newY = (tY + y + size) % size

            it.x = canvas.x + newX * cellSize
            it.y = canvas.y + newY * cellSize
        }

        canvas.children?.sortWith { a, b ->
            val cellSize = a.width
            val aColIndex = ((a.x - canvas.x) / cellSize).roundToInt()
            val aRowIndex = ((a.y - canvas.y) / cellSize).roundToInt()
            val bColIndex = ((b.x - canvas.x) / cellSize).roundToInt()
            val bRowIndex = ((b.y - canvas.y) / cellSize).roundToInt()

            val row = aRowIndex.compareTo(bRowIndex)
            if (row != 0) row else aColIndex.compareTo(bColIndex)
        }
        canvas.polyUI.inputManager.recalculate()
        needsToSave = true
    }

    /**
     * Morphs the given array to the provided size, attempting to keep the center of the array in the center of the new array.
     */
    private fun IntArray.morphToSize(size: Int, old: Int): IntArray {
        val out = IntArray(size * size)
        val center = if (size % 2 == 0) if (size < old) -1 else 1 else 0
        val ofsX = abs(size - old) / 2 + center
        val ofsY = abs(size - old) / 2 + center

        for (i in this.indices) {
            val oX = i / old
            val oY = i % old
            val newIndex = (oX + ofsY) * size + (oY + ofsX)
            if (newIndex !in out.indices) continue
            out[newIndex] = this[i]
        }
        return out
    }

    /**
     * Read out the color data from the canvas.
     */
    private fun genColorData(canvas: Component): IntArray {
        val children = canvas.children ?: return intArrayOf()
        val data = IntArray(children.size)
        var i = children.size - 1
        canvas.children!!.fastEachReversed {
            data[i] = (it as Drawable).color.argb
            i--
        }
        return data
    }

    private fun String.toFileName() = "crosshairs/${this.trim().replace(' ', '_')}.png"

    private fun String.fromFileName() = this.substringAfterLast('/').substringBeforeLast('.').replace('_', ' ')

    private fun IntArray.toByteArray(): ByteArray {
        var c = this[0]
        var i = 0
        var j = -8
        return ByteArray(size * 4) {
            if (j == 24) {
                j = -8
                i++
                c = this[i]
            }
            j += 8
            (c shr j and 0xFF).toByte()
        }
    }

    private fun ByteArray.toIntArray(): IntArray {
        require(size % 4 == 0) { "Array size must be a multiple of 4" }
        var i = -4
        return IntArray(size / 4) {
            i += 4
            this[i].toInt() and 0xFF or (this[i + 1].toInt() and 0xFF shl 8) or (this[i + 2].toInt() and 0xFF shl 16) or (this[i + 3].toInt() and 0xFF shl 24)
        }
    }
}
