package com.order_service.order_service.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {
    private Double latitude;
    private Double longitude;
}
