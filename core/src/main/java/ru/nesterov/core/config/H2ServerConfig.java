package ru.nesterov.core.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Slf4j
@Configuration
public class H2ServerConfig {
    private static final Server server = startServer();
    private static final Server uiServer = startUIServer();

    private static Server startServer() {
        try {
            log.info("Starting H2 server with params -tcp -tcpAllowOthers -tcpPort 9092...");
            Server startedServer = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092").start();
            log.info("H2 server started");
            return startedServer;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Server startUIServer() {
        try {
            log.info("Starting H2 ui server with params -web -webAllowOthers -webPort 8082...");
            Server server =  Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            log.info("H2 ui server started");
            return server;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stopServer() {
        log.info("Stopping H2 server...");
        server.stop();
        log.info("H2 server stopped");

        log.info("Stopping H2 ui server...");
        uiServer.stop();
        log.info("H2 ui server stopped");
    }
}
