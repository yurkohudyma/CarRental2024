package com.hudyma.CarRental2024.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hudyma.CarRental2024.constants.UserAccessLevel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table (name = "users")
@Data
@EqualsAndHashCode(of = "id")
@ToString
public class User implements UserDetails {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "email", unique = true)
    String email;

    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password;

    @Column(name = "passport_data", unique = true)
    String passportData;

    @Enumerated(EnumType.STRING)
    @Column (name = "access_level")
    UserAccessLevel accessLevel;

    @Column(name = "register_date")
    LocalDateTime registerDate;

    @Column(name = "update_date")
    LocalDateTime updateDate;



    //todo Cannot delete or update a parent row: a foreign key constraint fails
    //todo MySQL has a RESTRICT option on order.user_id constraint,
    //todo change manually to CASCADE
    @JsonManagedReference(value = "users_orders")
    @OneToMany (mappedBy = "user",
            cascade = CascadeType.ALL,
            //todo WHEN LAZY, restFUL fetches no data on user's retrieval
            fetch = FetchType.EAGER)
    @Setter(AccessLevel.PRIVATE)
    private List<Order> orderList = new ArrayList<>();

    @OneToMany (mappedBy = "user")
    private List<Token> tokens;

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(
                "ROLE_" + this.getAccessLevel().name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }




}
