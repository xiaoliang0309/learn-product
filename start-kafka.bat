@echo off
REM Kafka launcher (called by start-env.bat)
set JAVA_HOME=D:\javasdk
cd /d C:\kafka\kafka_2.13-3.7.0
bin\windows\kafka-server-start.bat config\kraft\server.properties
