package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequestMapping("/users")
@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserRepository userRepository;
    private static final String REDIRECT_USERS = "redirect:/users";

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("userList", userRepository.findAll());
        return "users";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable Long id, Model model) {
        model.addAttribute("userList",
                List.of(userRepository
                .findById(id)
                .orElseThrow()));
        System.out.println(model.asMap());
        return REDIRECT_USERS;
    }

    @PostMapping
    public String addUser(User user) {
        userRepository.save(user);
        return REDIRECT_USERS;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
        } else log.info(
                "...User " + id + " does not EXIST");
        return REDIRECT_USERS;
    }

    @DeleteMapping
    public String deleteAll() {
        userRepository
                .findAll()
                .forEach(userRepository::delete);
        return REDIRECT_USERS;
    }

    @PostMapping("/block/{id}")
    public String blockUser(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            log.info("...Blocking user = " + id);
            User user = userRepository.findById(id).orElseThrow();
            user.setAccessLevel("BLOCKED");
            userRepository.save(user);
        } else log.info("User " + id + " not found");
        return REDIRECT_USERS;
    }

    @PostMapping("/unblock/{id}")
    public String unblockUser(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            log.info("...Blocking user = " + id);
            User user = userRepository.findById(id).orElseThrow();
            user.setAccessLevel("USER");
            userRepository.save(user);
        } else log.info("User " + id + " not found");
        return REDIRECT_USERS;
    }

    @PostMapping("/setMgr/{id}")
    public String setManager(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            log.info("...Blocking user = " + id);
            User user = userRepository.findById(id).orElseThrow();
            user.setAccessLevel("MANAGER");
            userRepository.save(user);
        } else log.info("User " + id + " not found");
        return REDIRECT_USERS;
    }


    /*@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addUsers (@RequestBody User[] users){
        Arrays.stream(users)
                .forEach(this::addUser);
    }*/

}