@echo off
echo ========================================
echo Compilando Versão Serviço (Headless)
echo ========================================

REM Configurar Java 17 local
set JAVA_HOME=%~dp0..\java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo Usando Java: %JAVA_HOME%
echo Versao:
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo Compilando versão headless (serviço)...
.\mvnw.cmd clean package

if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha na compilacao!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================
echo Compilacao concluida com sucesso!
echo ========================================
echo.
echo JAR gerado: target\IntegradorHub-1.0.jar
echo Main-Class: br.com.erbs.integradorhub.IntegradorApplication
echo.
echo Para testar manualmente:
echo   scripts\start-service-manual.bat
echo.
echo Para instalar como serviço:
echo   scripts\install-service.bat

pause
