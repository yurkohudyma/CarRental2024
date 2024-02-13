package com.hudyma.CarRental2024.controller.restcontroller;

import com.hudyma.CarRental2024.constants.UserAccessLevel;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@Log4j2
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserRestController {

    private final UserRepository userRepository;

    @GetMapping
    public List<User> getAll (){
        return userRepository.findAll();
    }

    @GetMapping("{id}")
    public Optional<User> getById (@PathVariable Long id){
        return userRepository.findById(id);
    }

    @PostMapping("/one")
    @ResponseStatus(HttpStatus.CREATED)
    public void addUser (@RequestBody User user){
        if (user.getAccessLevel() == null) user.setAccessLevel(UserAccessLevel.USER);
        user.setRegisterDate(LocalDateTime.now());
        userRepository.save(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addUsers (@RequestBody User[] users){
        Arrays.stream(users)
                .forEach(this::addUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete (@PathVariable Long id){
        if (userRepository.findById(id).isPresent()){
            userRepository.deleteById(id);
        }
        else log.info(
                "..... User "+ id +" does not EXIST");
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll (){
        userRepository
                .findAll()
                .forEach(userRepository::delete);
    }
}
