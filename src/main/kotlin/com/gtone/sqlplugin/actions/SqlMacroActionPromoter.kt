package com.gtone.sqlplugin.actions

import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiFile

/** Gives SQL Macro shortcuts precedence over conflicting IDE actions in SQL editors. */
class SqlMacroActionPromoter : ActionPromoter {
    override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> {
        val editor = context.getData(CommonDataKeys.EDITOR)
        val psiFile = context.getData(CommonDataKeys.PSI_FILE)

        if (editor == null || psiFile == null || !isSqlEditor(psiFile)) {
            return emptyList()
        }

        return actions.filterIsInstance<BaseRunSqlMacroAction>()
    }

    private fun isSqlEditor(psiFile: PsiFile): Boolean {
        val languageIds = buildList {
            add(psiFile.language.id)
            psiFile.viewProvider.languages.forEach { add(it.id) }
            add(psiFile.fileType.name)
        }

        return languageIds.any { id ->
            val normalized = id.uppercase()
            normalized == "SQL" || normalized.contains("SQL") || normalized.contains("DATABASE CONSOLE")
        }
    }
}
