package com.gtone.sqlplugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

data class SqlMacroTemplate(
    var enabled: Boolean = false,
    var sql: String = ""
)

data class SqlMacroSettingsState(
    var templates: MutableMap<String, SqlMacroTemplate> = mutableMapOf(
        "1" to SqlMacroTemplate(
            true,
            """
            SELECT C.COLUMN_ID
                 , C.COLUMN_NAME
                 , C.DATA_TYPE
                 , C.DATA_LENGTH
                 , C.DATA_PRECISION
                 , C.DATA_SCALE
                 , C.NULLABLE
                 , C.DATA_DEFAULT
                 , CASE WHEN PK.COLUMN_NAME IS NOT NULL THEN 'Y' ELSE 'N' END AS IS_PK
                 , CASE WHEN FK.COLUMN_NAME IS NOT NULL THEN 'Y' ELSE 'N' END AS IS_FK
              FROM USER_TAB_COLUMNS C
                   LEFT JOIN (
                        SELECT UCC.TABLE_NAME, UCC.COLUMN_NAME
                          FROM USER_CONS_COLUMNS UCC
                          JOIN USER_CONSTRAINTS UC
                            ON UC.CONSTRAINT_NAME = UCC.CONSTRAINT_NAME
                           AND UC.TABLE_NAME = UCC.TABLE_NAME
                         WHERE UC.CONSTRAINT_TYPE = 'P'
                   ) PK
                ON PK.TABLE_NAME = C.TABLE_NAME
               AND PK.COLUMN_NAME = C.COLUMN_NAME
                   LEFT JOIN (
                        SELECT UCC.TABLE_NAME, UCC.COLUMN_NAME
                          FROM USER_CONS_COLUMNS UCC
                          JOIN USER_CONSTRAINTS UC
                            ON UC.CONSTRAINT_NAME = UCC.CONSTRAINT_NAME
                           AND UC.TABLE_NAME = UCC.TABLE_NAME
                         WHERE UC.CONSTRAINT_TYPE = 'R'
                    ) FK
                ON FK.TABLE_NAME = C.TABLE_NAME
               AND FK.COLUMN_NAME = C.COLUMN_NAME
             WHERE C.TABLE_NAME = UPPER('${'$'}Var')
             ORDER BY C.COLUMN_ID
            """.trimIndent()
        ),
        "2" to SqlMacroTemplate(true, "SELECT * FROM \$Var"),
        "3" to SqlMacroTemplate(true, "SELECT COUNT(*) FROM \$Var"),
        "4" to SqlMacroTemplate(),
        "5" to SqlMacroTemplate(),
        "6" to SqlMacroTemplate(),
        "7" to SqlMacroTemplate(),
        "8" to SqlMacroTemplate(),
        "9" to SqlMacroTemplate(),
        "0" to SqlMacroTemplate()
    )
)

@Service(Service.Level.APP)
@State(name = "SqlMacroSettings", storages = [Storage("jetbrainsplugin-sql-macro.xml")])
class SqlMacroSettingsService : PersistentStateComponent<SqlMacroSettingsState> {
    private var state = SqlMacroSettingsState()

    override fun getState(): SqlMacroSettingsState = state

    override fun loadState(state: SqlMacroSettingsState) {
        this.state = state
    }

    fun templateFor(slot: String): SqlMacroTemplate = state.templates.getOrPut(slot) { SqlMacroTemplate() }

    companion object {
        fun getInstance(): SqlMacroSettingsService =
            ApplicationManager.getApplication().getService(SqlMacroSettingsService::class.java)
    }
}
