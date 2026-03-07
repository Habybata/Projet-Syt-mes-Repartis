package com.univ.sriv.server;

import com.google.gson.Gson;
import com.univ.sriv.model.MetricData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Traite la connexion d'un client dans un thread dédié du pool.
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final DatabaseManager dbManager;
    private final ConcurrentHashMap<String, NodeStatus> activeNodes;
    private final Gson gson = new Gson();

    public ClientHandler(Socket socket, DatabaseManager dbManager, ConcurrentHashMap<String, NodeStatus> activeNodes) {
        this.clientSocket = socket;
        this.dbManager = dbManager;
        this.activeNodes = activeNodes;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String line = in.readLine();
            if (line != null) {
                MetricData metric = gson.fromJson(line, MetricData.class);
                logger.debug("Métrique reçue de {}: {}", metric.getNodeId(), line);

                // Met à jour le statut du nœud ou en crée un nouveau
                activeNodes.compute(metric.getNodeId(), (k, v) -> {
                    if (v == null) {
                        logger.info("Nouveau nœud '{}' enregistré.", k);
                        return new NodeStatus(k);
                    }
                    v.updateLastContact();
                    return v;
                });

                // Insère la métrique dans la base de données
                dbManager.insertMetric(metric);
            }
        } catch (IOException e) {
            logger.warn("Connexion perdue avec {}: {}", clientSocket.getInetAddress(), e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Erreur lors de la fermeture du socket client.", e);
            }
        }
    }
}
