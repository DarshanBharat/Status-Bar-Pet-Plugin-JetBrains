package com.github.pet.pycharm.ui

import com.github.pet.pycharm.state.PetPersistentState
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import java.time.LocalTime
import javax.swing.*

class AchievementsDialog(project: Project) : DialogWrapper(project) {

    init {
        title = "🏆 Coding Achievements & Milestones"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val state = PetPersistentState.getInstance()
        val root = JPanel(BorderLayout(0, 10)).apply {
            preferredSize = Dimension(JBUI.scale(480), JBUI.scale(420))
            border = JBUI.Borders.empty(12)
        }

        val rank = state.getDeveloperRank()
        val header = JLabel("<html><b>🐾 Companion: ${state.getPetName()}</b> &bull; Rank: <span style='color: #FA8231;'><b>$rank (Lv. ${state.level})</b></span><br/>" +
                "<span style='color: #888;'>Typed edits: ${state.manualEditCount} (+${state.totalKeystrokes}/−${state.manualCharsRemoved} chars)<br/>" +
                "Automated edits: ${state.otherEditCount + state.aiAgentEditCount} (+${state.otherCharsAdded + state.aiAgentCharsAdded}/−${state.otherCharsRemoved + state.aiAgentCharsRemoved} chars)<br/>" +
                "Coding: ${state.totalCodingMinutes}m | Streak: ${state.streakDays}d</span></html>").apply {
            font = Font("Segoe UI", Font.PLAIN, 12)
            border = JBUI.Borders.emptyBottom(8)
        }
        root.add(header, BorderLayout.NORTH)

        val listPanel = JPanel(GridLayout(6, 1, 6, 6))

        val achievements = listOf(
            Achievement("🦉 Night Owl", "Coded past 10 PM", state.nightOwlUnlocked),
            Achievement("⚡ Speed Demon", "Reached 1,000+ keystrokes", state.totalKeystrokes >= 1000),
            Achievement("🧪 Test Master", "Passed 5+ clean test runs", state.passedTestsCount >= 5),
            Achievement("📦 Git Champion", "Completed a code commit", state.commitsCount >= 1),
            Achievement("🔥 Streak Keeper", "Maintained 3+ days coding streak", state.streakDays >= 3),
            Achievement("☕ Deep Flow", "Completed a 25-minute Pomodoro focus sprint", state.completedPomodoros >= 1)
        )

        for (ach in achievements) {
            val statusIcon = if (ach.unlocked) "⭐ UNLOCKED" else "🔒 LOCKED"
            val color = if (ach.unlocked) "#2ECC71" else "#888888"
            val item = JLabel("<html><b>${ach.title}</b> &bull; <span style='color: $color;'>$statusIcon</span><br/><span style='color: #888;'>${ach.desc}</span></html>").apply {
                font = Font("Segoe UI", Font.PLAIN, 11)
                border = JBUI.Borders.compound(
                    JBUI.Borders.customLine(if (ach.unlocked) java.awt.Color(0x2E, 0xCC, 0x71) else java.awt.Color(0x55, 0x55, 0x55), 1),
                    JBUI.Borders.empty(4, 8)
                )
            }
            listPanel.add(item)
        }

        root.add(JScrollPane(listPanel).apply { border = JBUI.Borders.empty() }, BorderLayout.CENTER)
        return root
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction.apply { putValue(Action.NAME, "Awesome!") })
    }

    private data class Achievement(val title: String, val desc: String, val unlocked: Boolean)
}
