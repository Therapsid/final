package com.example.backend.payment.service.Impl;
import com.example.backend.payment.entity.Payment;
import com.example.backend.payment.repository.PaymentRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor

@Slf4j
public class PaymentExpirationService {

private final PaymentRepository paymentRepository;

    @Value("${payment.session.ttl.minutes:60}")
    private long ttlMinutes;

    @Scheduled(cron = "${payment.expire-check-cron}")
    public void expireOldPayments() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(ttlMinutes);
        List<Payment> toExpire = paymentRepository.findByStatusAndCreatedAtBefore(Payment.Status.CREATED, cutoff);
        for (Payment p : toExpire) {
            try {
                Session session = Session.retrieve(p.getSessionId());
                boolean paid = false;
                if (session.getPaymentIntent() != null) {
                    PaymentIntent pi = PaymentIntent.retrieve(session.getPaymentIntent());
                    paid = "succeeded".equals(pi.getStatus());
                } else if ("paid".equals(session.getPaymentStatus())) {
                    paid = true;
                }

                if (paid) {
                    p.setStatus(Payment.Status.PAID);
                    p.setPaidAt(OffsetDateTime.now());
                    paymentRepository.save(p);
                    continue;
                }

                p.setStatus(Payment.Status.FAILED);
                paymentRepository.save(p);
                if (session.getPaymentIntent() != null) {
                    try {
                        PaymentIntent pi = PaymentIntent.retrieve(session.getPaymentIntent());
                        pi.cancel();
                    } catch (Exception e) {
                        log.warn("Could not cancel PaymentIntent for session {}: {}", session.getId(), e.getMessage());
                    }
                }

            } catch (Exception ex) {
                log.error("Error processing payment expiration for payment {}: {}", p.getId(), ex.getMessage(), ex);
            }
        }
    }
}
