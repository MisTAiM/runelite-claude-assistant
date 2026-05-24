@echo off
setlocal

:: ============================================================
::  Claude Assistant - One-Click Deploy + Launch
::  Edit RUNELITE_DIR if your RuneLite checkout is elsewhere
:: ============================================================

set RUNELITE_DIR=%USERPROFILE%\runelite

echo.
echo  Claude Assistant - Deploy ^& Launch
echo  ====================================
echo  RuneLite dir: %RUNELITE_DIR%
echo.

:: Check RuneLite checkout exists
if not exist "%RUNELITE_DIR%\gradlew.bat" (
    echo  ERROR: RuneLite checkout not found at %RUNELITE_DIR%
    echo.
    echo  Clone it first:
    echo    git clone https://github.com/runelite/runelite %RUNELITE_DIR%
    echo.
    echo  Or edit RUNELITE_DIR at the top of this script.
    pause
    exit /b 1
)

:: Deploy plugin sources and launch RuneLite
echo  Deploying plugin sources...
call gradlew.bat deployToRunelite -PruneliteDir="%RUNELITE_DIR%"

if %ERRORLEVEL% neq 0 (
    echo.
    echo  Deploy failed. Check output above.
    pause
    exit /b 1
)

echo.
echo  Launching RuneLite...
echo  (This will take a minute on first run while Gradle downloads dependencies)
echo.

cd /d "%RUNELITE_DIR%"
call gradlew.bat :client:run

endlocal
