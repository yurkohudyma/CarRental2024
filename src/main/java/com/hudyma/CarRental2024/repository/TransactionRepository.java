package com.hudyma.CarRental2024.repository;

import com.hudyma.CarRental2024.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository <Transaction, Integer> {
    @Transactional(readOnly = true)
    List<Transaction> findAll ();

    Optional<Transaction> findById (Integer id);

    @Query(value = "select * from transactions t where t.user_id = ?1", nativeQuery = true)
    List<Transaction> findAllByUserId (Long userId);

}
