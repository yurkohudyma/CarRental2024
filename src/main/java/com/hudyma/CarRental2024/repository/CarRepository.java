package com.hudyma.CarRental2024.repository;

import com.hudyma.CarRental2024.model.Car;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    @Transactional(readOnly = true)
    List<Car> findAll();

    List<Car> findByAvailableNot (int limit, Sort sort);

    @Query(value = "select model from cars c where c.id = ?1", nativeQuery = true)
    String findModelById (Long carId);

    @Query("update Car c set c.available = c.available - 1 where c.id = :id")
    @Transactional
    @Modifying
    void decrementCarAvailable(Long id);

    @Query("update Car c set c.available = c.available + 1 where c.id = :id")
    @Transactional
    @Modifying
    void incrementCarAvailable(Long id);

    @Transactional(readOnly = true)
    Optional<Car> findById (Long id);

    @Query("delete from Car c where c.id = :id")
    @Modifying
    void deleteById (Long id);
}
