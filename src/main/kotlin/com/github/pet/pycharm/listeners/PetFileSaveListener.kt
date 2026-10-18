package com.github.pet.pycharm.listeners

import com.github.pet.pycharm.ui.PetStatusBarWidget
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManagerListener

class PetFileSaveListener : FileDocumentManagerListener {

    override fun beforeDocumentSaving(document: Document) {
        PetStatusBarWidget.notifySaved()
    }
}
