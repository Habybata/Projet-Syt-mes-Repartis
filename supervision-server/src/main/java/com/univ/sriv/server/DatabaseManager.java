package com.univ.sriv.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univ.sriv.model.MetricData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Gère la base de données, y compris le pool de connexions et les requêtes.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String JDBC_URL = "jdbc:sqlite:supervision.db";
    private HikariDataSource dataSource;

    public void initialize() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(JDBC_URL);
            dataSource = new HikariDataSource(config);
            createMetricsTable();
            logger.info("Base de données initialisée avec succès.");
        } catch (Exception e) {
            logger.error("Échec de l'initialisation de la base de données.", e);
            throw new RuntimeException(e);
        }
    }

    private void createMetricsTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS metrics (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, nodeId TEXT NOT NULL, timestamp INTEGER NOT NULL," +
                     "os TEXT, cpuType TEXT, cpuLoad REAL, memoryLoad REAL, diskUsage REAL, uptime INTEGER);";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void insertMetric(MetricData metric) {
        String sql = "INSERT INTO metrics(nodeId, timestamp, os, cpuType, cpuLoad, memoryLoad, diskUsage, uptime) VALUES(?,?,?,?,?,?,?,?);";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, metric.getNodeId());
            pstmt.setLong(2, metric.getTimestamp());
            pstmt.setString(3, metric.getOs());
            pstmt.setString(4, metric.getCpuType());
            pstmt.setDouble(5, metric.getCpuLoad());
            pstmt.setDouble(6, metric.getMemoryLoad());
            pstmt.setDouble(7, metric.getDiskUsage());
            pstmt.setLong(8, metric.getUptime());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion des métriques pour {}", metric.getNodeId(), e);
        }
    }

    public List<MetricData> getLatestMetrics(String nodeId, int limit) {
        List<MetricData> metrics = new ArrayList<>();
        String sql = "SELECT * FROM metrics WHERE nodeId LIKE ? ORDER BY timestamp DESC LIMIT ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nodeId + "%");
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
                    metrics.add(metric);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des métriques pour {}", nodeId, e);
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
