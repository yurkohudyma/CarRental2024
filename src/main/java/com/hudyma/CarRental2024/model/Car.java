package com.hudyma.CarRental2024.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @JsonManagedReference(value = "cars_carProfiles")
    @OneToOne(mappedBy = "car", cascade = CascadeType.ALL)
    private CarProfile carProfile;

    public void setProfile (CarProfile carProfile){
        carProfile.setCar(this);
        this.carProfile = carProfile;
    }
    @JsonBackReference(value = "cars_orders")
    @OneToOne
    private Order order;
}
