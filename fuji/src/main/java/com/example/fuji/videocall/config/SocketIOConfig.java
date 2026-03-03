package com.example.fuji.videocall.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Wires up the netty-socketio SocketIOServer bean.
 *
 * The server runs on a separate port (default 8081) from the main
 * Spring MVC REST server (8080), so they do not interfere.
 */
@org.springframework.context.annotation.Configuration
@Slf4j
public class SocketIOConfig {

    @Value("${socketio.port:8081}")
    private int port;

    @Value("${socketio.host:0.0.0.0}")
    private String host;

    @Value("${socketio.max-frame-payload-length:1048576}")
    private int maxFramePayloadLength;

    @Value("${socketio.ping-interval:25000}")
    private int pingInterval;

    @Value("${socketio.ping-timeout:60000}")
    private int pingTimeout;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setMaxFramePayloadLength(maxFramePayloadLength);
        config.setMaxHttpContentLength(maxFramePayloadLength);
        config.setPingInterval(pingInterval);
        config.setPingTimeout(pingTimeout);

        // Allow cross-origin from the Next.js frontend
        config.setOrigin("*");

        // Disable auto-start — we start it in ApplicationStartup
        config.setAllowCustomRequests(true);

        log.info("[SocketIO] Configured on {}:{}", host, port);
        return new SocketIOServer(config);
    }
}
