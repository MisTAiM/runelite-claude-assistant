# ============================================================
#  Claude Assistant - Full Automated Setup Script
#  Right-click -> Run with PowerShell (as Admin recommended)
# ============================================================

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

function Write-Step($msg) { Write-Host "`n  >> $msg" -ForegroundColor Cyan }
function Write-OK($msg)   { Write-Host "  OK  $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "  !!  $msg" -ForegroundColor Yellow }
function Write-Fail($msg) { Write-Host "  XX  $msg" -ForegroundColor Red }

Clear-Host
Write-Host ""
Write-Host "  ================================================" -ForegroundColor DarkRed
Write-Host "   Claude Assistant - RuneLite Plugin Setup" -ForegroundColor White
Write-Host "  ================================================" -ForegroundColor DarkRed
Write-Host ""

$RuneliteDir  = "$env:USERPROFILE\runelite"
$PluginDir    = "$env:USERPROFILE\runelite-claude-assistant"
$PluginGitUrl = "https://github.com/MisTAiM/runelite-claude-assistant.git"
$RuneliteUrl  = "https://github.com/runelite/runelite.git"
$JarPattern   = "$RuneliteDir\runelite-client\build\libs\client-*-shadow.jar"

# ── 1. Check Git ──────────────────────────────────────────────────────────────
Write-Step "Checking for Git..."
try {
    $gitVersion = git --version 2>&1
    Write-OK "Git found: $gitVersion"
} catch {
    Write-Fail "Git not found!"
    Write-Host "  Install Git from: https://git-scm.com/download/win" -ForegroundColor Yellow
    Read-Host "`n  Press Enter to open download page..."
    Start-Process "https://git-scm.com/download/win"
    exit 1
}

# ── 2. Clone plugin repo ──────────────────────────────────────────────────────
Write-Step "Setting up Claude Assistant plugin..."
if (Test-Path "$PluginDir\.git") {
    Write-OK "Plugin repo already exists - pulling latest..."
    Set-Location $PluginDir
    git pull origin main
} else {
    Write-Host "  Cloning from GitHub..." -ForegroundColor Gray
    git clone $PluginGitUrl $PluginDir
    Write-OK "Plugin cloned to: $PluginDir"
}

# ── 3. Clone RuneLite source ──────────────────────────────────────────────────
Write-Step "Setting up RuneLite source..."
if (Test-Path "$RuneliteDir\.git") {
    Write-OK "RuneLite already cloned at $RuneliteDir"
} else {
    Write-Host "  Cloning RuneLite (~150MB, may take a few minutes)..." -ForegroundColor Gray
    git clone --depth=1 $RuneliteUrl $RuneliteDir
    Write-OK "RuneLite cloned to: $RuneliteDir"
}

# ── 4. Deploy plugin into RuneLite ────────────────────────────────────────────
Write-Step "Deploying plugin into RuneLite source tree..."
$src  = "$PluginDir\src\main\java\net\runelite\client\plugins\claudeassistant"
$dest = "$RuneliteDir\runelite-client\src\main\java\net\runelite\client\plugins\claudeassistant"
New-Item -ItemType Directory -Force -Path $dest | Out-Null
Copy-Item "$src\*.java" $dest -Force
Write-OK "Plugin sources deployed"

# ── 5. Build shadow jar ───────────────────────────────────────────────────────
Write-Step "Building RuneLite + Claude plugin (this takes a few minutes first time)..."
Set-Location $RuneliteDir
.\gradlew.bat :client:shadowJar
if ($LASTEXITCODE -ne 0) {
    Write-Fail "Build failed. See output above."
    Read-Host "Press Enter to exit"
    exit 1
}
Write-OK "Build successful"

# ── 6. Create desktop shortcuts ───────────────────────────────────────────────
Write-Step "Creating desktop shortcuts..."
$WshShell = New-Object -ComObject WScript.Shell
$Desktop  = [System.Environment]::GetFolderPath("Desktop")

# Find the built jar
$jar = (Get-Item $JarPattern | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName
Write-OK "Found jar: $jar"

# Launch shortcut
$sc = $WshShell.CreateShortcut("$Desktop\RuneLite + Claude.lnk")
$sc.TargetPath       = "javaw.exe"
$sc.Arguments        = "-jar `"$jar`""
$sc.WorkingDirectory = $RuneliteDir
$sc.WindowStyle      = 1
$sc.Description      = "Launch RuneLite with Claude Assistant plugin"
$sc.Save()
Write-OK "Desktop shortcut created: 'RuneLite + Claude'"

# Update shortcut
$sc2 = $WshShell.CreateShortcut("$Desktop\Update Claude Plugin.lnk")
$sc2.TargetPath       = "powershell.exe"
$sc2.Arguments        = "-NoExit -Command `"cd '$PluginDir'; git pull origin main; Copy-Item 'src\main\java\net\runelite\client\plugins\claudeassistant\*.java' '$dest' -Force; cd '$RuneliteDir'; .\gradlew.bat :client:shadowJar; Write-Host 'Done! Relaunch RuneLite + Claude.' -ForegroundColor Green`""
$sc2.WorkingDirectory = $PluginDir
$sc2.WindowStyle      = 1
$sc2.Description      = "Pull latest Claude plugin and rebuild"
$sc2.Save()
Write-OK "Desktop shortcut created: 'Update Claude Plugin'"

# ── Done ──────────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "  ================================================" -ForegroundColor DarkGreen
Write-Host "   Setup Complete!" -ForegroundColor Green
Write-Host "  ================================================" -ForegroundColor DarkGreen
Write-Host ""
Write-Host "  Next steps:" -ForegroundColor White
Write-Host "   1. Double-click 'RuneLite + Claude' on your desktop" -ForegroundColor Gray
Write-Host "   3. Config (wrench icon) -> Claude Assistant -> paste your Anthropic API key" -ForegroundColor Gray
Write-Host "   4. Click the red C icon in the sidebar and start chatting" -ForegroundColor Gray
Write-Host ""
Write-Host "  Plugin repo : $PluginDir" -ForegroundColor DarkGray
Write-Host "  RuneLite    : $RuneliteDir" -ForegroundColor DarkGray
Write-Host "  Built jar   : $jar" -ForegroundColor DarkGray
Write-Host ""

$launch = Read-Host "  Launch RuneLite now? (y/n)"
if ($launch -eq 'y' -or $launch -eq 'Y') {
    Write-Host "`n  Launching..." -ForegroundColor Cyan
    Start-Process "javaw.exe" -ArgumentList "-jar `"$jar`""
}
