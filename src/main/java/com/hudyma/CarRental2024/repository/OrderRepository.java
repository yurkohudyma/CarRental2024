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

    List<Order> findAllByUserId(Long id);

    @Query(value = "select * from orders o where o.user_id = ?1 and o.status = 'RECEIVED' and o.date_end < now()",
            nativeQuery = true)
    List<Order> findAllDelayedReturn(Long userId);

    @Query(value = "select * from orders o where o.user_id = ?1 and o.status = 'CONFIRMED' and o.date_begin < now()",
            nativeQuery = true)
    List<Order> findAllNonPaidAndExpiredOrders(Long userId);

    @Transactional(readOnly = true)
    Optional<Order> findById(Long id);

    @Query("delete from Order o where o.id = :id")
    @Modifying
    void deleteById (Long id);

    @Transactional(readOnly = true)
    List<Order>findAllByCarId (Long id);
}
