@echo off
title Learning Env Starter (Redis + MongoDB + Kafka + XXL-JOB + Nacos + ES)

echo ============================================
echo   Start learning env:
echo   Redis(6379) + MongoDB(27017) + Kafka(9092) + XXL-JOB(9091) + Nacos(8848) + ES(9200)
echo ============================================
echo.

REM ===== 1. Start Redis =====
echo [1/5] Starting Redis ...
tasklist /FI "IMAGENAME eq redis-server.exe" | find /I "redis-server.exe" >nul
if %errorlevel%==0 (
    echo       Redis already running, skip.
) else (
    start "Redis" /min "C:\redis\redis-server.exe" --port 6379
    echo       Redis started at 127.0.0.1:6379
)
echo.

REM ===== 2. Start MongoDB =====
echo [2/6] Starting MongoDB ...
netstat -ano | find ":27017" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       MongoDB already running, skip.
) else (
    start "MongoDB" "%~dp0start-mongodb.bat"
    echo       MongoDB starting at localhost:27017 ...
)
echo.

REM ===== 3. Start Kafka =====
echo [3/6] Starting Kafka ...
netstat -ano | find ":9092" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       Kafka already running, skip.
) else (
    start "Kafka" "%~dp0start-kafka.bat"
    echo       Kafka starting at localhost:9092 ...
)
echo.

REM ===== 3. Start XXL-JOB Admin (scheduler center) =====
echo [4/6] Starting XXL-JOB Admin ...
netstat -ano | find ":9091" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       XXL-JOB Admin already running, skip.
) else (
    start "XXL-JOB" "%~dp0start-xxljob.bat"
    echo       XXL-JOB Admin starting at localhost:9091 ...
)
echo.

REM ===== 4. Start Nacos (registry/config center) =====
echo [5/6] Starting Nacos ...
netstat -ano | find ":8848" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       Nacos already running, skip.
) else (
    start "Nacos" "%~dp0start-nacos.bat"
    echo       Nacos starting at localhost:8848 ...
)
echo.

REM ===== 5. Start Elasticsearch =====
echo [6/6] Starting Elasticsearch ...
netstat -ano | find ":9200" | find "LISTENING" >nul
if %errorlevel%==0 (
    echo       ES already running, skip.
) else (
    start "Elasticsearch" "C:\elasticsearch-7.17.27\bin\elasticsearch.bat"
    echo       ES starting at localhost:9200 ...
)
echo.

echo ============================================
echo   Done! Services:
echo   - Redis        : 127.0.0.1:6379
echo   - MongoDB      : localhost:27017
echo   - Kafka        : localhost:9092
echo   - XXL-JOB Admin: http://localhost:9091/xxl-job-admin  (admin/123456)
echo   - Nacos        : http://localhost:8848/nacos  (nacos/nacos)
echo   - Elasticsearch: http://localhost:9200
echo   - MySQL        : windows service (auto)
echo.
echo   Now you can start Spring Boot app:
echo   - learning-mct-api      (port 9090)
echo   - learning-merchant-api (port 9095)
echo ============================================
echo.
