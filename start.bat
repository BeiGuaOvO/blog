@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title blog dev launcher

rem ==================== 配置区（按需修改） ====================
set "JDK_HOME=E:\jdk21"
set "MAVEN_HOME=E:\maven"
set "MAVEN_REPO=E:\maven\repository"
set "BACKEND_PORT=8080"
set "FRONTEND_PORT=5173"
set "WAIT_SECONDS=120"
rem ==========================================================

set "ROOT=%~dp0"
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"
set "BACKEND=%ROOT%\backend"
set "FRONTEND=%ROOT%\frontend"

echo ==========================================================
echo  blog dev launcher
echo  project : %ROOT%
echo  jdk     : %JDK_HOME%
echo  maven   : %MAVEN_HOME%
echo  m2 repo : %MAVEN_REPO%
echo  backend : http://localhost:%BACKEND_PORT%/api
echo  frontend: http://localhost:%FRONTEND_PORT%
echo ==========================================================
echo.

rem ---------- 0. 目录检查 ----------
if not exist "%BACKEND%\pom.xml" (
    echo [X] not found: %BACKEND%\pom.xml
    echo     put start.bat in project root, next to backend\ and frontend\
    goto :fail
)
if not exist "%FRONTEND%\package.json" (
    echo [X] not found: %FRONTEND%\package.json
    goto :fail
)

echo "%ROOT%" | findstr /c:" " >nul
if not errorlevel 1 (
    echo [!] project path contains a space: %ROOT%
    echo     child windows may fail. consider moving to E:\blog
)

rem ---------- 1. JDK ----------
if exist "%JDK_HOME%\bin\java.exe" (
    set "JAVA_HOME=%JDK_HOME%"
    set "PATH=%JDK_HOME%\bin;%PATH%"
) else (
    echo [!] %JDK_HOME% not found, falling back to existing JAVA_HOME / PATH
    if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
)
java -version >nul 2>&1
if errorlevel 1 (
    echo [X] java not available. install JDK 17+ ^(21 recommended^)
    goto :fail
)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JV=%%~v"
echo [OK] java !JV!

rem ---------- 2. Maven ----------
set "MVN="
where mvn >nul 2>&1 && set "MVN=mvn"
if not defined MVN if exist "%MAVEN_HOME%\bin\mvn.cmd" set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
if not defined MVN if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin\mvn.cmd" set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
if not defined MVN if exist "%BACKEND%\mvnw.cmd" set "MVN=%BACKEND%\mvnw.cmd"
if not defined MVN (
    echo [X] maven not found. set MAVEN_HOME correctly ^(expect %MAVEN_HOME%\bin\mvn.cmd^)
    goto :fail
)
echo [OK] maven   !MVN!
if not exist "%MAVEN_REPO%" (
    echo [!] maven repo not found: %MAVEN_REPO%
    echo     it will be created on first build ^(slow, needs network^)
)

rem ---------- 3. MySQL ----------
sc query MySQL80 2>nul | findstr /i "RUNNING" >nul
if errorlevel 1 (
    echo [!] MySQL80 not running, trying to start...
    net start MySQL80 >nul 2>&1
    sc query MySQL80 2>nul | findstr /i "RUNNING" >nul
    if errorlevel 1 (
        echo [X] cannot start MySQL80 ^(needs administrator^), or it is a docker mysql
        echo     start it manually, then run this script again
        goto :fail
    )
)
echo [OK] MySQL80 running

rem ---------- 4. backend ----------
netstat -ano | findstr /c:":%BACKEND_PORT% " | findstr /i "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [i] port %BACKEND_PORT% already listening - skip backend start
) else (
    echo [..] starting backend ^(new window^) ...
    start "blog-backend" /d "%BACKEND%" cmd /k "chcp 65001>nul && set JAVA_HOME=%JAVA_HOME% && %MVN% -Dmaven.repo.local=%MAVEN_REPO% spring-boot:run"
)

set "HAS_CURL=0"
where curl >nul 2>&1 && set "HAS_CURL=1"

echo [..] waiting for backend to be ready ^(up to %WAIT_SECONDS%s^) ...
call :wait_url "http://localhost:%BACKEND_PORT%/api/portal/articles" %WAIT_SECONDS%
if errorlevel 1 (
    echo [X] backend not ready. check the "blog-backend" window for the real error.
    echo     common causes: wrong DB password, missing tables, port conflict
    goto :fail
)
echo [OK] backend ready → http://localhost:%BACKEND_PORT%/api/swagger-ui.html

rem ---------- 5. frontend ----------
if not exist "%FRONTEND%\node_modules" (
    echo [..] node_modules missing, running npm install ...
    pushd "%FRONTEND%"
    call npm install
    set "NPM_RC=!ERRORLEVEL!"
    popd
    if not "!NPM_RC!"=="0" (
        echo [X] npm install failed
        goto :fail
    )
)

netstat -ano | findstr /c:":%FRONTEND_PORT% " | findstr /i "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [i] port %FRONTEND_PORT% already listening - skip frontend start
) else (
    echo [..] starting frontend ^(new window^) ...
    start "blog-frontend" /d "%FRONTEND%" cmd /k "chcp 65001>nul && npm run dev"
)

echo [..] waiting for frontend to be ready ...
call :wait_url "http://localhost:%FRONTEND_PORT%/" 60
if errorlevel 1 (
    echo [!] frontend not ready yet, opening browser anyway
)

echo.
echo ==========================================================
echo  done
echo    front : http://localhost:%FRONTEND_PORT%
echo    admin : http://localhost:%FRONTEND_PORT%/login
echo    login : admin / your password
echo    api   : http://localhost:%BACKEND_PORT%/api/swagger-ui.html
echo ==========================================================
start "" http://localhost:%FRONTEND_PORT%
timeout /t 3 /nobreak >nul
exit /b 0

:fail
echo.
echo launch aborted.
pause
exit /b 1

rem ==================== 子程序 ====================
:wait_url
rem %1 = url, %2 = max seconds
set "URL=%~1"
set "MAX=%~2"
set "N=0"
:wait_url_loop
if "%HAS_CURL%"=="1" (
    curl -s -f -o NUL "%URL%" >nul 2>&1
) else (
    powershell -NoProfile -Command "try{(Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -TimeoutSec 3)|Out-Null;exit 0}catch{exit 1}" >nul 2>&1
)
if not errorlevel 1 exit /b 0
set /a "N+=1"
if !N! geq !MAX! exit /b 1
timeout /t 1 /nobreak >nul
goto :wait_url_loop