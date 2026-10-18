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
        if (isTest) {
            PetStatusBarWidget.notifyWatchingTests(runName)
        }
    }

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        val runName = env.runProfile.name
        val isTest = runName.contains("test", ignoreCase = true) ||
                     runName.contains("pytest", ignoreCase = true) ||
                     runName.contains("spec", ignoreCase = true) ||
                     executorId.contains("test", ignoreCase = true)

        if (exitCode == 0) {
            val state = PetPersistentState.getInstance()
            state.successfulRunsCount += 1
            if (isTest) {
                state.passedTestsCount += 1
                state.addXp(25) // +25 XP for passing tests!
            } else {
                state.addXp(10) // +10 XP for clean run
            }
            PetStatusBarWidget.notifyRunFinished(runName, isSuccess = true, isTest = isTest)
        } else if (exitCode != 0 && exitCode != 130 && exitCode != 137) { // Ignore SIGINT/SIGKILL
            PetStatusBarWidget.notifyRunFinished(runName, isSuccess = false, isTest = isTest)
        }
    }
}
