package com.univ.sriv.model;

/**
 * Représente un objet de données de métrique collecté par un agent.
 * Cette classe est partagée entre le client (agent) et le serveur.
 */
public class MetricData {
    private String nodeId;
    private long timestamp;
    private String os;
    private String cpuType; // Type de CPU, ex: "Intel Core i7"
    private double cpuLoad; // Charge CPU en pourcentage, ex: 35.5
    private double memoryLoad; // Charge mémoire en pourcentage, ex: 62.0
    private double diskUsage; // Utilisation du disque en pourcentage, ex: 45.2
    private long uptime; // Temps de fonctionnement du système en secondes

    /**
     * Constructeur par défaut nécessaire pour la désérialisation JSON.
     */
    public MetricData() {
    }

    // --- Getters et Setters ---

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getCpuType() {
        return cpuType;
    }

    public void setCpuType(String cpuType) {
        this.cpuType = cpuType;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public double getMemoryLoad() {
        return memoryLoad;
    }

    public void setMemoryLoad(double memoryLoad) {
        this.memoryLoad = memoryLoad;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    @Override
    public String toString() {
        return "MetricData{" +
               "nodeId='" + nodeId + '\'' +
               ", timestamp=" + timestamp +
               ", os='" + os + '\'' +
               ", cpuType='" + cpuType + '\'' +
               ", cpuLoad=" + String.format("%.2f", cpuLoad) + "%" +
               ", memoryLoad=" + String.format("%.2f", memoryLoad) + "%" +
               ", diskUsage=" + String.format("%.2f", diskUsage) + "%" +
               ", uptime=" + uptime + "s" +
               '}';
    }
}
