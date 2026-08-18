@echo off
REM Nacos launcher (called by start-env.bat), standalone mode
set JAVA_HOME=D:\javasdk
cd /d C:\nacos\nacos\bin
startup.cmd -m standalone
