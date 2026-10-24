package com.github.pet.pycharm.listeners

import com.github.pet.pycharm.state.PetPersistentState
import com.github.pet.pycharm.ui.PetStatusBarWidget
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment

class PetExecutionListener : ExecutionListener {

    override fun processStarted(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler
    ) {
        val runName = env.runProfile.name
        val isTest = runName.contains("test", ignoreCase = true) ||
                     runName.contains("pytest", ignoreCase = true) ||
                     runName.contains("spec", ignoreCase = true) ||
                     executorId.contains("test", ignoreCase = true)
        if (executorId.contains("debug", ignoreCase = true)) {
            PetStatusBarWidget.notifyRunStarted(runName, true, env.project, env.executionId)
        } else if (isTest) {
            PetStatusBarWidget.notifyWatchingTests(runName, env.project, env.executionId)
        } else {
            PetStatusBarWidget.notifyRunStarted(runName, false, env.project, env.executionId)
        }
    }

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        PetStatusBarWidget.notifyRunStopped(env.executionId, env.project)
        val runName = env.runProfile.name
        val isTest = runName.contains("test", ignoreCase = true) ||
                     runName.contains("pytest", ignoreCase = true) ||
                     runName.contains("spec", ignoreCase = true) ||
                     executorId.contains("test", ignoreCase = true)

        if (exitCode == 0) {
            val state = PetPersistentState.getInstance()
            val leveledUp = state.recordRunSuccess(isTest)
            PetStatusBarWidget.notifyRunFinished(runName, isSuccess = true, isTest = isTest, project = env.project)
            if (leveledUp) PetStatusBarWidget.notifyLevelUp(state.level, state.getPetName())
        } else if (exitCode !in setOf(130, 137, -1, -1073741510)) { // User cancellation on Unix and Windows.
            PetStatusBarWidget.notifyRunFinished(runName, isSuccess = false, isTest = isTest, project = env.project)
        }
    }
}
