@echo off
echo ========================================
echo Removendo Serviço IntegradorHub
echo ========================================

set SERVICE_NAME=IntegradorHub

echo Verificando se o servico existe...
sc query "%SERVICE_NAME%" >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo AVISO: Servico %SERVICE_NAME% nao encontrado!
    echo Nada a remover.
    pause
    exit /b 0
)

echo.
echo ATENCAO: Isso ira remover permanentemente o servico!
echo.
set /p choice="Tem certeza que deseja remover o servico? (S/N): "
if /i not "%choice%"=="S" (
    echo Operacao cancelada.
    pause
    exit /b 0
)

echo.
echo Verificando status atual...
sc query "%SERVICE_NAME%" | find "STATE"

echo.
echo Parando servico (se estiver rodando)...
sc stop "%SERVICE_NAME%" >nul 2>&1
timeout /t 3 /nobreak >nul

echo Removendo servico...
sc delete "%SERVICE_NAME%"

if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha ao remover servico!
    echo Verifique se esta executando como Administrador.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Servico removido com sucesso!
echo ========================================
echo.
echo A aplicacao JAR continua disponivel em:
echo   target\IntegradorHub-1.0.jar
echo.
echo Para executar manualmente:
echo   scripts\start-service-manual.bat

pause
