package com.hudyma.CarRental2024.repository;

import com.hudyma.CarRental2024.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Transactional(readOnly = true)
    List<User> findAll();

    @Transactional(readOnly = true)
    Optional<User> findById(Long id);

    @Query("delete from User u where u.id = :id")
    @Modifying
    void deleteById (Long id);
}
