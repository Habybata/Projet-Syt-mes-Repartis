package com.univ.sriv.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.univ.sriv.model.MetricData;

/**
 * Traite la connexion d'un client dans un thread dédié du pool.
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final DatabaseManager dbManager;
    private final ConcurrentHashMap<String, NodeStatus> activeNodes;
    private final ConcurrentHashMap<String, String> pendingCommands;
    private final Gson gson = new Gson();

    public ClientHandler(Socket socket, DatabaseManager dbManager, ConcurrentHashMap<String, NodeStatus> activeNodes, ConcurrentHashMap<String, String> pendingCommands) {
        this.clientSocket = socket;
        this.dbManager = dbManager;
        this.activeNodes = activeNodes;
        this.pendingCommands = pendingCommands;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
             
            String line = in.readLine();
            if (line != null && !line.isEmpty()) {
                MetricData metric = gson.fromJson(line, MetricData.class);
                
                // Validation simple du format
                if (metric == null || metric.getNodeId() == null) {
                    logger.warn("Format de message invalide reçu de {}", clientSocket.getInetAddress());
                    return;
                }

                logger.debug("Métrique reçue de {}: CPU={}%", metric.getNodeId(), metric.getCpuLoad());

                // Met à jour le statut du nœud
                activeNodes.compute(metric.getNodeId(), (k, v) -> {
                    if (v == null) {
                        logger.info("Nouveau nœud '{}' enregistré.", k);
                        return new NodeStatus(k);
                    }
                    v.updateLastContact();
                    if (!v.isOnline()) {
                        v.setOnline(true);
                        logger.info("Nœud '{}' de retour en ligne.", k);
                    }
                    return v;
                });

                // Insère en base
                dbManager.insertMetric(metric);

                // Affichage direct des métriques dans la console du serveur
                System.out.printf("[METRICS] %s | CPU=%.2f%% | MEM=%.2f%% | DISK=%.2f%% | UPTIME=%d s%n",
                        metric.getNodeId(),
                        metric.getCpuLoad(),
                        metric.getMemoryLoad(),
                        metric.getDiskUsage(),
                        metric.getUptime());

                // Journalisation des alertes de dépassement de seuil (> 90%) côté serveur
                if (metric.getCpuLoad() > 90.0) {
                    logger.warn("ALERTE SEUIL - Nœud '{}' : Charge CPU critique ({}%)", metric.getNodeId(), String.format("%.2f", metric.getCpuLoad()));
                }
                if (metric.getMemoryLoad() > 90.0) {
                    logger.warn("ALERTE SEUIL - Nœud '{}' : Charge Mémoire critique ({}%)", metric.getNodeId(), String.format("%.2f", metric.getMemoryLoad()));
                }
                if (metric.getDiskUsage() > 90.0) {
                    logger.warn("ALERTE SEUIL - Nœud '{}' : Utilisation Disque critique ({}%)", metric.getNodeId(), String.format("%.2f", metric.getDiskUsage()));
                }

                // Envoi d'une commande en attente (ex: UP service) si présente
                String command = pendingCommands.remove(metric.getNodeId());
                if (command != null) {
                    out.println(command);
                    logger.info("Commande '{}' envoyée au nœud '{}'.", command, metric.getNodeId());
                }
            }
        } catch (IOException e) {
            logger.warn("Erreur de communication avec {}: {}", clientSocket.getInetAddress(), e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Erreur lors de la fermeture du socket client.", e);
            }
        }
    }
}
