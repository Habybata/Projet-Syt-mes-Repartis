package com.univ.sriv.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Tâche périodique pour vérifier le statut des nœuds connectés.
 */
public class NodeStatusMonitor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(NodeStatusMonitor.class);
    private static final long OFFLINE_THRESHOLD_MS = TimeUnit.SECONDS.toMillis(90);

    private final ConcurrentHashMap<String, NodeStatus> activeNodes;

    public NodeStatusMonitor(ConcurrentHashMap<String, NodeStatus> activeNodes) {
        this.activeNodes = activeNodes;
    }

    @Override
    public void run() {
        activeNodes.forEach((nodeId, status) -> {
            if (status.isOnline()) {
                long timeSinceLastContact = System.currentTimeMillis() - status.getLastContactTimestamp();
                if (timeSinceLastContact > OFFLINE_THRESHOLD_MS) {
                    status.setOnline(false);
                    logger.warn("ALERTE : Le nœud '{}' est maintenant considéré HORS LIGNE.", nodeId);
                }
            }
        });
    }
}
