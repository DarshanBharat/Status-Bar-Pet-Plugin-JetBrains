package com.github.pet.pycharm.tracking

import java.util.Locale
import java.awt.event.KeyEvent

/** Classification tables and the walker are shared instead of rebuilt per keystroke. */
object EditOriginClassifier {
    private val agents = listOf("junie" to "Junie", "copilot" to "Copilot", "codex" to "Codex",
        "claude" to "Claude", "windsurf" to "Windsurf", "codeium" to "Codeium", "tabnine" to "Tabnine",
        "ai assistant" to "JetBrains AI", "ai-generated" to "JetBrains AI")
    private val prefixes = listOf("com.intellij.ml.llm." to "JetBrains AI", "com.jetbrains.junie." to "Junie",
        "com.github.copilot." to "Copilot", "com.anthropic." to "Claude", "dev.continue." to "Continue")
    private val manualCommands = setOf("typing", "paste", "cut", "delete", "backspace", "enter", "insert tab", "delete line")
    private val toolCommands = listOf("undo", "redo", "format", "refactor", "rename", "optimize", "replace all", "generate", "organize imports")
    private val walker = StackWalker.getInstance()

    fun namedAgent(command: String): String? = agents.firstOrNull { command.contains(it.first, ignoreCase = true) }?.second

    fun stackAgent(): String? = walker.walk { frames ->
        frames.limit(96).iterator().asSequence().firstNotNullOfOrNull { frame ->
            prefixes.firstOrNull { frame.className.startsWith(it.first) }?.second
        }
    }

    fun isManual(command: String, directTyping: Boolean): Boolean {
        val normalized = command.trim().lowercase(Locale.ROOT)
        if (isToolCommand(normalized)) return false
        // A keyboard shortcut can launch a formatter or refactoring. That doesn't
        // turn every replacement made by the tool into personally typed code.
        return normalized in manualCommands || directTyping
    }

    fun isToolCommand(command: String): Boolean = toolCommands.any { command.contains(it, ignoreCase = true) }

    fun isDirectTyping(event: KeyEvent): Boolean {
        if (event.id == KeyEvent.KEY_TYPED) {
            return event.keyChar != KeyEvent.CHAR_UNDEFINED && !event.keyChar.isISOControl() &&
                (!event.isControlDown && !event.isAltDown || event.isAltGraphDown)
        }
        if (event.id != KeyEvent.KEY_PRESSED) return false
        if (event.keyCode == KeyEvent.VK_ENTER || event.keyCode == KeyEvent.VK_TAB) {
            return !event.isControlDown && !event.isMetaDown && !event.isAltDown
        }
        if (event.keyCode == KeyEvent.VK_BACK_SPACE || event.keyCode == KeyEvent.VK_DELETE) return !event.isAltDown
        return (event.isControlDown || event.isMetaDown) && !event.isAltDown && (event.keyCode == KeyEvent.VK_V || event.keyCode == KeyEvent.VK_X) ||
            event.isShiftDown && event.keyCode == KeyEvent.VK_INSERT
    }
}
