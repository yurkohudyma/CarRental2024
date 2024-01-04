package com.hudyma.CarJPA.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Column(name = "order_status",
            columnDefinition = "ENUM ('REQUESTED', 'PENDING', " +
                    "'APPROVED', 'PAID', 'DECLINED')")
    String orderStatus;

    @Column(name = "aux_needed")
    Boolean auxNeeded;

    @JsonManagedReference(value = "cars_orders")
    @OneToOne
    @JoinColumn(name = "car_id")
    private Car car;

    public void setCar(Car car) {
        car.setOrder(this);
        this.car = car;
    }

    @JsonBackReference(value = "users_orders")
    @ManyToOne(optional = false)
    //@MapsId
    @JoinColumn(name = "user_id")
    private User user;

}