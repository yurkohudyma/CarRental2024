package com.hudyma.CarRental2024.model;

import com.hudyma.CarRental2024.constants.CarClass;
import com.hudyma.CarRental2024.constants.CarColor;
import com.hudyma.CarRental2024.constants.CarPropulsion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "model", nullable = false)
    String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_class", nullable = false)
    CarClass carClass;

    String description;

    @Column(name = "price", nullable = false)
    Double price;

    @Column(name = "seats_quantity", nullable = false)
    Integer seatsQuantity;

    @Enumerated(EnumType.STRING)
    @Column (name = "color", nullable = false)
    CarColor color;

    @Enumerated(EnumType.STRING)
    @Column (name = "propulsion", nullable = false)
    CarPropulsion propulsion;

    @Column(name = "register_date")
    LocalDateTime registerDate;

    @Column(name = "update_date")
    LocalDateTime updateDate;
}
