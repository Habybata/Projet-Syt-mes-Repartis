package com.univ.sriv.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.univ.sriv.model.MetricData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gère la base de données, y compris le pool de connexions et les requêtes.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String JDBC_URL = "jdbc:sqlite:supervision.db";
    private HikariDataSource dataSource;
    private final Gson gson = new Gson();

    public void initialize() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(JDBC_URL);
            
            // Optimisation pour SQLite : mode WAL et gestion des verrous
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("synchronous", "NORMAL");
            config.addDataSourceProperty("foreign_keys", "true");
            
            // Limitation du pool pour éviter les conflits SQLite
            config.setMaximumPoolSize(10); // Augmenté avec WAL, mais géré par Hikari
            config.setConnectionTimeout(30000); // 30 secondes de timeout
            
            dataSource = new HikariDataSource(config);
            createMetricsTable();
            logger.info("Base de données initialisée avec succès (Mode WAL activé).");
        } catch (Exception e) {
            logger.error("Échec de l'initialisation de la base de données.", e);
            throw new RuntimeException(e);
        }
    }

    private void createMetricsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS metrics (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, nodeId TEXT NOT NULL, timestamp INTEGER NOT NULL," +
                     "os TEXT, cpuType TEXT, cpuLoad REAL, memoryLoad REAL, diskUsage REAL, uptime INTEGER," +
                     "services TEXT, ports TEXT);";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            // Migration simple si les colonnes manquent
            try {
                stmt.execute("ALTER TABLE metrics ADD COLUMN services TEXT;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE metrics ADD COLUMN ports TEXT;");
            } catch (SQLException ignored) {}
        }
    }

    public void insertMetric(MetricData metric) {
        String sql = "INSERT INTO metrics(nodeId, timestamp, os, cpuType, cpuLoad, memoryLoad, diskUsage, uptime, services, ports) VALUES(?,?,?,?,?,?,?,?,?,?);";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, metric.getNodeId());
            pstmt.setLong(2, metric.getTimestamp());
            pstmt.setString(3, metric.getOs());
            pstmt.setString(4, metric.getCpuType());
            pstmt.setDouble(5, metric.getCpuLoad());
            pstmt.setDouble(6, metric.getMemoryLoad());
            pstmt.setDouble(7, metric.getDiskUsage());
            pstmt.setLong(8, metric.getUptime());
            pstmt.setString(9, gson.toJson(metric.getServices()));
            pstmt.setString(10, gson.toJson(metric.getPorts()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion des métriques pour {} : {}", metric.getNodeId(), e.getMessage());
        }
    }

    public List<MetricData> getLatestMetrics(String nodeId, int limit) {
        List<MetricData> metrics = new ArrayList<>();
        String sql = "SELECT * FROM metrics WHERE nodeId = ? ORDER BY timestamp DESC LIMIT ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nodeId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MetricData metric = new MetricData();
                    metric.setNodeId(rs.getString("nodeId"));
                    metric.setTimestamp(rs.getLong("timestamp"));
                    metric.setOs(rs.getString("os"));
                    metric.setCpuType(rs.getString("cpuType"));
                    metric.setCpuLoad(rs.getDouble("cpuLoad"));
                    metric.setMemoryLoad(rs.getDouble("memoryLoad"));
                    metric.setDiskUsage(rs.getDouble("diskUsage"));
                    metric.setUptime(rs.getLong("uptime"));
                    
                    metric.setServices(gson.fromJson(rs.getString("services"), 
                        new TypeToken<Map<String, String>>(){}.getType()));
                    metric.setPorts(gson.fromJson(rs.getString("ports"), 
                        new TypeToken<Map<Integer, Integer>>(){}.getType()));
                        
                    metrics.add(metric);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des métriques pour {} : {}", nodeId, e.getMessage());
        }
        return metrics;
    }

    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Pool de connexions fermé.");
        }
    }
}
