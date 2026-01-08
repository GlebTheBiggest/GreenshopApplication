package com.sproutnbloom.greenshopapplication.service.stripe;

import com.stripe.exception.StripeException;
import org.springframework.stereotype.Service;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class StripeService implements IStripeService {

    @Override
    public PaymentIntent createPaymentIntent(Long amountInCents, String currency, String receiptEmail) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .setReceiptEmail(receiptEmail)
                .build();
        return PaymentIntent.create(params);
    }
}