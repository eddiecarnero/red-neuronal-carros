@echo off
title IA Conduccion Autonoma (Red Neuronal y AG)
:: Run the powershell runner script with bypass execution policy
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run.ps1"
if %errorlevel% neq 0 (
    echo.
    echo Ocurrio un error al ejecutar la aplicacion.
    pause
)
