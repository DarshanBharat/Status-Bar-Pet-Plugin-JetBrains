package com.github.pet.pycharm.listeners

import com.github.pet.pycharm.state.PetPersistentState
import com.github.pet.pycharm.ui.PetStatusBarWidget
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import java.util.concurrent.ConcurrentHashMap

class PetEditorFactoryListener : EditorFactoryListener {

    private val documentListeners = ConcurrentHashMap<Int, DocumentListener>()

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val document = editor.document
        val listener = object : DocumentListener {
            override fun documentChanged(docEvent: DocumentEvent) {
                val newFrag = docEvent.newFragment
                val oldFrag = docEvent.oldFragment
                val state = PetPersistentState.getInstance()

                if (newFrag.isNotEmpty()) {
                    PetStatusBarWidget.notifyLiveTyping(newFrag.length)
                    val leveledUp = state.recordKeystrokes(newFrag.length)
                    if (leveledUp) {
                        PetStatusBarWidget.notifyLevelUp(state.level, state.getPetName())
                    }
                } else if (oldFrag.isNotEmpty() && newFrag.isEmpty()) {
                    PetStatusBarWidget.notifyDeletion(oldFrag.length)
                }
            }
        }
        document.addDocumentListener(listener)
        documentListeners[System.identityHashCode(editor)] = listener
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor
        val listener = documentListeners.remove(System.identityHashCode(editor))
        if (listener != null) {
            runCatching {
                editor.document.removeDocumentListener(listener)
            }
        }
    }
}
