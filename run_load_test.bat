@echo off
REM Script de test de charge pour le système de supervision
REM Lance 50 instances de l'agent en arrière-plan.

SET AGENT_JAR="agent-client\target\agent-client-1.0-SNAPSHOT-jar-with-dependencies.jar"

IF NOT EXIST %AGENT_JAR% (
    echo Fichier JAR de l'agent non trouvé.
    echo Veuillez compiler le projet avec 'mvn clean package'.
    goto :eof
)

echo Lancement de 50 agents en arrière-plan...

for /L %%i in (1,1,50) do (
    REM Formatte l'ID du noeud avec un zéro pour les nombres < 10 (ex: agent-01)
    set "NODE_ID=load-test-%%i"
    if %%i LSS 10 (
        set "NODE_ID=load-test-0%%i"
    )

    echo Lancement de l'agent !NODE_ID!

    REM Lance l'agent en arrière-plan sans ouvrir de nouvelle fenêtre de console
    START /B java -jar %AGENT_JAR% !NODE_ID!
)

echo 50 agents ont été lancés. Surveillez la console du serveur pour voir leur activité.
echo Pour arrêter les agents, vous devrez utiliser la commande 'taskkill /IM java.exe /F' (attention, cela peut fermer d'autres applications Java).
