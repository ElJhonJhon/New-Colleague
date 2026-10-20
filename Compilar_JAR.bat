@echo off
title Compilar Mod - New Colleague Mod
cd /d "%~dp0"
if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
echo ===================================================
echo   Compilando archivo JAR final...
echo ===================================================
call gradlew.bat build
if %errorlevel% equ 0 (
    echo.
    echo ===================================================
    echo   EXITO: El archivo .jar se ha generado en:
    echo   build\libs\
    echo ===================================================
    explorer.exe "build\libs"
) else (
    echo.
    echo Ocurrio un error en la compilacion.
)
pause
