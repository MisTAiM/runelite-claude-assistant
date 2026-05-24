@echo off
setlocal
set RUNELITE_DIR=%USERPROFILE%\runelite
set PLUGIN_DIR=%USERPROFILE%\runelite-claude-assistant
set SRC=%PLUGIN_DIR%\src\main\java\net\runelite\client\plugins\claudeassistant
set DEST=%RUNELITE_DIR%\runelite-client\src\main\java\net\runelite\client\plugins\claudeassistant

echo.
echo  Deploying Claude plugin sources...
xcopy /y /q "%SRC%\*.java" "%DEST%\" > nul
echo  Building...
cd /d "%RUNELITE_DIR%"
call gradlew.bat :client:shadowJar
if %ERRORLEVEL% neq 0 ( echo  Build failed. & pause & exit /b 1 )
echo.
echo  Done! Launch with 'RuneLite + Claude' shortcut on your desktop.
pause
endlocal
