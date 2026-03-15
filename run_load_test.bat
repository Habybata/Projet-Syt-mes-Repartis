@echo off
chcp 65001 > nul
REM Script de test de charge pour le système de supervision
REM Lance plusieurs instances de l'agent en arrière-plan.

SETLOCAL EnableDelayedExpansion

REM Assure que le script s'exécute depuis son dossier (pour que les chemins relatifs fonctionnent)
pushd "%~dp0"

set "AGENT_JAR=agent-client\target\agent-client-1.0-SNAPSHOT-jar-with-dependencies.jar"

IF NOT EXIST %AGENT_JAR% (
    echo Fichier JAR de l'agent non trouvé.
    echo Veuillez compiler le projet avec 'mvn clean package'.
    popd
    goto :eof
)

<<<<<<< HEAD
echo Lancement de 100 agents en arrière-plan...

for /L %%i in (1,1,100) do (
    REM Formatage de l'ID avec un zéro pour les nombres < 10 (ex: agent-01)
    set "NODE_ID=agent-%%i"
    if %%i LSS 100 (
=======
REM Paramètres configurables (argument 1 = nombre d'agents, 2 = Xmx, 3 = pause entre démarrages)
set "NUM_AGENTS=%~1"
set "MAX_HEAP=%~2"
set "PAUSE_SEC=%~3"

if "%NUM_AGENTS%"=="" set "NUM_AGENTS=100"
if "%MAX_HEAP%"=="" set "MAX_HEAP=128m"
if "%PAUSE_SEC%"=="" set "PAUSE_SEC=1"

REM Options Java par défaut pour limiter l'empreinte mémoire par JVM.
set "JAVA_OPTS=-Xms64m -Xmx%MAX_HEAP% -XX:MaxRAMPercentage=30 -XX:+UseG1GC"

echo Lancement de %NUM_AGENTS% agents en arrière-plan (max heap = %MAX_HEAP%, pause = %PAUSE_SEC%s)...

echo NOTE: 100 JVMs utilisent beaucoup de mémoire. Si vous dépassez la mémoire, réduisez le nombre d'agents ou le paramètre Xmx.

echo Pour démarrer avec : run_load_test.bat 100 64m 2

for /L %%i in (1,1,%NUM_AGENTS%) do (
    REM Formatte l'ID du noeud avec un zéro pour les nombres < 10 (ex: agent-01)
    set "NODE_ID=agent-%%i"
    if %%i LSS 100 (
        set "NODE_ID=agent-0%%i"
    )
    if %%i LSS 100 (
>>>>>>> ce568f9306e3430b9b07366921ce475f9d5b6047
        set "NODE_ID=agent-0%%i"
    )

    echo [DÉMARRAGE] !NODE_ID! en cours...

    REM Lance l'agent en arrière-plan sans ouvrir de nouvelle fenêtre de console
    START /B java %JAVA_OPTS% -jar %AGENT_JAR% !NODE_ID!
)

echo 100 agents ont été lancés. Surveillez la console du serveur pour voir leur activité.
echo Pour arrêter les agents, vous devrez utiliser la commande 'taskkill /IM java.exe /F' (attention, cela peut fermer d'autres applications Java).
