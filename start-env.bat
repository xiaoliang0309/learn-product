@echo off
title Learning Env Starter (Redis + Kafka + XXL-JOB Admin)

echo ============================================
echo   Start learning env:
echo   Redis(6379) + Kafka(9092) + XXL-JOB(9091)
echo ============================================
echo.

REM ===== 1. Start Redis =====
echo [1/3] Starting Redis ...
tasklist /FI "IMAGENAME eq redis-server.exe" | find /I "redis-server.exe" >nul
if %errorlevel%==0 (
    echo       Redis already running, skip.
) else (
    start "Redis" /min "C:\redis\redis-server.exe" --port 6379
    echo       Redis started at 127.0.0.1:6379
)
echo.

REM ===== 2. Start Kafka =====
echo [2/3] Starting Kafka ...
netstat -ano | find ":9092" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       Kafka already running, skip.
) else (
    start "Kafka" "%~dp0start-kafka.bat"
    echo       Kafka starting at localhost:9092 ...
)
echo.

REM ===== 3. Start XXL-JOB Admin (scheduler center) =====
echo [3/3] Starting XXL-JOB Admin ...
netstat -ano | find ":9091" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       XXL-JOB Admin already running, skip.
) else (
    start "XXL-JOB" "%~dp0start-xxljob.bat"
    echo       XXL-JOB Admin starting at localhost:9091 ...
)
echo.

echo ============================================
echo   Done! Services:
echo   - Redis        : 127.0.0.1:6379
echo   - Kafka        : localhost:9092
echo   - XXL-JOB Admin: http://localhost:9091/xxl-job-admin  (admin/123456)
echo   - MySQL        : windows service (auto)
echo.
echo   Now you can start Spring Boot app (port 9090)
echo ============================================
echo.
