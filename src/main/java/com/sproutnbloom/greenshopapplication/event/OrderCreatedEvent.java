package com.sproutnbloom.greenshopapplication.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        String userEmail,
        BigDecimal totalAmount,
        int itemCount,
        LocalDateTime orderDate
) {
}
