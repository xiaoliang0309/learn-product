@echo off
REM MongoDB launcher (called by start-env.bat)
REM mongod home and data/log dirs
set "MONGOD_HOME=C:\mongodb\mongodb-win32-x86_64-windows-6.0.14"
set "DBPATH=C:\mongodb\data"
set "LOGDIR=C:\mongodb\log"

if not exist "%LOGDIR%" mkdir "%LOGDIR%"

"%MONGOD_HOME%\bin\mongod.exe" --dbpath "%DBPATH%" --port 27017 --logpath "%LOGDIR%\mongod.log" --logappend
