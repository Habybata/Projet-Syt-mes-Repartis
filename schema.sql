-- Script de création de la base de données pour le projet de supervision (SQLite)

CREATE TABLE IF NOT EXISTS metrics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nodeId TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    os TEXT,
    cpuType TEXT,
    cpuLoad REAL,
    memoryLoad REAL,
    diskUsage REAL,
    uptime INTEGER,
    services TEXT, -- Contient un JSON des services (ex: {"HTTP":"OK", "SSH":"KO"})
    ports TEXT     -- Contient un JSON des ports (ex: {"80":1, "443":0})
);

-- Index pour accélérer les recherches par nœud et par temps
CREATE INDEX IF NOT EXISTS idx_node_timestamp ON metrics (nodeId, timestamp DESC);
