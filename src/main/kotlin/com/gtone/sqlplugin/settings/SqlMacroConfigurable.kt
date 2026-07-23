package com.gtone.sqlplugin.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import java.awt.Dimension
import javax.swing.JComponent

class SqlMacroConfigurable : BoundConfigurable("SQL Macro") {
    override fun createPanel(): DialogPanel {
        val service = SqlMacroSettingsService.getInstance()
        return panel {
            row {
                text(
                    "Use \$Var for the selected text or the identifier under the caret. " +
                        "Example: DESC \$Var"
                )
            }
            group("Shortcut Slots") {
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { slot ->
                    val template = service.templateFor(slot)
                    row("Alt+$slot") {
                        checkBox("Enabled").bindSelected(template::enabled)
                        val sqlField = textField().bindText(template::sql).component
                        button("Edit SQL...") {
                            val dialog = SqlMacroEditorDialog(slot, sqlField.text)
                            if (dialog.showAndGet()) {
                                sqlField.text = dialog.sql
                            }
                        }
                    }
                }
            }
        }
    }
}

private class SqlMacroEditorDialog(slot: String, initialSql: String) : DialogWrapper(true) {
    private val sqlEditor = JBTextArea(initialSql, 20, 88)

    init {
        title = "Edit SQL Macro (Alt+$slot)"
        init()
    }

    override fun createCenterPanel(): JComponent = JBScrollPane(sqlEditor).apply {
        preferredSize = Dimension(780, 460)
    }

    val sql: String
        get() = sqlEditor.text
}
