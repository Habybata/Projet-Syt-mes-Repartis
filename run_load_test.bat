@echo off
SETLOCAL EnableDelayedExpansion

set "AGENT_JAR=agent-client\target\agent-client-1.0-SNAPSHOT-jar-with-dependencies.jar"

if not exist "%AGENT_JAR%" (
    echo JAR agent introuvable
    exit /b
)

set NUM_AGENTS=%1

if "%NUM_AGENTS%"=="" set NUM_AGENTS=5

echo Lancement de %NUM_AGENTS% agents

for /L %%i in (1,1,%NUM_AGENTS%) do (

    set NODE_ID=agent-0%%i

    echo Demarrage !NODE_ID!

    start /B java -jar "%AGENT_JAR%" !NODE_ID!

    timeout /t 1 >nul
)

echo Terminé
pause
