@echo off
echo ========================================
echo Integrador Hub - Java Side-by-Side
echo ========================================

REM Configurações
set APP_NAME=IntegradorHub
set JAVA_VERSION=%1
if "%JAVA_VERSION%"=="" set JAVA_VERSION=17

REM Detectar diretório da aplicação
set APP_DIR=%~dp0..
set JAR_FILE=%APP_DIR%\target\%APP_NAME%-1.0.jar

echo Procurando Java %JAVA_VERSION%...

REM 1) Tenta Java local na aplicacao (prioridade)
set JAVA_LOCAL=%APP_DIR%\java\jdk-%JAVA_VERSION%\bin\java.exe
if exist "%JAVA_LOCAL%" (
    set JAVA_HOME=%APP_DIR%\java\jdk-%JAVA_VERSION%
    set JAVA_CMD=%JAVA_LOCAL%
    echo Java %JAVA_VERSION% encontrado localmente: %JAVA_HOME%
    goto :run
)

REM 2) Tenta JDK padrão do sistema
if exist "C:\Program Files\Java\jdk-%JAVA_VERSION%\bin\java.exe" (
    set JAVA_HOME=C:\Program Files\Java\jdk-%JAVA_VERSION%
    set JAVA_CMD=%JAVA_HOME%\bin\java.exe
    echo Java %JAVA_VERSION% encontrado no sistema: %JAVA_HOME%
    goto :run
)

REM 3) Tenta JDK em Program Files (x86)
if exist "C:\Program Files (x86)\Java\jdk-%JAVA_VERSION%\bin\java.exe" (
    set JAVA_HOME=C:\Program Files (x86)\Java\jdk-%JAVA_VERSION%
    set JAVA_CMD=%JAVA_HOME%\bin\java.exe
    echo Java %JAVA_VERSION% encontrado em x86: %JAVA_HOME%
    goto :run
)

REM 4) Tenta instalação customizada
if exist "C:\Java\jdk-%JAVA_VERSION%\bin\java.exe" (
    set JAVA_HOME=C:\Java\jdk-%JAVA_VERSION%
    set JAVA_CMD=%JAVA_HOME%\bin\java.exe
    echo Java %JAVA_VERSION% encontrado em custom: %JAVA_HOME%
    goto :run
)

echo ERRO: Java %JAVA_VERSION% nao encontrado!
echo.
echo Opcoes:
echo 1. Instale Java localmente: scripts\install-java17.bat
echo 2. Instale Java globalmente em C:\Program Files\Java\jdk-%JAVA_VERSION%
echo 3. Especifique caminho customizado
echo.
echo Exemplos:
echo   scripts\start-with-java.bat 17
echo   scripts\start-with-java.bat 8
pause
exit /b 1

:run
echo.
echo Configuracoes:
echo - Java: %JAVA_VERSION%
echo - JAVA_HOME: %JAVA_HOME%
echo - JAR: %JAR_FILE%
echo.

REM Verifica se o JAR existe
if not exist "%JAR_FILE%" (
    echo ERRO: Arquivo %JAR_FILE% nao encontrado!
    echo Execute 'mvn clean package' primeiro.
    echo.
    echo Dica: Navegue para o diretorio raiz do projeto:
    echo   cd %APP_DIR%
    echo   mvn clean package
    pause
    exit /b 1
)

echo Iniciando %APP_NAME% com Java %JAVA_VERSION%...
echo.

REM Configura variáveis de ambiente
set PATH=%JAVA_HOME%\bin;%PATH%

REM Inicia a aplicação
"%JAVA_CMD%" -jar "%JAR_FILE%"

if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha ao executar aplicacao (codigo %ERRORLEVEL%)
    echo Verifique os logs para detalhes.
    pause
    exit /b %ERRORLEVEL%
)

echo Aplicacao finalizada.
pause
