@echo off
echo ========================================
echo Instalando IntegradorHub como Serviço Windows
echo ========================================

set APP_DIR=%~dp0..
set SERVICE_NAME=IntegradorHub
set SERVICE_DISPLAY=IntegradorHub Service
set SERVICE_DESCRIPTION="Servico de integracao de documentos NFC-e"
set JAVA_LOCAL=%APP_DIR%\java\jdk-17\bin\java.exe
set JAR_FILE=%APP_DIR%\target\IntegradorHub-1.0.jar

echo Verificando arquivos...
if not exist "%JAVA_LOCAL%" (
    echo ERRO: Java 17 nao encontrado em %JAVA_LOCAL%
    echo Execute: scripts\install-java17.bat
    pause
    exit /b 1
)

if not exist "%JAR_FILE%" (
    echo ERRO: JAR nao encontrado em %JAR_FILE%
    echo Execute: scripts\build-service.bat
    pause
    exit /b 1
)

echo.
echo Configuracoes do serviço:
echo - Nome: %SERVICE_NAME%
echo - Display: %SERVICE_DISPLAY%
echo - Java: %JAVA_LOCAL%
echo - JAR: %JAR_FILE%
echo.

REM Verificar se o serviço já existe
sc query "%SERVICE_NAME%" >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo Servico %SERVICE_NAME% ja existe.
    echo.
    set /p choice="Deseja reinstalar? (S/N): "
    if /i not "%choice%"=="S" (
        echo Operacao cancelada.
        pause
        exit /b 0
    )
    echo Parando e removendo servico existente...
    sc stop "%SERVICE_NAME%" >nul 2>&1
    timeout /t 3 /nobreak >nul
    sc delete "%SERVICE_NAME%" >nul 2>&1
    timeout /t 2 /nobreak >nul
)

echo Criando comando de instalacao...
set SERVICE_CMD=sc create "%SERVICE_NAME%" ^
    binPath= "\"%JAVA_LOCAL%\" -jar \"%JAR_FILE%\"" ^
    DisplayName= "%SERVICE_DISPLAY%" ^
    Description= "%SERVICE_DESCRIPTION%" ^
    start= auto

echo.
echo Executando: %SERVICE_CMD%
%SERVICE_CMD%

if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha ao criar servico!
    echo Verifique se esta executando como Administrador.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Servico instalado com sucesso!
echo ========================================
echo.
echo Para gerenciar o servico:
echo   Iniciar:   scripts\start-service.bat
echo   Parar:     scripts\stop-service.bat
echo   Status:    scripts\status-service.bat
echo   Remover:   scripts\remove-service.bat
echo.
echo Ou use o services.msc do Windows
echo.

set /p choice="Deseja iniciar o servico agora? (S/N): "
if /i "%choice%"=="S" (
    echo Iniciando servico...
    call "%~dp0start-service.bat"
) else (
    echo Servico instalado mas nao iniciado.
    echo Use 'scripts\start-service.bat' para iniciar.
)

pause
