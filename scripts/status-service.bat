@echo off
echo ========================================
echo Status do Serviço IntegradorHub
echo ========================================

set SERVICE_NAME=IntegradorHub

echo Verificando se o servico existe...
sc query "%SERVICE_NAME%" >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERRO: Servico %SERVICE_NAME% nao encontrado!
    echo Execute: scripts\install-service.bat
    pause
    exit /b 1
)

echo.
echo Informacoes do servico:
sc query "%SERVICE_NAME%"

echo.
echo Log recente (ultimas 10 linhas):
if exist "logs\integradorhub-%date:~-4,4%-%date:~-7,2%-%date:~-10,2%.log" (
    powershell -Command "Get-Content 'logs\integradorhub-%date:~-4,4%-%date:~-7,2%-%date:~-10,2%.log' | Select-Object -Last 10"
) else (
    echo Nenhum arquivo de log encontrado para hoje.
    echo Arquivos de log disponiveis:
    dir logs\*.log /B 2>nul
    if %ERRORLEVEL% neq 0 (
        echo Nenhum arquivo de log encontrado.
    )
)

echo.
pause
