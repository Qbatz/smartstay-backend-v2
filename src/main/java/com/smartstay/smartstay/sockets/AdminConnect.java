package com.smartstay.smartstay.sockets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

@Component
public class AdminConnect {
    @Value("${ENVIRONMENT}")
    private String environment;

    public void connect() throws ExecutionException, InterruptedException {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String baseUrl = "ws://localhost:8081/ws";

        if (environment.equalsIgnoreCase("PROD")) {
            baseUrl = "wss://ssconsoleapi.qbatz.com/ws";
        }

        StompSession session = stompClient.connectAsync(baseUrl,
                new StompSessionHandlerAdapter() {

                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//                        super.afterConnected(session, connectedHeaders);
                        System.out.println("✅ Connected to WebSocket");
                    }

                    @Override
                    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
//                        super.handleException(session, command, headers, payload, exception);
                        System.out.println("❌ Exception: " + exception.getMessage());
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
//                        super.handleTransportError(session, exception);
                        System.out.println("❌ Transport Error: " + exception.getMessage());
                    }
                }).get();
    }
}
