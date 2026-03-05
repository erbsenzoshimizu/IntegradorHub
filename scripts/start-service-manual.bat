@echo off
echo ========================================
echo Teste Manual do Serviço Headless
echo ========================================

REM Configurar Java 17 local
set APP_DIR=%~dp0..
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
echo Configuracoes:
echo - Java: %JAVA_LOCAL%
echo - JAR: %JAR_FILE%
echo.
echo Iniciando serviço em modo manual (headless)...
echo Pressione Ctrl+C para parar
echo.

REM Configurar variáveis de ambiente
set PATH=%APP_DIR%\java\jdk-17\bin;%PATH%

REM Iniciar a aplicação headless
"%JAVA_LOCAL%" -jar "%JAR_FILE%"

echo.
echo Serviço finalizado.
pause
