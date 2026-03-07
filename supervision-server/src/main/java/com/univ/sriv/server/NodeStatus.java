package com.univ.sriv.server;

import com.univ.sriv.model.MetricData;

/**
 * Représente l'état d'un nœud (agent) suivi par le serveur.
 */
public class NodeStatus {
    private final String nodeId;
    private long lastContactTimestamp;
    private boolean isOnline;

    public NodeStatus(String nodeId) {
        this.nodeId = nodeId;
        this.updateLastContact(); // Marque comme en ligne au premier contact
    }

    public String getNodeId() {
        return nodeId;
    }

    public long getLastContactTimestamp() {
        return lastContactTimestamp;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void updateLastContact() {
        this.lastContactTimestamp = System.currentTimeMillis();
        this.isOnline = true;
    }
}
