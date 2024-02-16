package com.hudyma.CarRental2024.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hudyma.CarRental2024.constants.UserAccessLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name = "users")
@Data
@EqualsAndHashCode(of = "id")
@ToString
public class User {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "email", unique = true)
    String email;

    @Column(name = "passport_data", unique = true)
    String passportData;

    @Enumerated(EnumType.STRING)
    @Column (name = "access_level")
    UserAccessLevel accessLevel;

    @Column(name = "register_date")
    LocalDateTime registerDate;

    @Column(name = "update_date")
    LocalDateTime updateDate;

    @Column(name = "balance")
    Double balance;

    @JsonManagedReference(value = "users_orders")
    @OneToMany (mappedBy = "user",
            //todo ORDER.FK manually CHANGED DB to CASCADE, CascadeType.ALL does not work
            cascade = CascadeType.ALL,
            //todo WHEN LAZY, restFUL fetches no data on user's retrieval
            fetch = FetchType.EAGER)
    @Setter(AccessLevel.PRIVATE)
    private List<Order> orderList = new ArrayList<>();

    public void addOrder (Order order){
        orderList.add(order);
        order.setUser(this);
    }

    public void removeOrder (Order order){
        orderList.remove(order);
        order.setUser(null);
    }

    public void updateOrder (Order updatedOrder){
        int idx = orderList.indexOf(updatedOrder);
        orderList.remove(idx);
        orderList.add(updatedOrder);
    }




}
