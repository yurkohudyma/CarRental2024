package com.hudyma.CarRental2024.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hudyma.CarRental2024.constants.CarClass;
import com.hudyma.CarRental2024.constants.CarColor;
import com.hudyma.CarRental2024.constants.CarPropulsion;
import jakarta.persistence.*;
import lombok.*;

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

    /*@JsonManagedReference(value = "cars_carProfiles")
    @OneToOne(mappedBy = "car", cascade = CascadeType.ALL)
    private CarProfile carProfile;

    public void setProfile (CarProfile carProfile){
        carProfile.setCar(this);
        this.carProfile = carProfile;
    }*/
    @JsonBackReference(value = "cars_orders")
    @OneToOne
    private Order order;
}
