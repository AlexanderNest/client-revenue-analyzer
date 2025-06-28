package ru.nesterov.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Slf4j
@Configuration
public class H2ServerConfig {
    private static final Server server;

    static {
        server = startServer();
    }

    private static Server startServer() {
        try {
            log.info("Starting H2 server with params -tcp -tcpAllowOthers -tcpPort 9092...");
            Server starterServer = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092").start();
            log.info("H2 server started");
            return starterServer;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stopServer() {
        log.info("Stopping H2 server...");
        server.stop();
        log.info("H2 server stopped");
    }
}
