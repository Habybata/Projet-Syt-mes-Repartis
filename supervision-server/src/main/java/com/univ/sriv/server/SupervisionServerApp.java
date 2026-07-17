package com.univ.sriv.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Application principale du serveur.
 */
public class SupervisionServerApp {
    private static final Logger logger = LoggerFactory.getLogger(SupervisionServerApp.class);
    private static final int PORT = 1234;
    private static final int THREAD_POOL_SIZE = 20;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final DatabaseManager dbManager = new DatabaseManager();
    private final ConcurrentHashMap<String, NodeStatus> activeNodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> pendingCommands = new ConcurrentHashMap<>(); // nodeId -> commande
    private final ExecutorService clientPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final ScheduledExecutorService monitorScheduler = Executors.newSingleThreadScheduledExecutor();
    private ServerSocket serverSocket;

    public void start() {
        dbManager.initialize();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        // Démarrage de la console et du moniteur de statut
        new Thread(new AdminConsole(dbManager, activeNodes, pendingCommands, this)).start();
        monitorScheduler.scheduleAtFixedRate(new NodeStatusMonitor(activeNodes), 0, 60, TimeUnit.SECONDS);

        try {
            serverSocket = new ServerSocket(PORT);
            logger.info("Serveur démarré sur le port {}.", PORT);
            while (running.get()) {
                Socket clientSocket = serverSocket.accept();
                clientPool.submit(new ClientHandler(clientSocket, dbManager, activeNodes, pendingCommands));
            }
        } catch (IOException e) {
            if (running.get()) {
                logger.error("Erreur sur le socket serveur.", e);
            }
        } finally {
            shutdown();
        }
    }

    public boolean isShuttingDown() {
        return !running.get();
    }

    public void shutdown() {
        if (!running.compareAndSet(true, false)) {
            return; // Déjà en cours d'arrêt
        }
        logger.info("Arrêt du serveur...");
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Erreur lors de la fermeture du socket serveur : {}", e.getMessage());
            }
        }

        monitorScheduler.shutdownNow();
        clientPool.shutdown();
        try {
            if (!clientPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientPool.shutdownNow();
        }
        dbManager.shutdown();
        logger.info("Serveur arrêté.");
    }

    public static void main(String[] args) {
        // Sur Windows, PowerShell/Console n'utilisent pas toujours UTF-8, ce qui peut casser
        // l'affichage des accents. Pour une sortie correcte, exécutez :
        //   chcp 65001
        // et/ou lancez Java avec :
        //   -Dfile.encoding=UTF-8
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                PrintStream utf8Out = new PrintStream(System.out, true, "UTF-8");
                PrintStream utf8Err = new PrintStream(System.err, true, "UTF-8");
                System.setOut(utf8Out);
                System.setErr(utf8Err);
            } catch (UnsupportedEncodingException ignored) {
                // Continue avec le comportement par défaut.
            }
        }

        new SupervisionServerApp().start();
    }
}
