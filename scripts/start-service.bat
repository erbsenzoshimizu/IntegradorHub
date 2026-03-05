@echo off
echo ========================================
echo Iniciando Serviço IntegradorHub
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

echo Verificando status atual...
sc query "%SERVICE_NAME%" | find "STATE"

echo.
echo Iniciando servico...
sc start "%SERVICE_NAME%"

if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha ao iniciar servico!
    echo Verifique o log do Windows Event Viewer.
    pause
    exit /b 1
)

echo.
echo Aguardando inicio do servico...
timeout /t 5 /nobreak >nul

echo Verificando status...
sc query "%SERVICE_NAME%" | find "STATE"

echo.
echo Servico iniciado com sucesso!
echo.
echo Para verificar logs:
echo   - Arquivo: logs\integradorhub-YYYY-MM-DD.log
echo   - Event Viewer: Windows Logs -> Application
echo.

pause
