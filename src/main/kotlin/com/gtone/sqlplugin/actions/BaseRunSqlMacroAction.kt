package com.gtone.sqlplugin.actions

import com.gtone.sqlplugin.runtime.SqlMacroExecutor
import com.gtone.sqlplugin.settings.SqlMacroSettingsService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiFile

abstract class BaseRunSqlMacroAction(private val slot: String) : AnAction(), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val template = SqlMacroSettingsService.getInstance().templateFor(slot)
        e.presentation.isEnabledAndVisible =
            editor != null &&
                psiFile != null &&
                isSqlEditor(psiFile) && 
                template.enabled &&
                template.sql.isNotBlank()
    }

    override fun actionPerformed(e: AnActionEvent) {
        SqlMacroExecutor.run(e, slot)
    }

    private fun isSqlEditor(psiFile: PsiFile): Boolean {
        val languageIds = buildList {
            add(psiFile.language.id)
            psiFile.viewProvider.languages.forEach { add(it.id) }
            psiFile.fileType.name.let { add(it) }
        }

        return languageIds.any { id ->
            val normalized = id.uppercase()
            normalized == "SQL" ||
                normalized.contains("SQL") ||
                normalized.contains("DATABASE CONSOLE")
        }
    }
}

class RunSqlMacro1Action : BaseRunSqlMacroAction("1")
class RunSqlMacro2Action : BaseRunSqlMacroAction("2")
class RunSqlMacro3Action : BaseRunSqlMacroAction("3")
class RunSqlMacro4Action : BaseRunSqlMacroAction("4")
class RunSqlMacro5Action : BaseRunSqlMacroAction("5")
class RunSqlMacro6Action : BaseRunSqlMacroAction("6")
class RunSqlMacro7Action : BaseRunSqlMacroAction("7")
class RunSqlMacro8Action : BaseRunSqlMacroAction("8")
class RunSqlMacro9Action : BaseRunSqlMacroAction("9")
class RunSqlMacro0Action : BaseRunSqlMacroAction("0")
