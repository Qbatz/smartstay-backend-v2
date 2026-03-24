package com.smartstay.smartstay.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class PaymentSocketController {

    @MessageMapping("/send")
    @SendTo("/consume/message")
    public String getPaymentStatus() {
        return "success";
    }
}
