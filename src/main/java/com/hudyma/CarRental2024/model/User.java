package com.hudyma.CarRental2024.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name = "users")
@Getter
@Setter
@NoArgsConstructor
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

    @Column (name = "access_level", nullable = false,
            columnDefinition = "ENUM ('ADMIN', 'USER', 'MANAGER', 'BLOCKED')")
    String accessLevel;

    //todo Cannot delete or update a parent row: a foreign key constraint fails
    @JsonManagedReference(value = "users_orders")
    @OneToMany (mappedBy = "user",
            cascade = CascadeType.ALL,
            //todo restFUL fetches no data on user's retrieval
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
