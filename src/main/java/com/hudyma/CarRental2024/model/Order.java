package com.hudyma.CarRental2024.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hudyma.CarRental2024.constants.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"car","user"})
@EqualsAndHashCode(of = "id")
public class Order {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "amount", nullable = false)
    Double amount;

    @Column(name = "date_begin")
    LocalDate dateBegin;

    @Column(name = "date_end")
    LocalDate dateEnd;

    @Column(name = "durability")
    Long durability;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    OrderStatus status;

    @Column(name = "aux_needed")
    Boolean auxNeeded;

    //@JsonManagedReference(value = "cars_orders")
    //todo upon creating new table HIBER creates `car_id` CONSTRAINT column as unique
    @OneToOne
    @JoinColumn(unique = false, name = "car_id") //todo this does not help, one must delete CONSTRAINT from DB manually
    private Car car;

    @JsonBackReference(value = "users_orders")
    @ManyToOne(optional = false)
    //@MapsId
    @JoinColumn(name = "user_id")
    private User user;

}