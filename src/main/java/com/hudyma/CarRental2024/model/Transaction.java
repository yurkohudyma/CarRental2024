package com.hudyma.CarRental2024.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table (name = "transactions")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = "user")
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String body;

    @ManyToOne
    @JsonBackReference(value = "users_transactions")
    @JoinColumn(name = "user_id")
    User user;
}
