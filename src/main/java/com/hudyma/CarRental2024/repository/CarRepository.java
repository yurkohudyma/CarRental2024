package com.hudyma.CarRental2024.repository;

import com.hudyma.CarRental2024.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findAll();

    Optional<Car> findById (Long id);





}
