package com.smartstay.smartstay.sockets;

import com.smartstay.smartstay.payloads.subscription.ZohoPaymentResponse;
import com.smartstay.smartstay.services.OrderHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

//This client connection is used for connect with payments.
@Component
public class ClientConnect {
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${ENVIRONMENT}")
    private String environment;

    public ClientConnect(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void connect(String paymentId) throws ExecutionException, InterruptedException {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String baseUrl = "wss://paymentdev.qbatz.com//ws";
//        "wss://payment.qbatz.com/ws",
        if (environment.equalsIgnoreCase("PROD")) {
            baseUrl = "wss://payment.qbatz.com//ws";
        }

        StompSession session = stompClient.connectAsync(
                "wss://paymentdev.qbatz.com//ws",
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        System.out.println("✅ Connected to WebSocket");
                    }

                    @Override
                    public void handleException(StompSession session, StompCommand command,
                                                StompHeaders headers, byte[] payload, Throwable exception) {
                        System.out.println("❌ Exception: " + exception.getMessage());
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        System.out.println("❌ Transport Error: " + exception.getMessage());
                    }
                }
        ).get();

        session.subscribe("/payments/" + paymentId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                System.out.println("getting payment");
                return ZohoPaymentResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof ZohoPaymentResponse paymentDetails) {
                    ZohoPaymentResponse paymentResponse = (ZohoPaymentResponse) payload;
                    if (paymentResponse.linkId() == null) {
//                        orderHistoryService.successfullMobilePayment(payload);
                        messagingTemplate.convertAndSend("/payments/" + paymentDetails.paymentSessionId(), "success");
                    }
                    else {
//                        orderHistoryService.successfullPayment(payload);
                        messagingTemplate.convertAndSend("/payments/" + paymentDetails.linkId(), "success");
                    }

                }

            }
        });

    }
}
