package com.hudyma.CarRental2024.restcontroller;

import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@Log4j2
@RequestMapping("/api/users")
@RequiredArgsConstructor
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
