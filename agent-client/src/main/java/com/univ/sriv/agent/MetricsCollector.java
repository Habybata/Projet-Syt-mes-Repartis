package com.univ.sriv.agent;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
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

    /**
     * Simule la collecte de la charge CPU.
     * @return Charge CPU en pourcentage (double entre 5.0 et 80.0).
     */
    public double collectCpuLoad() {
        // Simule une charge CPU réaliste entre 5% et 80%
        return 5.0 + (80.0 - 5.0) * random.nextDouble();
    }

    /**
     * Simule la collecte de la charge mémoire.
     * @return Charge mémoire en pourcentage (double entre 10.0 et 90.0).
     */
    public double collectMemoryLoad() {
        // Simule une charge mémoire réaliste entre 10% et 90%
        return 10.0 + (90.0 - 10.0) * random.nextDouble();
    }

    /**
     * Simule la collecte de l'utilisation du disque.
     * @return Utilisation du disque en pourcentage (double entre 20.0 et 95.0).
     */
    public double collectDiskUsage() {
        // Simule une utilisation du disque réaliste entre 20% et 95%
        return 20.0 + (95.0 - 20.0) * random.nextDouble();
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
