package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.OrderService;
import com.hudyma.CarRental2024.constants.UserAccessLevel;
import com.hudyma.CarRental2024.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@RequestMapping("/users")
@RequiredArgsConstructor
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    public static final String BLOCKING_USER = "...Blocking user = ", USER = "...User ", NOT_FOUND = "not found";
    public static final String USER_LIST = "userList", USER_ORDERS_LIST = "userOrdersList";
    public static final String SOLE_USER_CARD = "soleUserCard", REDIRECT_USERS = "redirect:/users";
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute(USER_LIST, userRepository.findAll());
        model.addAttribute(SOLE_USER_CARD, false);
        model.addAttribute(USER_ORDERS_LIST, new ArrayList<>());
        log.info("...Retrieving All users...");
        return "users";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable Long id, Model model) {
        model.addAttribute(USER_LIST,
                List.of(userRepository
                .findById(id)
                .orElseThrow()));
        model.addAttribute(USER_ORDERS_LIST,
                orderService.getOrdersByUserId(id));
        model.addAttribute(SOLE_USER_CARD, true);
        log.info("...Retrieving user "+id);
        return "users";
    }

    @PostMapping
    public String addUser(User user) {
        userRepository.save(user);
        user.setRegisterDate(LocalDateTime.now());
        return REDIRECT_USERS;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
            log.info("...deleting user {}", id);
        } else log.error(
                USER + id + " " + NOT_FOUND);
        return REDIRECT_USERS;
    }

    @DeleteMapping
    public String deleteAll() {
        userRepository
                .findAll()
                .forEach(userRepository::delete);
        log.info("...deleting All users");
        return REDIRECT_USERS;
    }

    @PostMapping("/block/{id}")
    public String blockUser(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            log.info(BLOCKING_USER + id);
            User user = userRepository.findById(id).orElseThrow();
            user.setAccessLevel(UserAccessLevel.BLOCKED);
            user.setUpdateDate(LocalDateTime.now());
            userRepository.save(user);
        } else log.error(USER + id + " " + NOT_FOUND);
        return REDIRECT_USERS;
    }

    @PostMapping("/unblock/{id}")
    public String unblockUser(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            log.info(BLOCKING_USER + id);
            User user = userRepository.findById(id).orElseThrow();
            user.setAccessLevel(UserAccessLevel.USER);
            user.setUpdateDate(LocalDateTime.now());
            userRepository.save(user);
        } else log.error(USER + id + " " + NOT_FOUND);
        return REDIRECT_USERS;
    }

    @PostMapping("/setMgr/{id}")
    public String setManager(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            log.info(BLOCKING_USER + id);
            User user = userRepository.findById(id).orElseThrow();
            user.setAccessLevel(UserAccessLevel.MANAGER);
            user.setUpdateDate(LocalDateTime.now());
            userRepository.save(user);
        } else log.error(USER + id + " " + NOT_FOUND);
        return REDIRECT_USERS;
    }

    @PatchMapping ("/{id}")
    public String editUser (@PathVariable Long id, User user){
        if (user.getId().equals(id)) {
            log.info("...updating user "+id);
            User prvUser = userRepository.findById(id).orElseThrow();
            user.setAccessLevel(prvUser.getAccessLevel());
            user = userService.ifNullableMergeOldValues(user, prvUser);
            user.setUpdateDate(LocalDateTime.now());
            userRepository.save(user);
        }
        else log.error(USER + id + " " + NOT_FOUND);
        return REDIRECT_USERS+"/"+id;
    }
}