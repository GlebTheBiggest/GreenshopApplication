package com.sproutnbloom.greenshopapplication.controller.user;

import com.sproutnbloom.greenshopapplication.dto.OrderDto;
import com.sproutnbloom.greenshopapplication.exceptions.InsufficientStockException;
import com.sproutnbloom.greenshopapplication.exceptions.ResourceNotFoundException;
import com.sproutnbloom.greenshopapplication.model.Order;
import com.sproutnbloom.greenshopapplication.model.User;
import com.sproutnbloom.greenshopapplication.request.StripePaymentRequest;
import com.sproutnbloom.greenshopapplication.response.ApiResponse;
import com.sproutnbloom.greenshopapplication.response.PaymentResponse;
import com.sproutnbloom.greenshopapplication.service.order.IOrderService;
import com.sproutnbloom.greenshopapplication.service.stripe.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/user/orders")
public class UserOrderController {
    private final IOrderService orderService;
    private final StripeService stripeService;

    @PostMapping("/create-with-payment")
    public ResponseEntity<ApiResponse> createOrderWithPayment(@RequestBody StripePaymentRequest request, @AuthenticationPrincipal User currentUser) {
        try {
            // 1) Створюємо замовлення в базі
            Order order = orderService.placeOrder(currentUser.getId());
            OrderDto dto = orderService.convertToDto(order);

            // 2) Готуємо суму в копійках
            Long cents = order.getTotalAmount()
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .longValue();

            // 3) Створюємо Stripe PaymentIntent
            PaymentIntent intent = stripeService.createPaymentIntent(cents, request.getCurrency(), request.getReceiptEmail());

            // 4) Повертаємо клієнтську частину для підтвердження (client_secret)
            return ResponseEntity.ok(new ApiResponse(
                    "Order created and payment initialized",
                    new PaymentResponse(dto, intent.getClientSecret())
            ));

        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Insufficient stock", e.getMessage()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Stripe error", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error Occurred!", e.getMessage()));
        }
    }

    /*@PostMapping("/order")
    public ResponseEntity<ApiResponse> createOrder(@RequestParam Long userId) {
        try {
            Order order = orderService.placeOrder(userId);
            OrderDto orderDto = orderService.convertToDto(order);
            return ResponseEntity.ok(new ApiResponse("Item Order Success!", orderDto));
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Insufficient stock", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error Occurred!", e.getMessage()));
        }
    }*/
}