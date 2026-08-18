@echo off
title Learning Env Starter (Redis + Kafka + XXL-JOB + Nacos)

echo ============================================
echo   Start learning env:
echo   Redis(6379) + Kafka(9092) + XXL-JOB(9091) + Nacos(8848)
echo ============================================
echo.

REM ===== 1. Start Redis =====
echo [1/4] Starting Redis ...
tasklist /FI "IMAGENAME eq redis-server.exe" | find /I "redis-server.exe" >nul
if %errorlevel%==0 (
    echo       Redis already running, skip.
) else (
    start "Redis" /min "C:\redis\redis-server.exe" --port 6379
    echo       Redis started at 127.0.0.1:6379
)
echo.

REM ===== 2. Start Kafka =====
echo [2/4] Starting Kafka ...
netstat -ano | find ":9092" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       Kafka already running, skip.
) else (
    start "Kafka" "%~dp0start-kafka.bat"
    echo       Kafka starting at localhost:9092 ...
)
echo.

REM ===== 3. Start XXL-JOB Admin (scheduler center) =====
echo [3/4] Starting XXL-JOB Admin ...
netstat -ano | find ":9091" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       XXL-JOB Admin already running, skip.
) else (
    start "XXL-JOB" "%~dp0start-xxljob.bat"
    echo       XXL-JOB Admin starting at localhost:9091 ...
)
echo.

REM ===== 4. Start Nacos (registry/config center) =====
echo [4/4] Starting Nacos ...
netstat -ano | find ":8848" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       Nacos already running, skip.
) else (
    start "Nacos" "%~dp0start-nacos.bat"
    echo       Nacos starting at localhost:8848 ...
)
echo.

echo ============================================
echo   Done! Services:
echo   - Redis        : 127.0.0.1:6379
echo   - Kafka        : localhost:9092
echo   - XXL-JOB Admin: http://localhost:9091/xxl-job-admin  (admin/123456)
echo   - Nacos        : http://localhost:8848/nacos  (nacos/nacos)
echo   - MySQL        : windows service (auto)
echo.
echo   Now you can start Spring Boot app:
echo   - learning-mct-api      (port 9090)
echo   - learning-merchant-api (port 9095)
echo ============================================
echo.
