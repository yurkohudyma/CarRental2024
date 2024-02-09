package com.hudyma.CarRental2024.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Entity(name = "tokens")
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true)
    public String token;

    public boolean revoked, expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;


}
