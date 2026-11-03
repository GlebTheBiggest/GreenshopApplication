package com.sproutnbloom.greenshopapplication.listener;

import com.sproutnbloom.greenshopapplication.event.OrderCreatedEvent;
import com.sproutnbloom.greenshopapplication.service.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final EmailService emailService;

    @KafkaListener(topics = "order-created", groupId = "greenshop-order-notifications")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order-created event for order {}", event.orderId());

        String subject = "Greenshop: order #" + event.orderId() + " confirmed";
        String text = """
                Thank you for your order!

                Order number: %d
                Items: %d
                Total amount: %s
                Date: %s
                """.formatted(event.orderId(), event.itemCount(), event.totalAmount(), event.orderDate());

        emailService.send(event.userEmail(), subject, text);
    }
}
