package com.github.pet.pycharm.reporting

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.Consumer
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.swing.*

/** User-initiated reporting from the IDE Fatal Errors dialog. */
class PetErrorReportSubmitter : ErrorReportSubmitter() {
    override fun getReportActionText() = "Report by email…"

    override fun getPrivacyNoticeText() =
        "Review the error report and open an email draft to $SUPPORT_EMAIL. " +
            "Nothing is sent automatically. The IDE cannot confirm email delivery."

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        val report = buildString {
            appendLine("Status Bar Pet ${pluginDescriptor?.version ?: "unknown"}")
            appendLine(ApplicationInfo.getInstance().fullApplicationName)
            appendLine("Java ${System.getProperty("java.version")}; ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
            if (!additionalInfo.isNullOrBlank()) appendLine("\nUser description:\n$additionalInfo")
            events.forEach { event ->
                appendLine("\n${event.message.orEmpty()}")
                appendLine(event.throwableText)
            }
        }
        EmailReportDialog(parentComponent, report).show()
        // Opening a mail client is a handoff, not confirmed delivery. Returning
        // false keeps the IDE error available instead of claiming it was sent.
        return false
    }

    private class EmailReportDialog(parent: Component, private val report: String) : DialogWrapper(parent, true) {
        init {
            title = "Report Status Bar Pet error"
            setOKButtonText("Open email draft")
            setCancelButtonText("Close")
            init()
        }

        override fun createCenterPanel(): JComponent = JPanel(BorderLayout(0, JBUI.scale(10))).apply {
            add(JLabel("<html>To: $SUPPORT_EMAIL<br>Review the report below. Copy the full report into your email before sending.</html>"), BorderLayout.NORTH)
            add(JBScrollPane(JBTextArea(report, 18, 76).apply {
                isEditable = false
                caretPosition = 0
                accessibleContext.accessibleName = "Full error report"
            }), BorderLayout.CENTER)
            add(JLabel("Nothing is sent automatically. Send the draft from your email app."), BorderLayout.SOUTH)
        }

        override fun createActions(): Array<Action> = arrayOf(object : AbstractAction("Copy full report") {
            override fun actionPerformed(e: ActionEvent) {
                CopyPasteManager.getInstance().setContents(StringSelection(report))
                putValue(Action.NAME, "Report copied")
            }
        }, okAction, cancelAction)

        override fun doOKAction() {
            BrowserUtil.browse(emailDraftUrl(report))
            setOKButtonText("Open email draft again")
        }
    }

    companion object {
        const val SUPPORT_EMAIL = "contact@darshan-bharat.com"
        private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")

        private fun emailDraftUrl(report: String): String {
            val prefix = "mailto:$SUPPORT_EMAIL?subject=${encode("Status Bar Pet error report")}&body="
            val note = "\n\n[Paste the full report copied from the IDE here.]"
            // Windows mail handlers have URL length limits. The complete report
            // remains available for explicit copying in the review dialog.
            val encodedNote = encode(note)
            val excerpt = StringBuilder()
            var offset = 0
            while (offset < report.length) {
                val codePoint = report.codePointAt(offset)
                val next = encode(String(Character.toChars(codePoint)))
                if (prefix.length + excerpt.length + next.length + encodedNote.length > 1800) break
                excerpt.append(next)
                offset += Character.charCount(codePoint)
            }
            return prefix + excerpt + encodedNote
        }
    }
}
