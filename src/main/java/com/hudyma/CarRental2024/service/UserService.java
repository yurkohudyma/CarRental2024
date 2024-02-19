package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserService {
    public static final String IS_MISSING_FOR_USER = "{} is missing for User {}";
    private final UserRepository userRepository;

    public int getAllUsersQuantity() {
        return userRepository.findAll().size();
    }

    public List<User> getAllUsersSortedByFieldAsc(String sortField) {
        return userRepository.findAll(Sort.by(
                Sort.Direction.ASC, sortField));
    }

    public List<User> getAllUsersSortedByFieldAsc() {
        return userRepository.findAll(Sort.by(
                Sort.Direction.ASC, "name"));
    }

    public User ifNullableMergeOldValues(User user, User prvUser) {
        if (user.getName().equals("")) {
            user.setName(prvUser.getName());
            log.error(IS_MISSING_FOR_USER, "name", user.getId());
        }
        if (user.getEmail().equals("")) {
            user.setEmail(prvUser.getEmail());
            log.error(IS_MISSING_FOR_USER, "email", user.getId());
        }
        if (user.getPassportData().equals("")) {
            user.setPassportData(prvUser.getPassportData());
            log.error(IS_MISSING_FOR_USER, "passportData", user.getId());
        }
        return user;
    }
}
