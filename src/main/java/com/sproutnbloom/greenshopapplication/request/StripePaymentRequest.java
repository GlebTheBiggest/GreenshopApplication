package com.sproutnbloom.greenshopapplication.request;

import lombok.Data;

@Data
public class StripePaymentRequest {
    private Long userId;
    private Long orderId;
    private String currency;
    private String receiptEmail;
}