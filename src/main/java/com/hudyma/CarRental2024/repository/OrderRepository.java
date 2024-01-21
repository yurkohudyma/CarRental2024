package com.hudyma.CarRental2024.repository;

import com.hudyma.CarRental2024.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Transactional(readOnly = true)
    List<Order> findAll();

    @Transactional(readOnly = true)
    Optional<Order> findById(Long id);

    @Query("delete from Order o where o.id = :id")
    @Modifying
    void deleteById (Long id);
}
