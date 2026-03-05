@echo off
echo ========================================
echo Download e Instalação Java 17 (Local)
echo ========================================

REM Configurações
set JAVA_VERSION=17
set DOWNLOAD_DIR=C:\Temp\JavaDownloads
set APP_DIR=%~dp0..
set JAVA_LOCAL_DIR=%APP_DIR%\java\jdk-%JAVA_VERSION%
set JDK_URL=https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.zip

echo Instalando Java %JAVA_VERSION% localmente na aplicacao...
echo Destino: %JAVA_LOCAL_DIR%
echo.

echo Criando diretorio de downloads...
if not exist "%DOWNLOAD_DIR%" mkdir "%DOWNLOAD_DIR%"

echo.
echo Baixando Java %JAVA_VERSION%...
echo URL: %JDK_URL%
echo Destino: %DOWNLOAD_DIR%\jdk-%JAVA_VERSION%_windows-x64_bin.zip
echo.

REM Usar PowerShell para download (mais confiável)
powershell -Command "& {Invoke-WebRequest -Uri '%JDK_URL%' -OutFile '%DOWNLOAD_DIR%\jdk-%JAVA_VERSION%_windows-x64_bin.zip' -UseBasicParsing}"

if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha no download!
    echo Verifique sua conexao com a internet.
    pause
    exit /b 1
)

echo Download concluido!

echo.
echo Extraindo Java %JAVA_VERSION% para pasta local...
REM Criar pasta java na aplicacao
if not exist "%APP_DIR%\java" mkdir "%APP_DIR%\java"

REM Extrair usando PowerShell
powershell -Command "& {Expand-Archive -Path '%DOWNLOAD_DIR%\jdk-%JAVA_VERSION%_windows-x64_bin.zip' -DestinationPath '%APP_DIR%\java\' -Force}"

if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha na extracao!
    pause
    exit /b 1
)

echo.
echo Procurando pasta extraida...
REM Encontrar e renomear pasta para padrao jdk-17
for /d %%i in ("%APP_DIR%\java\jdk-%JAVA_VERSION%*") do (
    echo Encontrado: %%i
    if /i not "%%~nxi"=="jdk-%JAVA_VERSION%" (
        echo Renomeando para jdk-%JAVA_VERSION%
        ren "%%i" "jdk-%JAVA_VERSION%"
    )
    goto :found
)

:found
echo.
echo Verificando instalacao...
if exist "%JAVA_LOCAL_DIR%\bin\java.exe" (
    echo SUCESSO: Java %JAVA_VERSION% instalado em %JAVA_LOCAL_DIR%
    echo.
    echo Testando instalacao...
    "%JAVA_LOCAL_DIR%\bin\java.exe" -version
    echo.
    echo Instalacao concluida com sucesso!
    echo.
    echo Para usar o Java %JAVA_VERSION% com o IntegradorHub:
    echo   scripts\start-with-java.bat %JAVA_VERSION%
    echo.
    echo BENEFICIOS:
    echo - Java 17 embutido na aplicacao
    echo - Nenhuma instalacao global necessaria
    echo - Portabilidade total
    echo - Side-by-side com Java 8 (se existir)
) else (
    echo ERRO: Instalacao nao encontrada em %JAVA_LOCAL_DIR%\bin\java.exe
    pause
    exit /b 1
)

echo.
echo Limpando arquivos temporarios...
del "%DOWNLOAD_DIR%\jdk-%JAVA_VERSION%_windows-x64_bin.zip"

echo.
echo ========================================
echo Java %JAVA_VERSION% instalado localmente!
echo ========================================
echo Local: %JAVA_LOCAL_DIR%
echo Tamanho: ~160MB
echo Portavel: Sim
echo ========================================
pause
