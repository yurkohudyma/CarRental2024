package com.hudyma.CarJPA.repository;

import com.hudyma.CarJPA.model.CarProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarProfileRepository extends JpaRepository <CarProfile, Long> {

    List<CarProfile> findAll();

    Optional<CarProfile> findById (Long id);

    @Query ("delete from CarProfile c where c.id = :id")
    @Modifying
    void deleteById (Long id);
}
