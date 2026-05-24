@echo off
setlocal

:: ============================================================
::  Claude Assistant - Update from GitHub + Deploy + Launch
:: ============================================================

set RUNELITE_DIR=%USERPROFILE%\runelite

echo.
echo  Claude Assistant - Update ^& Launch
echo  =====================================
echo  Pulling latest from GitHub...
echo.

git pull origin main
if %ERRORLEVEL% neq 0 (
    echo  Git pull failed. Are you in the right directory?
    pause
    exit /b 1
)

echo.
echo  Deploying to RuneLite...
call gradlew.bat updatePlugin -PruneliteDir="%RUNELITE_DIR%"

if %ERRORLEVEL% neq 0 (
    echo  Deploy failed.
    pause
    exit /b 1
)

echo.
echo  Launching RuneLite...
cd /d "%RUNELITE_DIR%"
call gradlew.bat :runelite-client:run

endlocal
