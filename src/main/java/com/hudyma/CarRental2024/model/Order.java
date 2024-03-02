package com.hudyma.CarRental2024.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hudyma.CarRental2024.constants.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(of = "id")
public class Order {

    @Id
    @GeneratedValue (strategy =
            GenerationType.IDENTITY)
    Long id;

    @Column(name = "amount")
    Double amount;

    @Column(name = "date_begin")
    LocalDate dateBegin;

    @Column(name = "date_end")
    LocalDate dateEnd;

    @Column(name = "duration")
    Long duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    OrderStatus status;

    @Column(name = "aux_needed")
    Boolean auxNeeded;

    @Column(name = "register_date")
    LocalDateTime registerDate;

    @Column(name = "update_date")
    LocalDateTime updateDate;

    @Column(name = "rental_payment")
    Double rentalPayment;

    @Column(name = "payment_date")
    LocalDateTime paymentDate;

    Double deposit;

    @Column(name = "aux_payment")
    Double auxPayment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "car_id")
    private Car car;

    @JsonBackReference(value = "users_orders")
    @ManyToOne(optional = false)
    //@MapsId
    @JoinColumn(name = "user_id")
    private User user;

}