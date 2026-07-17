#!/bin/bash

# Script de test de charge pour le système de supervision
# Lance 50 instances de l'agent en arrière-plan.

# Assurez-vous que le projet a été compilé avec `mvn clean package`
AGENT_JAR="agent-client/target/agent-client-1.0-SNAPSHOT-jar-with-dependencies.jar"

if [ ! -f "$AGENT_JAR" ]; then
    echo "Fichier JAR de l'agent non trouvé. Veuillez compiler le projet avec 'mvn clean package'."
    exit 1
fi

NUM_AGENTS="${1:-100}"
PAUSE_SEC="${2:-1}"

echo "Lancement de $NUM_AGENTS agents en arrière-plan..."

for i in $(seq 1 "$NUM_AGENTS")
do
   if [ "$i" -lt 10 ]; then
       NODE_ID=$(printf "agent-%02d" "$i")
   else
       NODE_ID=$(printf "agent-%d" "$i")
   fi

   echo "[DÉMARRAGE] $NODE_ID en cours..."

   # Lance l'agent en arrière-plan, redirigeant sa sortie pour ne pas encombrer le terminal
   java -jar "$AGENT_JAR" "$NODE_ID" > /dev/null 2>&1 &

   if [ "$PAUSE_SEC" -gt 0 ]; then
       sleep "$PAUSE_SEC"
   fi
done

echo "$NUM_AGENTS agents ont été lancés. Surveillez la console du serveur pour voir leur activité."
echo "Pour arrêter les agents, vous devrez utiliser 'pkill -f agent-client' ou les tuer manuellement."
