package com.github.pet.pycharm.listeners

import com.github.pet.pycharm.state.PetPersistentState
import com.github.pet.pycharm.ui.PetStatusBarWidget
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory

class PetCheckinHandlerFactory : CheckinHandlerFactory() {

    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return object : CheckinHandler() {
            override fun beforeCheckin(): ReturnResult {
                val now = java.time.LocalDateTime.now()
                val isFridayEvening = now.dayOfWeek == java.time.DayOfWeek.FRIDAY && now.hour >= 16
                val filesCount = panel.files.size

                if (isFridayEvening) {
                    PetStatusBarWidget.notifyFridayPushMeme()
                } else if (filesCount >= 10) {
                    PetStatusBarWidget.notifyMonsterDiffMeme(filesCount)
                } else if (now.hour >= 23 || now.hour < 5) {
                    PetStatusBarWidget.notifyLateNightMeme()
                }
                return ReturnResult.COMMIT
            }

            override fun checkinSuccessful() {
                val msg = panel.commitMessage
                val state = PetPersistentState.getInstance()
                state.commitsCount += 1
                val leveledUp = state.addXp(50)
                PetStatusBarWidget.notifyCommitSuccess(msg, leveledUp)
            }
        }
    }
}
