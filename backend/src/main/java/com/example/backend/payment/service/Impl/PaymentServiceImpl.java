package com.example.backend.payment.service.Impl;
import com.example.backend.order.entity.Order;
import com.example.backend.order.entity.OrderStatus;
import com.example.backend.order.repository.OrderRepository;
import com.example.backend.payment.dto.PaymentConfirmDto;
import com.example.backend.payment.dto.PaymentCreateResponse;
import com.example.backend.payment.dto.RefundRequest;
import com.example.backend.payment.entity.Payment;
import com.example.backend.payment.exception.OrderPaymentNotAllowedException;
import com.example.backend.payment.exception.PaymentNotFoundException;
import com.example.backend.payment.exception.StripeOperationException;
import com.example.backend.payment.repository.PaymentRepository;
import com.example.backend.payment.service.PaymentService;
import com.example.backend.payment.utils.StripeUtils;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
@SuppressWarnings("ALL")
@Slf4j
@Service

@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

private final OrderRepository orderRepository;

private final PaymentRepository paymentRepository;

    @Value("${stripe.secretKey}")
    private String stripeApiKey;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

public PaymentCreateResponse createCheckoutSessionForOrder(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderPaymentNotAllowedException("Order not found: " + orderId));
        if (order.getUser() == null || order.getUser().getEmail() == null ||
                !order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new OrderPaymentNotAllowedException("Order does not belong to authenticated user");
        }

        BigDecimal amountInCentsBD = order.getTotalAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
        long amountInCents = amountInCentsBD.longValueExact();
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Order #" + order.getId())
                        .build();
        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(amountInCents)
                        .setProductData(productData)
                        .build();
        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(priceData)
                        .setQuantity(1L)
                        .build();
        String successUrl = appBaseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl  = appBaseUrl + "/payment/cancel";
        Session session;
        try {
            session = Session.create(
                    SessionCreateParams.builder()
                            .addLineItem(lineItem)
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl(successUrl)
                            .setCancelUrl(cancelUrl)
                            .setCustomerEmail(userEmail)
                            .putMetadata("order_id", String.valueOf(order.getId()))
                            .build()
            );
        } catch (StripeException e) {
            throw new StripeOperationException("Stripe error while creating checkout session", e);
        }

        Payment payment = new Payment(order.getId(), session.getId());
        payment.setStatus(Payment.Status.CREATED);
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setExpiresAt(payment.getCreatedAt().plusMinutes(60));
        paymentRepository.save(payment);
        order.setStatus(OrderStatus.DELIVERED);
        return  PaymentCreateResponse.of(session.getId(), session.getUrl());
    }

    @Override
    public PaymentConfirmDto confirmPaymentBySessionId(String sessionId, String userEmail) {
        Session session;
        PaymentIntent pi = null;
        try {
            session = Session.retrieve(sessionId);
        } catch (StripeException e) {
            throw new StripeOperationException("Stripe error while confirming payment", e);
        }

        String sessionEmail = session.getCustomerEmail();
        if (sessionEmail != null && !sessionEmail.equalsIgnoreCase(userEmail)) {
            throw new OrderPaymentNotAllowedException("Authenticated user does not match session email");
        }

        String orderIdStr = session.getMetadata() != null
                ? session.getMetadata().get("order_id")
                : null;
        if (orderIdStr == null) {
            Payment payment = paymentRepository.findBySessionId(sessionId)
                    .orElseThrow(() ->
                            new PaymentNotFoundException("Payment not found for session: " + sessionId));
            orderIdStr = String.valueOf(payment.getOrderId());
        }

        Order order = orderRepository.findById(Long.valueOf(orderIdStr))
                .orElseThrow(() ->
                        new OrderPaymentNotAllowedException("Order not found"));
        if (!order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new OrderPaymentNotAllowedException("Order does not belong to authenticated user");
        }

        boolean paid = false;
        try {
            if (session.getPaymentIntent() != null) {
                pi = PaymentIntent.retrieve(session.getPaymentIntent());
                paid = "succeeded".equals(pi.getStatus());
            } else if ("paid".equals(session.getPaymentStatus())) {
                paid = true;
            }

        } catch (StripeException e) {
            throw new StripeOperationException("Stripe error while checking payment status", e);
        }

        if (!paid) {
            return PaymentConfirmDto.pending("Payment not completed yet");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        }

        String piId = session.getPaymentIntent();
        PaymentIntent finalPi = pi;
        paymentRepository.findBySessionId(sessionId).ifPresent(p -> {
            if (piId != null && !piId.isBlank()) {
                p.setPaymentIntentId(piId);
            }

            p.setStatus(Payment.Status.PAID);
            p.setPaidAt(java.time.OffsetDateTime.now());
            if (finalPi != null) {
                Long applicationFee = finalPi.getApplicationFeeAmount();
                Long amount = finalPi.getAmount();
                if (finalPi.getTransferData() != null) {
                    String dest = finalPi.getTransferData().getDestination();
                    p.setSellerStripeAccountId(dest);
                }

                if (applicationFee != null) {
                    p.setPlatformFeeAmount(applicationFee);
                }

                if (amount != null) {
                    long fee = (applicationFee != null ? applicationFee : 0L);
                    p.setSellerAmount(amount - fee);
                }
            }

            paymentRepository.save(p);
        });
        return PaymentConfirmDto.ok("Payment confirmed successfully");
    }

    @Override
    public com.stripe.model.Refund refundPaymentForOrder(RefundRequest request, String userEmail) {
        Payment payment = paymentRepository.findByOrderIdAndStatus(request.getOrderId(), Payment.Status.PAID)
                .orElseThrow(() -> new PaymentNotFoundException("Paid payment not found for order id=" + request.getOrderId()));
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderPaymentNotAllowedException("Order not found id=" + request.getOrderId()));
        if (!order.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new OrderPaymentNotAllowedException("User cannot refund this order");
        }

        String paymentIntentId = payment.getPaymentIntentId();
        log.debug("Refund requested for orderId={} paymentId={} paymentIntentId={}",
                request.getOrderId(), payment.getId(), paymentIntentId);
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new PaymentNotFoundException("PaymentIntent ID missing for payment id=" + payment.getId());
        }

        try {
            com.stripe.param.RefundCreateParams.Builder builder = com.stripe.param.RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId);
            if (request.getAmount() != null) {
                BigDecimal originalAmount = order.getTotalAmount();
                if (originalAmount != null && request.getAmount().compareTo(originalAmount) > 0) {
                    throw new IllegalArgumentException("Refund amount cannot be greater than original amount");
                }

                long cents = StripeUtils.amountToCents(request.getAmount());
                builder.setAmount(cents);
            }

            com.stripe.model.Refund stripeRefund = com.stripe.model.Refund.create(builder.build());
            payment.setStatus(Payment.Status.REFUNDED);
            order.setStatus(OrderStatus.REFUNDED);
            payment.setRefundedAt(java.time.OffsetDateTime.now());
            paymentRepository.save(payment);
            return stripeRefund;
        } catch (com.stripe.exception.StripeException e) {
            log.error("Stripe refund failed for paymentId={} paymentIntentId={}: {}", payment.getId(), paymentIntentId, e.getMessage(), e);
            throw new StripeOperationException("Stripe refund failed: " + e.getMessage(), e);
        }
    }
}
