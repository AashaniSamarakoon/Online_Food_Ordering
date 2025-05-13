package com.restaurant_service.restaurant_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private boolean open;
    private String imageUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "restaurant_latitude", nullable = false)),
            @AttributeOverride(name = "longitude", column = @Column(name = "restaurant_longitude", nullable = false))
    })
    private Coordinates restaurantCoordinates;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FoodItem> items;

    @ElementCollection
    private List<String> categories;
}
