package com.univ.sriv.agent;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Cette classe est responsable de la collecte des métriques système.
 * Pour ce projet, les métriques sont principalement simulées pour des raisons de simplicité
 * et de portabilité, conformément aux recommandations du projet.
 */
public class MetricsCollector {
    private final Random random = new Random();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();

    // Listes de services et ports à surveiller
    private final String[] networkServices = {"HTTP", "SSH", "DNS"};
    private final String[] publicApps = {"Chrome", "VLC", "Discord"};
    private final int[] monitoredPorts = {80, 443, 22, 3306};

    /**
     * Simule la collecte de la charge CPU.
     * @return Charge CPU en pourcentage (double entre 5.0 et 80.0).
     */
    public double collectCpuLoad() {
        // Simule une charge CPU réaliste entre 5% et 95% (pour tester les alertes > 90%)
        return 5.0 + (95.0 - 5.0) * random.nextDouble();
    }

    /**
     * Simule la collecte de la charge mémoire.
     * @return Charge mémoire en pourcentage (double entre 10.0 et 90.0).
     */
    public double collectMemoryLoad() {
        return 10.0 + (90.0 - 10.0) * random.nextDouble();
    }

    /**
     * Simule la collecte de l'utilisation du disque.
     * @return Utilisation du disque en pourcentage (double entre 20.0 et 95.0).
     */
    public double collectDiskUsage() {
        return 20.0 + (95.0 - 20.0) * random.nextDouble();
    }

    /**
     * Collecte le statut de 6 services (3 réseau + 3 applications).
     * @return Map des services et leur statut (OK/KO).
     */
    public Map<String, String> collectServicesStatus() {
        Map<String, String> statusMap = new HashMap<>();
        // Simulation aléatoire : 80% de chance que le service soit OK
        for (String s : networkServices) statusMap.put(s, random.nextDouble() > 0.2 ? "OK" : "KO");
        for (String s : publicApps) statusMap.put(s, random.nextDouble() > 0.2 ? "OK" : "KO");
        return statusMap;
    }

    /**
     * Collecte le statut de 4 ports prédéfinis.
     * @return Map des ports et leur statut (1 pour ouvert, 0 pour fermé).
     */
    public Map<Integer, Integer> collectPortsStatus() {
        Map<Integer, Integer> portMap = new HashMap<>();
        // Simulation aléatoire : 50% de chance que le port soit ouvert
        for (int p : monitoredPorts) portMap.put(p, random.nextBoolean() ? 1 : 0);
        return portMap;
    }

    /**
     * Récupère le temps de fonctionnement du système (JVM) en secondes.
     * Une approximation de l'uptime du système réel pour cet exemple.
     * @return Temps de fonctionnement en secondes.
     */
    public long collectUptime() {
        return (System.currentTimeMillis() - jvmStartTime) / 1000;
    }

    /**
     * Récupère le nom du système d'exploitation.
     * @return Nom du système d'exploitation.
     */
    public String getOsName() {
        return osBean.getName();
    }

    /**
     * Récupère le type de CPU (simple simulation).
     * @return Type de CPU.
     */
    public String getCpuType() {
        return osBean.getArch(); // Retourne l'architecture du CPU, ex: "amd64"
    }
}
