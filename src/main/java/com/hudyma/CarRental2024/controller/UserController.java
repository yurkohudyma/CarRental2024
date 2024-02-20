package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.exception.UserNotFoundException;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.CarService;
import com.hudyma.CarRental2024.service.OrderService;
import com.hudyma.CarRental2024.constants.UserAccessLevel;
import com.hudyma.CarRental2024.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.hudyma.CarRental2024.controller.OrderController.ERROR_DATES_ASSIGN;

@Log4j2
@RequestMapping("/users")
@RequiredArgsConstructor
@Controller
public class UserController {

    public static final String BLOCKING_USER = "...Blocking user = ", USER = "...User ", NOT_FOUND = "not found";
    public static final String USER_LIST = "userList", USER_ORDERS_LIST = "userOrdersList", SOLE_USER_CARD = "soleUserCard";
    private static final String REDIRECT_USERS = "redirect:/users", CAR_LIST = "carList";
    public static final String CURRENT_DATE = "currentDate", CURRENT_NEXT_DATE = "currentNextDate", ORDER = "order";
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final UserService userService;
    private final CarService carService;

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
        log.info("...Retrieving user {} ", id);
        return "users";
    }

    @GetMapping("/account/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userRepository
                .findById(id).orElseThrow(UserNotFoundException::new));
        assignModelAttributes(model, id);
        return "user";
    }

    @GetMapping("/account/{id}/dateError")
    public String getUserDateError(@PathVariable Long id, Model model) {
        model.addAttribute("user", userRepository
                .findById(id).orElseThrow(UserNotFoundException::new));
        assignModelAttributes(model, id);
        model.addAttribute(ERROR_DATES_ASSIGN, true);
        return "user";
    }

    private void assignModelAttributes(Model model, Long id) {
        model.addAllAttributes(Map.of(
                USER_ORDERS_LIST,orderService.getOrdersByUserId(id),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc(),
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1)));
    }

    @PostMapping
    public String addUser(User user) {
        userRepository.save(user);
        user.setRegisterDate(LocalDateTime.now());
        return REDIRECT_USERS;
    }

    @DeleteMapping("/{id}") //todo needs DB FK on ORDER => CASCADE instead of RESTRICT
    public String delete(@PathVariable Long id) {
        if (userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
            log.info("...deleting user {} with all orders", id);
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

    @PatchMapping("/{id}")
    public String editUser(@PathVariable Long id, User user) {
        if (user.getId().equals(id)) {
            log.info("...updating user " + id);
            User prvUser = userRepository.findById(id).orElseThrow();
            user.setAccessLevel(prvUser.getAccessLevel());
            user = userService.ifNullableMergeOldValues(user, prvUser);
            user.setUpdateDate(LocalDateTime.now());
            userRepository.save(user);
        } else log.error(USER + id + " " + NOT_FOUND);
        return REDIRECT_USERS + "/" + id;
    }

    @PatchMapping("/{id}/top-up")
    public String topUpUserBalance(
            @PathVariable Long id,
            @ModelAttribute("balance") Double balance) {
        User user = userRepository
                .findById(id)
                .orElseThrow();
        Double prevBalance = user.getBalance();
        user.setBalance(Math.round((balance + prevBalance) * 100d) / 100d);
        user.setUpdateDate(LocalDateTime.now());
        log.info("...topping up user {} balance", id);
        userRepository.save(user);
        return REDIRECT_USERS + "/account/" + id;
    }
}