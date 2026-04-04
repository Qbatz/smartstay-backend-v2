package com.smartstay.smartstay.sockets;

import com.smartstay.smartstay.payloads.subscription.PaymentLinks;
import com.smartstay.smartstay.services.OrderHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

@Component
public class ClientConnect {
    private final OrderHistoryService orderHistoryService;
    private final SimpMessagingTemplate messagingTemplate;

    public ClientConnect(OrderHistoryService orderHistoryService,
                         SimpMessagingTemplate messagingTemplate) {
        this.orderHistoryService = orderHistoryService;
        this.messagingTemplate = messagingTemplate;
    }

    public void connect(String paymentId) throws ExecutionException, InterruptedException {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = stompClient.connectAsync(
                "wss://payment.qbatz.com/ws",
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    }

                    @Override
                    public void handleException(StompSession session, StompCommand command,
                                                StompHeaders headers, byte[] payload, Throwable exception) {
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                    }
                }
        ).get();

        session.subscribe("/payments/" + paymentId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return PaymentLinks.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof PaymentLinks paymentLinks) {
                    orderHistoryService.successfullPayment(payload);
                    messagingTemplate.convertAndSend("/payments/" + paymentLinks.paymentLinkId(), "success");
                }

            }
        });

    }
}
