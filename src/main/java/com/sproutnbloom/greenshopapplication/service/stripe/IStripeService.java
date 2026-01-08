package com.sproutnbloom.greenshopapplication.service.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface IStripeService {
    PaymentIntent createPaymentIntent(Long amount, String currency, String receiptEmail) throws StripeException;
}