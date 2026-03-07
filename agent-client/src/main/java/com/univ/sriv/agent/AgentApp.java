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
        data.setCpuLoad(metricsCollector.collectCpuLoad());
        data.setMemoryLoad(metricsCollector.collectMemoryLoad());
        data.setDiskUsage(metricsCollector.collectDiskUsage());
        data.setUptime(metricsCollector.collectUptime());

        String jsonMetrics = gson.toJson(data);

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(jsonMetrics);
            logger.info("Métriques envoyées avec succès pour le nœud '{}'.", nodeId);

        } catch (ConnectException e) {
            logger.warn("Serveur injoignable. Réessai dans {} secondes.", COLLECTION_INTERVAL_SECONDS);
        } catch (IOException e) {
            logger.error("Erreur d'E/S lors de l'envoi des métriques : {}", e.getMessage());
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
