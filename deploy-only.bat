@echo off
setlocal

:: ============================================================
::  Claude Assistant - Deploy Only (no launch)
::  Useful when RuneLite is already running and you just want
::  to push updated plugin sources for the next restart.
:: ============================================================

set RUNELITE_DIR=%USERPROFILE%\runelite

echo.
echo  Claude Assistant - Deploy Only
echo  ================================

call gradlew.bat deployToRunelite -PruneliteDir="%RUNELITE_DIR%"

if %ERRORLEVEL% equ 0 (
    echo.
    echo  Done. Restart RuneLite dev client to pick up changes.
    echo  Run: gradlew runDev   or   deploy.bat
) else (
    echo.
    echo  Deploy failed. See output above.
)

echo.
pause
endlocal
