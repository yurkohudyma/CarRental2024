package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.constants.UserAccessLevel;
import com.hudyma.CarRental2024.exception.UserNotFoundException;
import com.hudyma.CarRental2024.model.Transaction;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.TransactionRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.CarService;
import com.hudyma.CarRental2024.service.OrderService;
import com.hudyma.CarRental2024.service.TransactionService;
import com.hudyma.CarRental2024.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hudyma.CarRental2024.controller.OrderController.*;

@Log4j2
@RequestMapping("/users")
@RequiredArgsConstructor
@Controller
public class UserController {

    public static final String BLOCKING_USER = "...Blocking user = ", USER = "user", NOT_FOUND = "not found";
    public static final String USER_LIST = "userList", USER_ORDERS_LIST = "userOrdersList", SOLE_USER_CARD = "soleUserCard";
    public static final String REDIRECT_USERS = "redirect:/users", CAR_LIST = "carList", LOW_BALANCE_ERROR = "lowBalanceError";
    public static final String CURRENT_DATE = "currentDate", CURRENT_NEXT_DATE = "currentNextDate", ORDER = "order";
    public static final String USER_BLOCKED_ERROR = "blockedUserError", ERROR_DATES_ASSIGN = "errorDatesAssign";
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final UserService userService;
    private final CarService carService;
    private final TransactionService transactionService;

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute(USER_LIST, userRepository.findAll());
        model.addAttribute(SOLE_USER_CARD, false);
        model.addAttribute(USER_ORDERS_LIST, new ArrayList<>());
        assignAttributesForStats(model);
        log.info("...Retrieving All users...");
        return "users";
    }

    private void assignAttributesForStats(Model model) {
        model.addAllAttributes(
                Map.of("ordersQuantity", orderService.getAllOrders().size(),
                        "ordersAmount", orderService.getAllOrdersRentalPayments(),
                        "carsQuantity", carService.getAllCarsQuantity(),
                        "usersQuantity", userService.getAllUsersQuantity()));
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

    @GetMapping("/account/{userId}")
    public String getUser(@PathVariable Long userId, Model model) {
        model.addAttribute(USER, userRepository
                .findById(userId).orElseThrow(
                        UserNotFoundException::new));
        orderService.handleNonPaidExpiredOrders(userId);
        orderService.handleDelayedCarReturnOrders (userId);
        assignModelAttributes(model, userId);
        model.addAttribute("userQty",
                userRepository.findAll().size());
        model.addAttribute("id", userId + 1);
        return USER;
    }

    @GetMapping("/account/{userId}/dateError")
    public String getUserDateError(@PathVariable Long userId, Model model) {
        model.addAttribute(USER, userRepository
                .findById(userId).orElseThrow(UserNotFoundException::new));
        assignModelAttributes(model, userId);
        model.addAttribute(ERROR_DATES_ASSIGN, true);
        return USER;
    }

    @GetMapping ("/account/{userId}/no-car")
    public String getUserNoCar (@PathVariable Long userId, Model model){
        model.addAttribute(USER, userRepository
                .findById(userId).orElseThrow(UserNotFoundException::new));
        assignModelAttributes(model, userId);
        model.addAttribute("carNotAvailError", true);
        return USER;
    }


    @GetMapping("/account/{id}/checkout")
    public String getUserOrderCheckout(@PathVariable("id") Long userId, Model model,
                                       HttpServletRequest req) {
        assignModelAttributesCheckout(model, userId, req);
        return USER;
    }

    @GetMapping("/account/{id}/lowBalanceError")
    public String getUserLowBalanceError(@PathVariable ("id") Long userId, Model model, HttpServletRequest req) {
        assignModelAttributes(model, userId);
        Double insufficient = (Double) req.getSession().getAttribute("insufficient");
        model.addAttribute(LOW_BALANCE_ERROR, true);
        model.addAttribute("insufficient", insufficient);
        return USER;
    }

    @GetMapping("/account/{id}/blockedUser")
    public String getUserBlockedUserError(@PathVariable("id") Long userId, Model model) {
        assignModelAttribWhenUserBlocked(model, userId);
        model.addAttribute("userQty",
                userRepository.findAll().size());
        model.addAttribute("id", userId + 1);
        return USER;
    }

    private void assignModelAttributes(Model model, Long userId) {
        model.addAllAttributes(Map.of(
                USER, userRepository
                        .findById(userId)
                        .orElseThrow(UserNotFoundException::new),
                USER_ORDERS_LIST, orderService.getOrdersByUserId(userId),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc(),
                CURRENT_DATE, LocalDate.now(),
                "min_order_date", LocalDate.now().plusDays(1),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(2),
                "tx_list", transactionRepository.findAllByUserId(userId)));
    }

    private void assignModelAttribWhenUserBlocked(Model model, Long userId) {
        model.addAllAttributes(Map.of(
                USER, userRepository
                        .findById(userId)
                        .orElseThrow(UserNotFoundException::new),
                USER_LIST, userRepository.findAll(),
                USER_BLOCKED_ERROR, true,
                USER_ORDERS_LIST, orderService.getOrdersByUserId(userId),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc(),
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1)));
    }

    private void assignModelAttributesCheckout(Model model, Long userId,
                                               HttpServletRequest req) {
        Double auxPayment = (Double) req.getSession().getAttribute("auxPayment");
        Double deposit = (Double) req.getSession().getAttribute("deposit");
        Double deductible = (Double) req.getSession().getAttribute("deductible");
        LocalDate dateBegin = (LocalDate) req.getSession().getAttribute("orderDateBegin");
        LocalDate dateEnd = (LocalDate) req.getSession().getAttribute("orderDateEnd");
        Boolean auxNeeded = (Boolean) req.getSession().getAttribute("auxNeeded");
        Long duration = (Long) req.getSession().getAttribute("duration");
        Double price = (Double) req.getSession().getAttribute("price");
        log.info("...retrieved param from Session = {}", auxNeeded);
        if (auxNeeded == null) {
            auxNeeded = false;
            log.info("...auxNeeded is null, setting to {}", auxNeeded);
        }
        //String carModel = (String) req.getSession().getAttribute("carModel");
        Long carId = (Long) req.getSession().getAttribute("carId");
        String carModel = carService.getModelByCarId(carId);
        Integer paymentId = (Integer) req.getSession().getAttribute("paymentId");
        Double totalCheckout = deposit + auxPayment + deductible;
        model.addAllAttributes(Map.of(
                USER, userRepository.findById(userId).orElseThrow(
                        UserNotFoundException::new),
                USER_ORDERS_LIST, orderService.getOrdersByUserId(userId),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc(),
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1),
                "checkout", true,
                "auxPayment", orderService.formatDecimalNum(auxPayment),
                "deductible", orderService.formatDecimalNum(deductible),
                "deposit", orderService.formatDecimalNum(deposit)));
        model.addAllAttributes(Map.of(
                "auxNeeded", auxNeeded,
                "orderDateBegin", dateBegin,
                "orderDateEnd", dateEnd,
                "carModel", carModel,
                "paymentId", paymentId,
                "duration", duration,
                "price", price,
                "totalCheckout", totalCheckout,
                "tx_list", transactionRepository.findAllByUserId(userId)));

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
        } else log.error(USER + " " + id + " " + NOT_FOUND);
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
        } else log.error(USER + " " + id + " " + NOT_FOUND);
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
        } else log.error(USER + " " + id + " " + NOT_FOUND);
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
        } else log.error(USER + " " + id + " " + NOT_FOUND);
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
        } else log.error(USER + " " + id + " " + NOT_FOUND);
        return REDIRECT_USERS + "/" + id;
    }

    @PatchMapping("/{userId}/top-up")
    public String topUpUserBalance(
            @PathVariable Long userId,
            @ModelAttribute("balance") Double balance, Transaction transaction) {
        User user = userRepository.findById(userId)
                .orElseThrow();
        if (userService.checkUserAccessRestriction(userId)) {
            log.error("... topUpUserBalance failed, user {} is BLOCKED", userId);
            return REDIRECT_USER_ACCOUNT_ORDERS + userId + "/blockedUser";
        }
        Double prevBalance = user.getBalance();
        user.setBalance(Math.round((balance + prevBalance) * 100d) / 100d);
        user.setUpdateDate(LocalDateTime.now());
        log.info("...topping up user {} balance", userId);

        transaction = transactionService.addTransaction(transaction, "top-up", user, balance);
        user.addTransaction(transaction);

        userRepository.save(user);
        return REDIRECT_USER_ACCOUNT_ORDERS + userId;
    }


}