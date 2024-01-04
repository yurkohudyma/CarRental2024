package com.hudyma.CarJPA.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "car_profiles")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString (exclude = "car")
public class CarProfile {

    @Id
    @Column(name = "id")
    //@GeneratedValue(strategy = GenerationType.AUTO) //shall fetch it from CAR as SHARED FK
    Long id;

    @Column(name = "seats_quantity", nullable = false)
    Integer seatsQuantity;

    @Column (name = "color",
            columnDefinition = "ENUM ('RED', 'GREEN', 'WHITE', 'BLACK', 'GREY', 'YELLOW', 'BLUE', 'BROWN')",
            nullable = false)
    String color;

    @Column (name = "propulsion", nullable = false,
            columnDefinition = "ENUM ('DIESEL', 'BENZINE', 'ELECTRIC', 'HYBRID', 'HYDROGEN')")
    String propulsion;

    @JsonBackReference(value = "cars_carProfiles")
    @OneToOne (/*fetch = FetchType.EAGER,*/ optional = false)
    @MapsId
    @JoinColumn(name = "car_id")
    private Car car;















}
