package com.univ.sriv.server;

import com.univ.sriv.model.MetricData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère la console d'administration pour interagir avec le serveur.
 */
public class AdminConsole implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AdminConsole.class);
    private final DatabaseManager dbManager;
    private final ConcurrentHashMap<String, NodeStatus> activeNodes;
    private final ConcurrentHashMap<String, String> pendingCommands;
    private final SupervisionServerApp serverApp;

    public AdminConsole(DatabaseManager dbManager, ConcurrentHashMap<String, NodeStatus> activeNodes, ConcurrentHashMap<String, String> pendingCommands, SupervisionServerApp serverApp) {
        this.dbManager = dbManager;
        this.activeNodes = activeNodes;
        this.pendingCommands = pendingCommands;
        this.serverApp = serverApp;
    }

    @Override
    public void run() {
        logger.info("Console d'administration prête. Tapez 'help' pour la liste des commandes.");
        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            while (!serverApp.isShuttingDown()) {
                System.out.print("> ");
                String command = consoleReader.readLine();
                if (command == null) break;
                processCommand(command.trim());
            }
        } catch (Exception e) {
            logger.error("Erreur dans la console d'administration.", e);
        }
    }

    private void processCommand(String command) {
        String[] parts = command.split(" ", 3);
        switch (parts[0].toLowerCase()) {
            case "list-nodes":
                listNodes();
                break;
            case "show-metrics":
                if (parts.length > 1) showMetrics(parts[1]);
                else System.out.println("Usage: show-metrics <nodeId>");
                break;
            case "activate-service":
                if (parts.length > 2) activateService(parts[1], parts[2]);
                else System.out.println("Usage: activate-service <nodeId> <serviceName>");
                break;
            case "help":
                displayHelp();
                break;
            case "exit":
                serverApp.shutdown();
                break;
            default:
                System.out.println("Commande inconnue.");
        }
    }

    private void listNodes() {
        System.out.println("--- Nœuds Actifs ---");
        if (activeNodes.isEmpty()) System.out.println("Aucun nœud actif.");
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            activeNodes.forEach((id, status) -> System.out.printf("- %s (Statut: %s, Dernier contact: %s)%n",
                id, status.isOnline() ? "En ligne" : "Hors ligne", sdf.format(new Date(status.getLastContactTimestamp()))));
        }
        System.out.println("--------------------");
    }

    private void showMetrics(String nodeId) {
        System.out.printf("--- 5 Dernières Métriques pour '%s' ---%n", nodeId);
        List<MetricData> metrics = dbManager.getLatestMetrics(nodeId, 5);
        if (metrics.isEmpty()) System.out.println("Aucune métrique trouvée.");
        else {
            metrics.forEach(m -> {
                System.out.println(m.toString());
                System.out.println("  Services : " + m.getServices());
                System.out.println("  Ports    : " + m.getPorts());
            });
        }
        System.out.println("---------------------------------------");
    }

    private void activateService(String nodeId, String serviceName) {
        pendingCommands.put(nodeId, "UP:" + serviceName);
        System.out.printf("Commande d'activation pour '%s' (nœud '%s') mise en attente.%n", serviceName, nodeId);
    }

    private void displayHelp() {
        System.out.println("Commandes: list-nodes, show-metrics <nodeId>, activate-service <nodeId> <service>, exit, help.");
    }
}
