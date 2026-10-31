@echo off
chcp 65001 >nul
for %%P in (8080 5173) do (
    for /f "tokens=5" %%I in ('netstat -ano ^| findstr /c:":%%P " ^| findstr /i "LISTENING"') do (
        echo killing pid %%I on port %%P
        taskkill /PID %%I /F >nul 2>&1
    )
)
echo done.
timeout /t 2 /nobreak >nul