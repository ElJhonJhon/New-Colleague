@echo off
title Lanzar Minecraft - New Colleague Mod
cd /d "%~dp0"
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
echo ===================================================
echo   Iniciando Minecraft con New Colleague Mod...
echo ===================================================
call gradlew.bat runClient
if %errorlevel% neq 0 (
    echo.
    echo Ocurrio un error al iniciar. Revisa los mensajes arriba.
    pause
)
