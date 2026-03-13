package com.univ.sriv.agent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.univ.sriv.model.MetricData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Application principale de l'agent de supervision.
 * Cet agent collecte des métriques système et les envoie périodiquement à un serveur.
 */
public class AgentApp {
    private static final Logger logger = LoggerFactory.getLogger(AgentApp.class);
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1234;
    private static final int COLLECTION_INTERVAL_SECONDS = 30; // Intervalle de collecte et d'envoi

    private final String nodeId;
    private final MetricsCollector metricsCollector;
    private final Gson gson;

    public AgentApp(String nodeId) {
        this.nodeId = nodeId;
        this.metricsCollector = new MetricsCollector();
        this.gson = new GsonBuilder().create();
    }

    /**
     * Lance le processus de collecte et d'envoi des métriques.
     */
    public void start() {
        logger.info("Agent '{}' démarré. Tentative de connexion au serveur {}:{}", nodeId, SERVER_HOST, SERVER_PORT);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::sendMetrics, 0, COLLECTION_INTERVAL_SECONDS, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Arrêt de l'agent '{}'.", nodeId);
            scheduler.shutdownNow();
        }));
    }

    /**
     * Collecte les métriques et les envoie au serveur.
     */
    private void sendMetrics() {
        MetricData data = new MetricData();
        data.setNodeId(nodeId);
        data.setTimestamp(Instant.now().toEpochMilli());
        data.setOs(metricsCollector.getOsName());
        data.setCpuType(metricsCollector.getCpuType());
        
        double cpu = metricsCollector.collectCpuLoad();
        double mem = metricsCollector.collectMemoryLoad();
        double disk = metricsCollector.collectDiskUsage();
        
        data.setCpuLoad(cpu);
        data.setMemoryLoad(mem);
        data.setDiskUsage(disk);
        data.setUptime(metricsCollector.collectUptime());
        
        // Services et Ports
        data.setServices(metricsCollector.collectServicesStatus());
        data.setPorts(metricsCollector.collectPortsStatus());

        // Alerte si charge > 90%
        if (cpu > 90.0) logger.warn("ALERTE : Charge CPU critique sur {} : {}%", nodeId, String.format("%.2f", cpu));
        if (mem > 90.0) logger.warn("ALERTE : Charge Mémoire critique sur {} : {}%", nodeId, String.format("%.2f", mem));
        if (disk > 90.0) logger.warn("ALERTE : Utilisation Disque critique sur {} : {}%", nodeId, String.format("%.2f", disk));

        String jsonMetrics = gson.toJson(data);

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()))) {

            // Envoi des métriques
            out.println(jsonMetrics);
            logger.info("Métriques envoyées pour le nœud '{}'.", nodeId);

            // Lecture de la réponse du serveur (pour d'éventuelles commandes comme UP service)
            String response = in.readLine();
            if (response != null && !response.isEmpty()) {
                logger.info("Commande reçue du serveur : {}", response);
                handleServerCommand(response);
            }

        } catch (ConnectException e) {
            logger.warn("Serveur injoignable ({}:{}). Réessai dans {}s.", SERVER_HOST, SERVER_PORT, COLLECTION_INTERVAL_SECONDS);
        } catch (IOException e) {
            logger.error("Erreur d'E/S lors de la communication avec le serveur : {}", e.getMessage());
        }
    }

    /**
     * Gère les commandes reçues du serveur (ex: "UP:HTTP").
     */
    private void handleServerCommand(String command) {
        if (command.startsWith("UP:")) {
            String service = command.substring(3);
            logger.info("ACTION : Activation du service '{}' demandée par le serveur.", service);
            // Ici, on simulerait l'activation du service
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar agent-client.jar <nodeId>");
            System.exit(1);
        }
        String nodeId = args[0];
        AgentApp agent = new AgentApp(nodeId);
        agent.start();
    }
}
