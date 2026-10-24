package com.github.pet.pycharm.tracking

import com.github.pet.pycharm.state.PetPersistentState
import com.github.pet.pycharm.ui.PetStatusBarWidget
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectFileIndex
import java.awt.Component
import java.awt.EventQueue
import java.awt.event.KeyEvent

/** One application listener also covers documents shared by split editors. */
class PetEditTracker : Disposable {
    private var lastConflictNoticeAt: Long = 0L

    init {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                if (event.newLength == event.oldLength && event.newFragment.contentEquals(event.oldFragment)) return
                val file = FileDocumentManager.getInstance().getFile(event.document) ?: return
                if (file.fileType.isBinary) return
                val project = ProjectManager.getInstance().openProjects.firstOrNull {
                    !it.isDisposed && ProjectFileIndex.getInstance(it).isInContent(file)
                } ?: return

                if (event.newFragment.contains("<<<<<<<") && System.currentTimeMillis() - lastConflictNoticeAt > 30_000L) {
                    lastConflictNoticeAt = System.currentTimeMillis()
                    PetStatusBarWidget.notifyMergeConflict(file.name, project)
                }

                val state = PetPersistentState.getInstance()
                val command = CommandProcessor.getInstance().currentCommandName.orEmpty()
                val input = EventQueue.getCurrentEvent()
                if (EditOriginClassifier.isManual(command, input is KeyEvent && EditOriginClassifier.isDirectTyping(input))) {
                    val leveled = state.recordManualEdit(event.newLength, event.oldLength)
                    if (event.oldLength > 0 && event.newLength == 0) PetStatusBarWidget.notifyDeletion(event.oldLength, project)
                    else if (event.newLength > 0) PetStatusBarWidget.notifyLiveTyping(event.newLength, project)
                    if (leveled) PetStatusBarWidget.notifyLevelUp(state.level, state.getPetName())
                } else {
                    state.recordOtherEdit(event.newLength, event.oldLength)
                }
            }
        }, this)

        // Capture direct user typing in terminal or console windows where EditorFactory document events are not fired.
        IdeEventQueue.getInstance().addPostprocessor(IdeEventQueue.EventDispatcher { awtEvent ->
            if (awtEvent is KeyEvent && EditOriginClassifier.isDirectTyping(awtEvent)) {
                val source = awtEvent.component ?: (awtEvent.source as? Component)
                val className = source?.javaClass?.name.orEmpty()
                if (!className.contains("EditorComponent")) {
                    val state = PetPersistentState.getInstance()
                    val leveled = state.recordManualEdit(1, 0)
                    val project = ProjectManager.getInstance().openProjects.firstOrNull { !it.isDisposed }
                    PetStatusBarWidget.notifyLiveTyping(1, project)
                    if (leveled) PetStatusBarWidget.notifyLevelUp(state.level, state.getPetName())
                }
            }
            false
        }, this)
    }
    override fun dispose() = Unit
}
