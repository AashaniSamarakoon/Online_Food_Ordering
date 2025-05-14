package com.sudarshan.paymentservice.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.sudarshan.paymentservice.dto.PaymentRequest;
import com.sudarshan.paymentservice.dto.StripeResponse;
import com.sudarshan.paymentservice.entity.Payment;
import com.sudarshan.paymentservice.enums.PaymentStatus;
import com.sudarshan.paymentservice.enums.PaymentType;
import com.sudarshan.paymentservice.enums.RefundStatus;
import com.sudarshan.paymentservice.exceptions.StripePollingException;
import com.sudarshan.paymentservice.exceptions.StripeSessionCreationException;
import com.sudarshan.paymentservice.repository.PaymentRepository;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@CrossOrigin
public class StripeServiceImpl implements StripeService {

    private final PaymentRepository paymentRepository;
    private String secretKey;

    public StripeServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.secretKey = dotenv.get("STRIPE_SECRET_KEY");
    }

    @Override
    public StripeResponse checkoutProducts(PaymentRequest paymentRequest) {
        Stripe.apiKey = secretKey;

        long mealPrice = paymentRequest.getMealPrice();
        long deliveryCharge = paymentRequest.getDeliveryCharge();
        int numberOfMeals = paymentRequest.getNumberOfMeals();
        long totalAmount = paymentRequest.getTotalAmount();
        long companyCommission = (long) (mealPrice * 0.10);

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(paymentRequest.getMealName())
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(paymentRequest.getCurrency().toString().toLowerCase())
                        .setUnitAmount(totalAmount * 100)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(priceData)
                        .build();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:8080/success")
                        .setCancelUrl("http://localhost:8080/cancel")
                        .addLineItem(lineItem)
                        .build();

        Session session = null;
        try {
            session = Session.create(params);
        } catch (StripeException e) {
            log.error("Stripe session creation failed: {}", e.getMessage());
            throw new StripeSessionCreationException("Stripe session could not be created: " + e.getMessage());
        }

        Payment payment = Payment.builder()
                .orderId(paymentRequest.getOrderId())
                .restaurantId(paymentRequest.getRestaurantId())
                .riderId(paymentRequest.getRiderId())
                .paymentType(PaymentType.CARD)
                .totalAmount(totalAmount)
                .mealPrice(mealPrice)
                .deliveryCharge(deliveryCharge)
                .numberOfMeals(numberOfMeals)
                .companyCommission(companyCommission)
                .paymentStatus(PaymentStatus.PENDING)
                .refundStatus(RefundStatus.NOT_REQUESTED)
                .createdAt(LocalDateTime.now())
                .mealName(paymentRequest.getMealName())
                .currency(paymentRequest.getCurrency())
                .restaurantBalance(mealPrice - companyCommission)
                .sessionId(session.getId())
                .build();

        paymentRepository.save(payment);

        return StripeResponse
                .builder()
                .status("SUCCESS")
                .message("Payment session created ")
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void pollStripeForPendingPayments() {
        log.info("Polling Stripe for pending payments...");

        List<Payment> pendingPayments = paymentRepository.findByPaymentStatus(PaymentStatus.PENDING);

        for (Payment payment : pendingPayments) {
            try {
                String sessionId = payment.getSessionId();

                if (sessionId == null) {
                    log.warn("Skipping payment {} due to null session ID", payment.getOrderId());
                    continue;
                }

                Session session = Session.retrieve(sessionId);

                String paymentIntentId = session.getPaymentIntent();
                if (paymentIntentId == null) {
                    log.warn("Session {} has no PaymentIntent associated", sessionId);
                    continue;
                }

                PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

                switch (paymentIntent.getStatus()) {
                    case "succeeded":
                        payment.setPaymentStatus(PaymentStatus.COMPLETED);
                        break;
                    case "requires_payment_method":
                    case "canceled":
                        payment.setPaymentStatus(PaymentStatus.FAILED);
                        break;
                    default:
                        continue;
                }

                paymentRepository.save(payment);
                log.info("Updated status for sessionId {} (PaymentIntent {}): {}", sessionId, paymentIntentId, payment.getPaymentStatus());

            } catch (StripeException e) {
                log.error("Failed to update status for payment {}: {}", payment.getOrderId(), e.getMessage());
                throw new StripePollingException("Polling error for payment " + payment.getOrderId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public List<Payment> getPaymentsByRestaurantId(Long restaurantId) {
        return paymentRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public List<Payment> getPaymentsByRiderId(Long riderId) {
        return paymentRepository.findByRiderId(riderId);
    }

    @Override
    public List<Payment> getPaymentsByRestaurantAndRider(Long restaurantId, Long riderId) {
        return paymentRepository.findByRestaurantIdAndRiderId(restaurantId, riderId);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment updateRiderId(Long paymentId, Long newRiderId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        payment.setRiderId(newRiderId);
        return paymentRepository.save(payment);
    }
}
