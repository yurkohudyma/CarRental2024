package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.OrderService;
import com.hudyma.CarRental2024.constants.UserAccessLevel;
import com.hudyma.CarRental2024.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@RequestMapping("/users")
@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserRepository userRepository;
    private final OrderService orderService;
    private final UserService userService;
    private static final String REDIRECT_USERS = "redirect:/users";

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("userList", userRepository.findAll());
        model.addAttribute("soleUserCard", false);
        model.addAttribute("userOrdersList", new ArrayList<>());
        log.info("...Retrieving All users...");
        return "users";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable Long id, Model model) {
        model.addAttribute("userList",
                List.of(userRepository
                .findById(id)
                .orElseThrow()));
        model.addAttribute("userOrdersList",
                orderService.getOrdersByUserId(id));
        model.addAttribute("soleUserCard", true);
        log.info("...Retrieving user "+id);
        return "users";
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
        } else log.error(
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
            user.setAccessLevel(UserAccessLevel.BLOCKED);
            userRepository.save(user);
        } else log.error("User " + id + " not found");
        return REDIRECT_USERS;
    }

    @PostMapping("/unblock/{id}")
    public String unblockUser(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            log.info("...Blocking user = " + id);
            User user = userRepository.findById(id).orElseThrow();
            user.setAccessLevel(UserAccessLevel.USER);
            userRepository.save(user);
        } else log.error("User " + id + " not found");
        return REDIRECT_USERS;
    }

    @PostMapping("/setMgr/{id}")
    public String setManager(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            log.info("...Blocking user = " + id);
            User user = userRepository.findById(id).orElseThrow();
            user.setAccessLevel(UserAccessLevel.MANAGER);
            userRepository.save(user);
        } else log.error("User " + id + " not found");
        return REDIRECT_USERS;
    }

    @PatchMapping ("/{id}")
    public String editUser (@PathVariable Long id, User user){
        if (user.getId().equals(id)) {
            log.info("updating user "+id);
            User prvUser = userRepository.findById(id).orElseThrow();
            user.setAccessLevel(prvUser.getAccessLevel());
            user = userService.ifNullableMergeOldValues(user, prvUser);
            userRepository.save(user);
        }
        else log.error(
                "Id does not correspond to Editable User");
        return REDIRECT_USERS+"/"+id;
    }
}