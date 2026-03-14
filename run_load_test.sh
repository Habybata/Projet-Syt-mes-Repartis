#!/bin/bash

# Script de test de charge pour le système de supervision
# Lance 50 instances de l'agent en arrière-plan.

# Assurez-vous que le projet a été compilé avec `mvn clean package`
AGENT_JAR="agent-client/target/agent-client-1.0-SNAPSHOT-jar-with-dependencies.jar"

if [ ! -f "$AGENT_JAR" ]; then
    echo "Fichier JAR de l'agent non trouvé. Veuillez compiler le projet avec 'mvn clean package'."
    exit 1
fi

echo "Lancement de 50 agents en arrière-plan..."

for i in {1..50}
do
   # Formatte l'ID du noeud avec un zéro pour les nombres < 10 (ex: agent-01)
   NODE_ID=$(printf "agent-%02d" $i)

   # Lance l'agent en arrière-plan, redirigeant sa sortie pour ne pas encombrer le terminal
   java -jar $AGENT_JAR $NODE_ID > /dev/null 2>&1 &

   echo "Agent $NODE_ID lancé."
done

echo "50 agents ont été lancés. Surveillez la console du serveur pour voir leur activité."
echo "Pour arrêter les agents, vous devrez utiliser 'pkill -f agent-client' ou les tuer manuellement."
