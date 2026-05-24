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

# ── 1. Check Git ──────────────────────────────────────────────────────────────
Write-Step "Checking for Git..."
try {
    $gitVersion = git --version 2>&1
    Write-OK "Git found: $gitVersion"
} catch {
    Write-Fail "Git not found!"
    Write-Host ""
    Write-Host "  Install Git from: https://git-scm.com/download/win" -ForegroundColor Yellow
    Write-Host "  Then re-run this script." -ForegroundColor Yellow
    Read-Host "`n  Press Enter to open download page..."
    Start-Process "https://git-scm.com/download/win"
    exit 1
}

# ── 3. Clone plugin repo ──────────────────────────────────────────────────────
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

# ── 4. Clone RuneLite source ──────────────────────────────────────────────────
Write-Step "Setting up RuneLite source..."
if (Test-Path "$RuneliteDir\.git") {
    Write-OK "RuneLite already cloned - skipping (use 'git pull' in $RuneliteDir to update)"
} else {
    Write-Host "  Cloning RuneLite (this is ~150MB, may take a few minutes)..." -ForegroundColor Gray
    git clone --depth=1 $RuneliteUrl $RuneliteDir
    Write-OK "RuneLite cloned to: $RuneliteDir"
}

# ── 5. Deploy plugin into RuneLite ────────────────────────────────────────────
Write-Step "Deploying plugin into RuneLite source tree..."
$src  = "$PluginDir\src\main\java\net\runelite\client\plugins\claudeassistant"
$dest = "$RuneliteDir\runelite-client\src\main\java\net\runelite\client\plugins\claudeassistant"

New-Item -ItemType Directory -Force -Path $dest | Out-Null
Copy-Item "$src\*.java" $dest -Force
Write-OK "Plugin sources deployed to RuneLite"

# ── 6. First build check ──────────────────────────────────────────────────────
Write-Step "Verifying RuneLite can find the plugin (compiling)..."
Set-Location $RuneliteDir
try {
    .\gradlew.bat :client:compileJava --quiet 2>&1 | Tail -5
    Write-OK "Compile successful - plugin is wired in correctly"
} catch {
    Write-Warn "Compile had warnings/errors - see output above"
    Write-Host "  This is sometimes normal on first run due to Gradle downloading deps." -ForegroundColor Gray
}

# ── 7. Create desktop shortcuts ───────────────────────────────────────────────
Write-Step "Creating desktop shortcuts..."

$WshShell = New-Object -ComObject WScript.Shell
$Desktop  = [System.Environment]::GetFolderPath("Desktop")

# Shortcut: Launch RuneLite with Claude plugin
$sc = $WshShell.CreateShortcut("$Desktop\RuneLite + Claude.lnk")
$sc.TargetPath       = "cmd.exe"
$sc.Arguments        = "/c cd /d `"$RuneliteDir`" && gradlew.bat :client:run"
$sc.WorkingDirectory = $RuneliteDir
$sc.WindowStyle      = 1
$sc.Description      = "Launch RuneLite with Claude Assistant plugin"
$sc.Save()
Write-OK "Desktop shortcut created: 'RuneLite + Claude'"

# Shortcut: Update plugin
$sc2 = $WshShell.CreateShortcut("$Desktop\Update Claude Plugin.lnk")
$sc2.TargetPath       = "cmd.exe"
$sc2.Arguments        = "/k cd /d `"$PluginDir`" && gradlew.bat updatePlugin -PruneliteDir=`"$RuneliteDir`""
$sc2.WorkingDirectory = $PluginDir
$sc2.WindowStyle      = 1
$sc2.Description      = "Pull latest Claude plugin from GitHub and deploy"
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
Write-Host "   2. Wait for RuneLite to compile + launch (~2 min first time)" -ForegroundColor Gray
Write-Host "   3. In RuneLite: Config -> Claude Assistant -> paste your API key" -ForegroundColor Gray
Write-Host "   4. Click the red C icon in the sidebar and start chatting" -ForegroundColor Gray
Write-Host ""
Write-Host "  Plugin repo : $PluginDir" -ForegroundColor DarkGray
Write-Host "  RuneLite    : $RuneliteDir" -ForegroundColor DarkGray
Write-Host ""

Read-Host "  Press Enter to launch RuneLite now (or close to launch later)"

Set-Location $RuneliteDir
.\gradlew.bat :client:run
