@echo off
chcp 65001 > nul
REM Script de test de charge pour le système de supervision
REM Lance 50 instances de l'agent en arrière-plan.

SETLOCAL EnableDelayedExpansion

SET AGENT_JAR="agent-client\target\agent-client-1.0-SNAPSHOT-jar-with-dependencies.jar"

IF NOT EXIST %AGENT_JAR% (
    echo [ERREUR] Fichier JAR de l'agent non trouvé.
    echo Veuillez compiler le projet avec 'mvn clean package'.
    goto :eof
)

echo Lancement de 50 agents en arrière-plan...

for /L %%i in (1,1,50) do (
    REM Formatage de l'ID avec un zéro pour les nombres < 10 (ex: agent-01)
    set "NODE_ID=agent-%%i"
    if %%i LSS 10 (
        set "NODE_ID=agent-0%%i"
    )

    echo [DÉMARRAGE] !NODE_ID! en cours...

    REM Lance l'agent en arrière-plan sans ouvrir de nouvelle fenêtre de console
    START /B java -jar %AGENT_JAR% !NODE_ID! > nul 2>&1
)

echo.
echo ✅ 50 agents ont été lancés. Surveillez la console du serveur pour voir leur activité.
echo ℹ️  Pour arrêter les agents : 'taskkill /IM java.exe /F'
