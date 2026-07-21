package com.gtone.sqlplugin.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

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
                        textField()
                            .bindText(template::sql)
                    }
                }
            }
        }
    }
}
