package com.sudarshan.paymentservice.entity;

import com.sudarshan.paymentservice.enums.Currency;
import com.sudarshan.paymentservice.enums.PaymentStatus;
import com.sudarshan.paymentservice.enums.PaymentType;
import com.sudarshan.paymentservice.enums.RefundStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId; // Auto-generated primary key

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Restaurant ID cannot be null")
    private Long restaurantId;

    @NotNull(message = "Rider ID cannot be null")
    private Long riderId;

    @NotNull(message = "Payment type is required")
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @NotNull(message = "Total amount cannot be null")
    @Positive(message = "Total amount must be positive")
    private Long totalAmount;

    @NotNull(message = "Meal price cannot be null")
    @PositiveOrZero(message = "Meal price cannot be negative")
    private Long mealPrice;

    @NotNull(message = "Delivery charge cannot be null")
    @PositiveOrZero(message = "Delivery charge cannot be negative")
    private Long deliveryCharge;

    @NotNull(message = "Number of meals is required")
    @Min(value = 1, message = "There must be at least one meal")
    private Integer numberOfMeals;

    @NotNull(message = "Company commission cannot be null")
    @PositiveOrZero(message = "Company commission cannot be negative")
    private Long companyCommission;

    @NotNull(message = "Restaurant balance cannot be null")
    @PositiveOrZero(message = "Restaurant balance cannot be negative")
    private Long restaurantBalance;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @NotNull(message = "Refund status is required")
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;

    @NotBlank(message = "Meal name cannot be blank")
    private String mealName;

    @NotNull(message = "Currency is required")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @NotNull(message = "Creation time cannot be null")
    private LocalDateTime createdAt;

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;
}
