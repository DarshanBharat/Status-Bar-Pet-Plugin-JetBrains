package com.github.pet.pycharm.ui

import com.github.pet.pycharm.icons.PetVectorRenderer
import com.github.pet.pycharm.models.*
import com.github.pet.pycharm.state.PetPersistentState
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.GraphicsUtil
import com.intellij.ui.scale.JBUIScale
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import javax.swing.*
import javax.swing.plaf.basic.BasicMenuItemUI
import javax.swing.plaf.basic.BasicMenuUI
import kotlin.math.max

/** Compact quick actions and native cascading menus, styled together. */
class PetMenuPanel(
    private val state: PetPersistentState,
    private val focusRunning: () -> Boolean,
    private val onClose: () -> Unit,
    private val onFeed: () -> Unit,
    private val onCheer: () -> Unit,
    private val onFocus: () -> Unit,
    private val onChange: () -> Unit
) : JPopupMenu() {
    private val menuBackground = JBColor.namedColor("PopupMenu.background", JBColor(Color(0xFAFAFA), Color(0x2B2D30)))
    private val textColor = JBColor.namedColor("Label.foreground", JBColor(Color(0x24262B), Color(0xDFE1E5)))
    private val mutedColor = JBColor.namedColor("Label.infoForeground", JBColor(Color(0x70747C), Color(0x969AA3)))
    private val accentColor = JBColor.namedColor("Component.focusColor", JBColor(Color(0x3574F0), Color(0x548AF7)))
    private val hoverColor: Color get() = mix(menuBackground, accentColor, 0.14f)
    private val lineColor: Color get() = mix(menuBackground, textColor, 0.10f)
    private val bindings = mutableListOf<() -> Unit>()
    private val nameLabel = JLabel().apply { putClientProperty("html.disable", true) }
    private var glowUntil = 0L
    private var displayedLevel = state.level
    private val progressLabel = object : JLabel() {
        override fun paintComponent(g: Graphics) {
            if (System.currentTimeMillis() < glowUntil) {
                val graphics = smoothGraphics(g)
                graphics.color = hoverColor
                graphics.fillRoundRect(0, 0, width, height, JBUI.scale(6), JBUI.scale(6))
                graphics.dispose()
            }
            super.paintComponent(g)
        }
    }
    private val subtitle = JLabel().apply { putClientProperty("html.disable", true) }
    private val portrait = JLabel()
    private var feedbackText: String? = null
    private lateinit var feedButton: JButton
    val preferredFocus: JComponent get() = feedButton

    init {
        stylePopup(this)
        val header = JPanel(BorderLayout(JBUI.scale(10), 0)).apply {
            isOpaque = false
            border = JBUI.Borders.empty(7, 7, 10, 3)
            add(portrait, BorderLayout.WEST)
            add(JPanel(GridLayout(2, 1)).apply {
                isOpaque = false
                add(JPanel(BorderLayout(JBUI.scale(7), 0)).apply {
                    isOpaque = false
                    add(nameLabel.apply { foreground = textColor; font = font.deriveFont(Font.BOLD, font.size2D + 1f) }, BorderLayout.CENTER)
                    add(progressLabel.apply { foreground = mutedColor; font = font.deriveFont(Font.PLAIN, font.size2D - 1f) }, BorderLayout.EAST)
                })
                add(subtitle.apply { foreground = mutedColor; font = font.deriveFont(Font.PLAIN, font.size2D - 1f) })
            }, BorderLayout.CENTER)
            add(JButton(MenuIcon("close")).apply {
                preferredSize = JBUI.size(25, 25)
                isContentAreaFilled = false; isBorderPainted = false; toolTipText = null
                accessibleContext.accessibleName = "Close pet menu"
                addActionListener { onClose() }
            }, BorderLayout.EAST)
            addMouseListener(object : MouseAdapter() {
                override fun mouseEntered(e: MouseEvent) = closeFlyout()
            })
        }
        add(header)
        add(JPanel(GridLayout(1, 3, JBUI.scale(5), 0)).apply {
            isOpaque = false
            border = JBUI.Borders.empty(0, 6, 7, 6)
            feedButton = actionButton("Feed", onFeed)
            add(feedButton)
            add(actionButton("Cheer", onCheer))
            val focus = actionButton("Focus", onFocus)
            bindings.add {
                focus.text = if (focusRunning()) "Stop focus" else "Focus"
                focus.accessibleContext.accessibleName = if (focusRunning()) "Stop focus session" else "Start 25 minute focus session"
            }
            add(focus)
        })
        divider(this)
        add(petsMenu())
        add(accessoriesMenu())
        add(motionMenu())
        add(statsMenu())
        refresh()
    }

    // Keep the device transform intact: text and vectors render at the monitor's
    // actual resolution, including fractional Windows display scales.
    override fun paint(g: Graphics) {
        val graphics = smoothGraphics(g)
        try { super.paint(graphics) } finally { graphics.dispose() }
    }

    private fun smoothGraphics(g: Graphics): Graphics2D = (g.create() as Graphics2D).apply {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        GraphicsUtil.applyRenderingHints(this)
        setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
    }

    override fun getPreferredSize(): Dimension = super.getPreferredSize().let {
        Dimension((it.width + JBUI.scale(8)).coerceIn(JBUI.scale(280), JBUI.scale(440)), it.height)
    }

    fun showFeedback(text: String) {
        feedbackText = text.lineSequence().firstOrNull().orEmpty()
        subtitle.text = feedbackText
        subtitle.accessibleContext.accessibleName = text
        repaint()
    }

    fun refresh() {
        nameLabel.text = state.getPetName()
        if (state.level > displayedLevel) glowUntil = System.currentTimeMillis() + 2200
        displayedLevel = state.level
        progressLabel.text = "Lv ${state.level} · ${state.xp}/${state.xpNeededForNextLevel()} XP"
        progressLabel.foreground = if (System.currentTimeMillis() < glowUntil) accentColor else mutedColor
        progressLabel.accessibleContext.accessibleName = "Level ${state.level}, ${state.xp} of ${state.xpNeededForNextLevel()} XP toward the next level"
        portrait.icon = Portrait(state.getSelectedPetType())
        subtitle.text = feedbackText ?: when (state.getSelectedPetType()) {
            PetType.CAT -> "Cat"
            PetType.DOG -> "Corgi"
            PetType.DUCK -> "Duck"
            PetType.CRAB -> "Rust Crab"
        }
        bindings.forEach { it() }
        smoothComponents(this)
        revalidate()
        repaint()
    }

    private fun smoothComponents(component: JComponent) {
        GraphicsUtil.setAntialiasingType(component, GraphicsUtil.createAATextInfo(RenderingHints.VALUE_TEXT_ANTIALIAS_ON))
        component.components.filterIsInstance<JComponent>().forEach(::smoothComponents)
        if (component is JMenu) smoothComponents(component.popupMenu)
    }

    private fun change(action: () -> Unit) {
        // Settings saves run in the background and take the same state lock.
        synchronized(state) { action() }
        feedbackText = null
        refresh()
        onChange()
    }

    private fun petsMenu(): JMenu = category("Pets").apply {
        heading(popupMenu, "Main pet")
        PetType.entries.forEach { type ->
            add(choice(state.getPetName(type), radio = true, selected = { state.getSelectedPetType() == type }) {
                change { state.setSelectedPetType(type) }
            }.also { item -> bindings.add { item.text = state.getPetName(type) } })
        }
        divider(popupMenu)
        heading(popupMenu, "Additional pets")
        PetType.entries.forEach { type ->
            val item = choice(
                state.getPetName(type),
                selected = { type.id in state.companionPetIds },
                enabled = { (type.id in state.companionPetIds) || state.companionPetIds.size < 2 }
            ) {
                change { state.toggleCompanion(type) }
            }
            bindings.add { item.isVisible = type != state.getSelectedPetType(); item.text = state.getPetName(type) }
            add(item)
        }
        divider(popupMenu)
        add(JMenuItem("Rename main pet…").apply {
            setUI(ItemUi(true)); styleItem(this)
            addActionListener {
                closeFlyout()
                val type = state.getSelectedPetType()
                val name = Messages.showInputDialog(this@PetMenuPanel, "Pet name (1–20 characters)", "Rename pet", null,
                    state.getPetName(type), object : InputValidator {
                        override fun checkInput(input: String): Boolean = input.trim().let { it.isNotEmpty() && it.codePointCount(0, it.length) <= 20 && it.none(Char::isISOControl) }
                        override fun canClose(input: String) = checkInput(input)
                    })
                if (name != null) change { state.customNames[type.id] = name.trim() }
            }
        })
    }

    private fun accessoriesMenu(): JMenu = category("Accessories").apply {
        PetAccessory.entries.forEach { accessory ->
            val label = accessory.displayName.substringAfter(' ')
            val item = choice(label, selected = { state.isAccessoryActive(accessory) }, enabled = { state.isAccessoryUnlocked(accessory) }) {
                change { state.toggleAccessory(accessory) }
            }
            bindings.add { item.text = label + if (state.isAccessoryUnlocked(accessory)) "" else " · Lv ${accessory.unlockLevel}" }
            add(item)
        }
        divider(popupMenu)
        add(choice("Show pet homes", selected = { state.showHomes }) { change { state.showHomes = !state.showHomes } })
    }

    private fun motionMenu(): JMenu = category("Motion").apply {
        heading(popupMenu, "Movement")
        add(choice("Wander around", selected = { state.wanderingEnabled }) { change { state.wanderingEnabled = !state.wanderingEnabled } })
        add(choice("Follow mouse", selected = { state.followMouse }) { change { state.followMouse = !state.followMouse } })
        add(choice("Companion interactions", selected = { state.companionInteractions }, enabled = { state.wanderingEnabled }) { change { state.companionInteractions = !state.companionInteractions } })
        divider(popupMenu)
        heading(popupMenu, "Animation speed")
        listOf(AnimationSpeed.ZEN_SLOW to "Slow", AnimationSpeed.MEDIUM to "Calm", AnimationSpeed.FAST to "Lively").forEach { (speed, label) ->
            add(choice(label, radio = true, selected = { state.getSpeed() == speed }) { change { state.setSpeed(speed) } })
        }
        divider(popupMenu)
        heading(popupMenu, "Status bar width")
        listOf("compact" to "Compact", "normal" to "Comfortable", "wide" to "Wide").forEach { (id, label) ->
            add(choice(label, radio = true, selected = { state.widthId == id }) { change { state.widthId = id } })
        }
        divider(popupMenu)
        add(choice("Break & stretch reminders", selected = { state.healthRemindersEnabled }) { change { state.healthRemindersEnabled = !state.healthRemindersEnabled } })
        add(choice("Freeze animation", selected = { state.isPaused }) { change { state.isPaused = !state.isPaused } })
    }

    private fun statsMenu(): JMenu = category("Statistics").apply {
        heading(popupMenu, "Statistics")
        add(statRow("Typed edits") { "${state.manualEditCount} · +${state.totalKeystrokes} / −${state.manualCharsRemoved}" })
        add(statRow("Automated edits") { "${state.otherEditCount + state.aiAgentEditCount} · +${state.otherCharsAdded + state.aiAgentCharsAdded} / −${state.otherCharsRemoved + state.aiAgentCharsRemoved}" })
        add(statRow("Active coding") { "${state.totalCodingMinutes} minutes" })
    }

    private fun category(label: String): JMenu = JMenu(label).apply {
        setUI(CategoryUi())
        styleItem(this)
        icon = MenuIcon(when (label) {
            "Pets" -> "paw"
            "Accessories" -> "sparkle"
            "Motion" -> "settings"
            else -> "stats"
        })
        iconTextGap = JBUI.scale(10)
        delay = 300
        stylePopup(popupMenu)
    }
    private fun stylePopup(popup: JPopupMenu) {
        popup.background = menuBackground
        popup.border = JBUI.Borders.compound(JBUI.Borders.customLine(lineColor, 1), JBUI.Borders.empty(5))
    }
    private fun styleItem(item: JMenuItem) {
        item.putClientProperty("html.disable", true)
        item.background = menuBackground
        item.foreground = textColor
        item.font = UIManager.getFont("Label.font").deriveFont(Font.PLAIN)
        item.border = JBUI.Borders.empty(6, 11)
        item.toolTipText = null
    }
    private fun heading(popup: JPopupMenu, text: String) {
        popup.add(JLabel(text).apply {
            foreground = mutedColor
            font = font.deriveFont(Font.PLAIN, font.size2D - 1f)
            border = JBUI.Borders.empty(5, 11, 4, 11)
        })
    }
    private fun divider(popup: JPopupMenu) {
        popup.add(JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.empty(4, 5)
            add(object : JComponent() {
                override fun getPreferredSize() = Dimension(1, JBUI.scale(1))
                override fun paintComponent(g: Graphics) {
                    g.color = lineColor
                    g.fillRect(0, 0, width, JBUI.scale(1))
                }
            }, BorderLayout.CENTER)
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(9))
        })
    }
    private fun statRow(label: String, value: () -> String): JPanel = JPanel(BorderLayout(JBUI.scale(20), 0)).apply {
        isOpaque = false
        border = JBUI.Borders.empty(4, 11)
        add(JLabel(label).apply { foreground = mutedColor }, BorderLayout.WEST)
        val data = JLabel().apply { foreground = textColor }
        bindings.add { data.text = value() }
        add(data, BorderLayout.EAST)
    }
    private fun choice(label: String, radio: Boolean = false, selected: () -> Boolean, enabled: () -> Boolean = { true }, action: () -> Unit): JMenuItem {
        val item = if (radio) JRadioButtonMenuItem() else JCheckBoxMenuItem()
        item.setUI(ItemUi(true))
        styleItem(item)
        item.text = label
        item.icon = SelectionIcon(radio) { item.isSelected }
        bindings.add { item.isSelected = selected(); item.isEnabled = enabled() }
        item.addActionListener { action() }
        return item
    }
    private fun closeFlyout() {
        if (isVisible) MenuSelectionManager.defaultManager().selectedPath = arrayOf(this)
    }
    private fun actionButton(text: String, action: () -> Unit): JButton = object : JButton(text) {
        override fun paintComponent(g: Graphics) {
            val graphics = smoothGraphics(g)
            try {
                graphics.color = when { model.isPressed -> mix(menuBackground, accentColor, 0.24f)
                    model.isRollover || isFocusOwner -> hoverColor
                    else -> mix(menuBackground, textColor, 0.05f) }
                val inset = JBUIScale.scale(0.5f)
                val arc = JBUIScale.scale(8f)
                val shape = RoundRectangle2D.Float(inset, inset, width - inset * 2, height - inset * 2, arc, arc)
                graphics.fill(shape)
                graphics.color = if (isFocusOwner) accentColor else lineColor
                graphics.stroke = BasicStroke(JBUIScale.scale(1f))
                graphics.draw(shape)
                super.paintComponent(graphics)
            } finally { graphics.dispose() }
        }
    }.apply {
        isOpaque = false; isContentAreaFilled = false; isBorderPainted = false; isFocusPainted = false; isRolloverEnabled = true
        foreground = textColor
        font = UIManager.getFont("Label.font").deriveFont(Font.PLAIN)
        border = JBUI.Borders.empty(6, 4)
        preferredSize = JBUI.size(82, 30)
        toolTipText = null
        accessibleContext.accessibleName = text
        addMouseListener(object : MouseAdapter() { override fun mouseEntered(e: MouseEvent) = closeFlyout() })
        addActionListener { closeFlyout(); action(); refresh() }
    }

    private fun paintRow(g: Graphics, item: JMenuItem, color: Color) {
        g.color = menuBackground
        g.fillRect(0, 0, item.width, item.height)
        if (item.model.isArmed || item is JMenu && item.isSelected) {
            val graphics = g.create() as Graphics2D
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.color = hoverColor
            graphics.fillRoundRect(1, 1, item.width - 2, item.height - 2, JBUI.scale(5), JBUI.scale(5))
            graphics.dispose()
        }
    }
    private fun paintLabel(g: Graphics, item: JMenuItem, bounds: Rectangle, text: String) {
        val graphics = smoothGraphics(g)
        try {
            graphics.font = item.font
            graphics.color = if (item.isEnabled) item.foreground else mutedColor
            graphics.drawString(text, bounds.x, bounds.y + graphics.fontMetrics.ascent)
        } finally { graphics.dispose() }
    }
    private fun paintMenuItem(g: Graphics, item: JMenuItem, arrow: Icon? = null) {
        paintRow(g, item, menuBackground)
        val insets = item.insets
        val icon = item.icon
        val left = insets.left + JBUI.scale(3)
        icon?.paintIcon(item, g, left, (item.height - icon.iconHeight) / 2)
        val textX = left + if (icon == null) 0 else icon.iconWidth + item.iconTextGap
        val arrowWidth = arrow?.iconWidth ?: 0
        val available = (item.width - insets.right - arrowWidth - JBUI.scale(8) - textX).coerceAtLeast(0)
        val graphics = smoothGraphics(g)
        try {
            graphics.font = item.font
            val metrics = graphics.fontMetrics
            var text = item.text.orEmpty()
            if (metrics.stringWidth(text) > available) {
                while (text.isNotEmpty() && metrics.stringWidth("$text…") > available) {
                    text = text.substring(0, text.offsetByCodePoints(text.length, -1))
                }
                text += "…"
            }
            paintLabel(graphics, item, Rectangle(textX, (item.height - metrics.height) / 2, available, metrics.height), text)
        } finally { graphics.dispose() }
        arrow?.paintIcon(item, g, item.width - insets.right - arrowWidth, (item.height - arrow.iconHeight) / 2)
    }
    private inner class ItemUi(private val keepOpen: Boolean) : BasicMenuItemUI() {
        override fun installDefaults() { super.installDefaults(); checkIcon = null; arrowIcon = null }
        override fun paint(g: Graphics, c: JComponent) = paintMenuItem(g, c as JMenuItem)
        override fun paintBackground(g: Graphics, item: JMenuItem, color: Color) = paintRow(g, item, color)
        override fun paintText(g: Graphics, item: JMenuItem, bounds: Rectangle, text: String) = paintLabel(g, item, bounds, text)
        override fun getPreferredMenuItemSize(c: JComponent, check: Icon?, arrow: Icon?, gap: Int): Dimension =
            super.getPreferredMenuItemSize(c, check, arrow, gap).let { Dimension(max(JBUI.scale(250), it.width), JBUI.scale(30)) }
        override fun doClick(manager: MenuSelectionManager) {
            if (keepOpen) menuItem.doClick(0) else super.doClick(manager)
        }
    }
    private inner class CategoryUi : BasicMenuUI() {
        override fun installDefaults() { super.installDefaults(); checkIcon = null; arrowIcon = MenuIcon("chevron") }
        override fun paint(g: Graphics, c: JComponent) = paintMenuItem(g, c as JMenuItem, arrowIcon)
        override fun paintBackground(g: Graphics, item: JMenuItem, color: Color) = paintRow(g, item, color)
        override fun paintText(g: Graphics, item: JMenuItem, bounds: Rectangle, text: String) = paintLabel(g, item, bounds, text)
        override fun getPreferredMenuItemSize(c: JComponent, check: Icon?, arrow: Icon?, gap: Int): Dimension =
            super.getPreferredMenuItemSize(c, check, arrow, gap).let { Dimension(max(JBUI.scale(260), it.width), JBUI.scale(32)) }
    }
    private inner class SelectionIcon(private val radio: Boolean, private val selected: () -> Boolean) : Icon {
        override fun getIconWidth() = JBUI.scale(16)
        override fun getIconHeight() = JBUI.scale(16)
        override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
            val graphics = g.create() as Graphics2D
            graphics.translate(x.toDouble(), y.toDouble()); graphics.scale(iconWidth / 16.0, iconHeight / 16.0)
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.color = if (selected() && c?.isEnabled != false) accentColor else mutedColor
            graphics.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            if (radio) {
                graphics.draw(Ellipse2D.Float(2.5f, 2.5f, 11f, 11f))
                if (selected()) graphics.fill(Ellipse2D.Float(5.5f, 5.5f, 5f, 5f))
            } else {
                graphics.draw(RoundRectangle2D.Float(2.5f, 2.5f, 11f, 11f, 3f, 3f))
                if (selected()) graphics.draw(Path2D.Float().apply {
                    moveTo(4.5, 7.5); lineTo(7.0, 10.0); lineTo(11.5, 5.0)
                })
            }
            graphics.dispose()
        }
    }
    private fun mix(base: Color, tint: Color, amount: Float): Color = Color(
        (base.red * (1 - amount) + tint.red * amount).toInt(),
        (base.green * (1 - amount) + tint.green * amount).toInt(),
        (base.blue * (1 - amount) + tint.blue * amount).toInt())

    private inner class Portrait(private val type: PetType) : Icon {
        override fun getIconWidth() = JBUI.scale(38)
        override fun getIconHeight() = JBUI.scale(38)
        override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
            val graphics = g.create() as Graphics2D
            graphics.translate(x.toDouble(), y.toDouble())
            graphics.scale(iconWidth / 38.0, iconHeight / 38.0)
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            graphics.color = hoverColor
            graphics.fill(Ellipse2D.Float(0f, 0f, 38f, 38f))
            graphics.translate(19.0, 23.0)
            graphics.scale(1.12, 1.12)
            PetVectorRenderer.renderPet(graphics, type, PetActivityState.IDLE, 0, 0f, true, isPaused = true, coat = state.getCoat(type))
            graphics.dispose()
        }
    }

    private inner class MenuIcon(private val glyph: String) : Icon {
        override fun getIconWidth() = JBUI.scale(18)
        override fun getIconHeight() = JBUI.scale(18)
        override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
            val graphics = g.create() as Graphics2D
            graphics.translate(x.toDouble(), y.toDouble())
            graphics.scale(iconWidth / 18.0, iconHeight / 18.0)
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.color = mutedColor
            graphics.stroke = BasicStroke(1.35f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            fun line(x1: Int, y1: Int, x2: Int, y2: Int) = graphics.drawLine(x1, y1, x2, y2)
            when (glyph) {
                "close" -> { line(5, 5, 13, 13); line(13, 5, 5, 13) }
                "chevron" -> { line(7, 5, 11, 9); line(11, 9, 7, 13) }
                "clock" -> { graphics.drawOval(2, 2, 14, 14); line(9, 5, 9, 9); line(9, 9, 12, 11) }
                "stats" -> { line(3, 14, 3, 9); line(8, 14, 8, 4); line(13, 14, 13, 7) }
                "paw" -> {
                    graphics.fill(Ellipse2D.Float(5f, 8f, 8f, 7f))
                    listOf(2f to 5f, 6f to 2f, 11f to 2f, 14f to 5f).forEach { (px, py) ->
                        graphics.fill(Ellipse2D.Float(px, py, 3f, 4f))
                    }
                }
                "settings" -> {
                    line(3, 4, 15, 4); line(3, 9, 15, 9); line(3, 14, 15, 14)
                    graphics.color = menuBackground
                    graphics.fillOval(5, 2, 4, 4); graphics.fillOval(10, 7, 4, 4); graphics.fillOval(4, 12, 4, 4)
                    graphics.color = mutedColor
                    graphics.drawOval(5, 2, 4, 4); graphics.drawOval(10, 7, 4, 4); graphics.drawOval(4, 12, 4, 4)
                }
                "mail" -> { graphics.drawRoundRect(2, 4, 14, 10, 2, 2); line(3, 5, 9, 10); line(9, 10, 15, 5) }
                "feed" -> { graphics.drawArc(2, 5, 14, 10, 180, 180); line(2, 10, 16, 10); line(6, 4, 6, 6); line(10, 3, 10, 6) }
                "sparkle" -> {
                    val star = Path2D.Float()
                    star.moveTo(9.0, 2.0); star.lineTo(11.0, 7.0); star.lineTo(16.0, 9.0); star.lineTo(11.0, 11.0)
                    star.lineTo(9.0, 16.0); star.lineTo(7.0, 11.0); star.lineTo(2.0, 9.0); star.lineTo(7.0, 7.0); star.closePath()
                    graphics.draw(star)
                }
            }
            graphics.dispose()
        }
    }
}
