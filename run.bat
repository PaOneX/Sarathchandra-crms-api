@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "PORT=8080"
set "AUTO_KILL=0"

if /I "%~1"=="--kill" set "AUTO_KILL=1"

echo Checking port %PORT%...

for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":%PORT% " ^| findstr LISTENING') do (
    set "PID=%%P"
    goto :found
)

echo Port %PORT% is free.
goto :start

:found
echo Port %PORT% is in use by PID %PID%.

if "%AUTO_KILL%"=="0" (
    set /p "CONFIRM=Kill process %PID% and start the app? [y/N]: "
    if /I not "!CONFIRM!"=="y" (
        echo Aborted. Stop the process manually or run: run.bat --kill
        exit /b 1
    )
)

echo Stopping PID %PID%...
taskkill /PID %PID% /F >nul 2>&1
if errorlevel 1 (
    echo Failed to stop PID %PID%. Run as administrator or stop it manually.
    exit /b 1
)

timeout /t 2 /nobreak >nul
echo Port %PORT% cleared.

:start
cd /d "%~dp0"
echo Starting Car Rental API...
call mvnw.cmd spring-boot:run
exit /b %ERRORLEVEL%
