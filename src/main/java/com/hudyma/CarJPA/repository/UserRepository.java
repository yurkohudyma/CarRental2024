package com.hudyma.CarJPA.repository;

import com.hudyma.CarJPA.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAll();

    Optional<User> findById(Long id);

    @Query("delete from User u where u.id = :id")
    @Modifying
    void deleteById (Long id);
}
