# SQL Macro

Database Tools editor shortcut macros for DataGrip and IntelliJ IDEA that expand `\$Var` from the current selection or identifier under the caret.

## Behavior

- `Alt+1` through `Alt+0` are registered as plugin actions.
- Each slot stores one SQL template.
- `\$Var` resolves in this order:
  - selected text
  - identifier under the caret
- Example:
  - template: `DESC $Var`
  - caret on `orders`
  - executed SQL: `DESC orders`

## Configure

Open `Settings > Tools > SQL Macro`.

Default examples:

- `Alt+1`: `DESC $Var`
- `Alt+2`: `SELECT * FROM $Var`

## Implementation note

This version executes SQL in a temporary JDBC console bound to the current database session, so the active editor text is not modified.

## Build

Use Gradle:

```powershell
.\gradlew build
```
