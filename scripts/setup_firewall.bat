@echo off
echo =======================================================
echo Adding MediSlot FastAPI TCP 8000 Inbound Firewall Rule
echo =======================================================
echo.

netsh advfirewall firewall show rule name="MediSlot FastAPI 8000" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Firewall rule 'MediSlot FastAPI 8000' already exists. Updating...
    netsh advfirewall firewall delete rule name="MediSlot FastAPI 8000"
)

netsh advfirewall firewall add rule name="MediSlot FastAPI 8000" dir=in action=allow protocol=TCP localport=8000 profile=any
if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS: Firewall rule created allowing TCP 8000 across Domain, Private, and Public profiles!
) else (
    echo.
    echo ERROR: Failed to set firewall rule. Please right-click this script and select 'Run as administrator'.
)

pause
