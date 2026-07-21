package com.gtone.sqlplugin.runtime

import com.gtone.sqlplugin.settings.SqlMacroSettingsService
import com.intellij.database.console.JdbcConsole
import com.intellij.database.console.JdbcConsoleProvider
import com.intellij.database.settings.DatabaseSettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile

object SqlMacroExecutor {
    private const val MACRO_CONSOLE_PREFIX = "SQL Macro"

    fun run(event: AnActionEvent, slot: String) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val sourceFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        if (sourceFile == null) {
            showError(project, "No database file is associated with the current editor.")
            return
        }

        val template = SqlMacroSettingsService.getInstance().templateFor(slot)
        if (!template.enabled || template.sql.isBlank()) {
            showError(project, "Alt+$slot macro is not configured.")
            return
        }

        val variable = resolveVariable(editor)
        if (variable.isNullOrBlank()) {
            showError(project, "No selection or identifier under the caret.")
            return
        }

        val sql = template.sql.replace("\$Var", variable).trim()
        if (sql.isBlank()) {
            showError(project, "The generated SQL is empty.")
            return
        }

        executeInConsole(project, sourceFile, sql, slot)
    }

    private fun executeInConsole(project: Project, sourceFile: VirtualFile, sql: String, slot: String) {
        val session = JdbcConsoleProvider.findOrCreateSession(project, sourceFile)
        if (session == null) {
            showError(project, "Could not resolve an active database session for this file.")
            return
        }

        val consoleFile = LightVirtualFile("$MACRO_CONSOLE_PREFIX $slot.sql", sourceFile.fileType, "")
        val console = JdbcConsole.newConsole(project)
            .forFile(consoleFile)
            .fromDataSource(session.connectionPoint)
            .useSession(session)
            .build()

        val statement = ensureStatementTerminator(sql)
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.writeCommandAction(project)
                .withName("Prepare SQL Macro Console")
                .run<RuntimeException> {
                    console.document.setText(statement)
                }

            val consoleEditor = console.consoleView.consoleEditor
            consoleEditor.caretModel.moveToOffset(console.document.textLength)
            val model = console.scriptModel
            if (!console.beforeExecuteQueries(model)) {
                return@invokeLater
            }

            console.executeQueries(consoleEditor, model, DatabaseSettings.ExecOption())
        }
    }

    private fun ensureStatementTerminator(sql: String): String =
        if (sql.trimEnd().endsWith(";")) sql else "$sql;"

    private fun resolveVariable(editor: Editor): String? {
        val selection = editor.selectionModel.selectedText?.trim()
        if (!selection.isNullOrBlank()) {
            return selection
        }

        val text = editor.document.charsSequence
        if (text.isEmpty()) {
            return null
        }

        val offset = editor.caretModel.offset.coerceIn(0, text.length - 1)
        if (!isIdentifierChar(text[offset]) && offset > 0 && isIdentifierChar(text[offset - 1])) {
            return readIdentifier(text, offset - 1)
        }

        return if (isIdentifierChar(text[offset])) readIdentifier(text, offset) else null
    }

    private fun readIdentifier(text: CharSequence, index: Int): String {
        var start = index
        var end = index

        while (start > 0 && isIdentifierChar(text[start - 1])) {
            start--
        }
        while (end < text.length - 1 && isIdentifierChar(text[end + 1])) {
            end++
        }

        return text.subSequence(start, end + 1).toString()
    }

    private fun isIdentifierChar(ch: Char): Boolean =
        ch.isLetterOrDigit() || ch == '_' || ch == '.' || ch == '"' || ch == '`'

    private fun showError(project: Project, message: String) {
        Messages.showErrorDialog(project, message, "SQL Macro")
    }
}
