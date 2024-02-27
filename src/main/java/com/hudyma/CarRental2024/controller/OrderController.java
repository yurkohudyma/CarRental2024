package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.exception.CarNotAvailableException;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.CarService;
import com.hudyma.CarRental2024.service.OrderService;
import com.hudyma.CarRental2024.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hudyma.CarRental2024.controller.UserController.USER_ORDERS_LIST;

@Log4j2
@RequestMapping("/orders")
@RequiredArgsConstructor
@Controller
public class OrderController {

    private static final String REDIRECT_ORDERS = "redirect:/orders";
    private static final String ORDERS = "orders";
    static final String ORDER_LIST = "orderList";
    public static final String ERROR_DATES_ASSIGN = "errorDatesAssign", USER_LIST = "userList", CAR_LIST = "carList";
    public static final String CURRENT_DATE = "currentDate", CURRENT_NEXT_DATE = "currentNextDate", ORDER = "order";
    public static final String ACTION = "action", REDIRECT_USER_ACCOUNT_ORDERS = "redirect:/users/account/";
    private static final String USER_BLOCKED_ERROR = "blockedUserError", CAR_NOT_AVAIL = "carNotAvailError";
    private final OrderRepository orderRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final CarService carService;
    private final UserService userService;

    @GetMapping({"", "/sortById"})
    public String getAll(Model model) {
        List<Order> orderList = orderService.getAllOrders();
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    private void prepareDataForSortingDisplay(Model model, List<Order> orderList) {
        model.addAttribute(ORDER_LIST, orderList);
        assignAttributesWhenSortingFields(model);
        assignAttributesForStats(model, orderList);
    }

    private void assignAttributesWhenSortingFields(Model model) {
        model.addAllAttributes(Map.of(
                USER_LIST, userService.getAllUsersSortedByFieldAsc(),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc(),
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1)));
    }

    private void assignAttributesForStats(Model model, List<Order> orderList) {
        model.addAllAttributes(
                Map.of("ordersQuantity", orderList.size(),
                        "ordersAmount", orderService.getAllOrdersAmount(),
                        "carsQuantity", carService.getAllCarsQuantity(),
                        "usersQuantity", userService.getAllUsersQuantity()));
    }


    /*@PostMapping
    public String addOrder(Order order,
                           @ModelAttribute("user_id") Long userId,
                           @ModelAttribute("car_id") Long carId, Model model) {
        if (checkUserAccessRestriction(userId)) {
            assignAttribIfNewOrderFails(model);
            model.addAttribute(USER_BLOCKED_ERROR, true);
            log.error("... addOrder: user {} is BLOCKED", userId);
            return ORDERS;
        } else if (!checkCarAvailability(carId)) {
            assignAttribIfNewOrderFails(model);
            model.addAttribute(CAR_NOT_AVAIL, true);
            log.error("... addOrder: car {} not avail", carId);
            return ORDERS;
        } else if (orderService.setOrder(order, carId, userId)) {
            if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
            log.info("...add Order: persisting order of {}", order.getUser().getName());
            order.setRegisterDate(LocalDateTime.now());
            orderRepository.save(order);
            return REDIRECT_ORDERS;
        } else {
            assignAttribIfNewOrderFails(model);
            model.addAttribute(ERROR_DATES_ASSIGN, true);
            log.error("... addOrder: dates assignation error");
            return ORDERS;
        }
    }*/

    @PostMapping("/{id}")
    public String addOrder(Order order, Model model,
                                      @PathVariable("id") Long userId,
                                      @ModelAttribute("car_id") Long carId,
                                      @ModelAttribute("payment") Integer paymentId) {
        order.setId(null); //todo orderId is somehow assigned to 1, if not nulled - overrites existing order
        log.info("...proceeding order of user {}: ", userId);
        Boolean auxNeeded = order.getAuxNeeded();
        log.info("...order auxNeeded is {}", auxNeeded);
        if (auxNeeded == null) {
            order.setAuxNeeded(false);
            auxNeeded = false;
            log.info("...auxNeeded set to {}", false);
        }
        /*if (userService.checkUserAccessRestriction(userId)) {
            assignAttribIfNewOrderFails(model);
            model.addAttribute(USER_BLOCKED_ERROR, true);
            log.error("... addOrder: user {} is BLOCKED", userId);
            return REDIRECT_USER_ACCOUNT_ORDERS + userId;
        } else if (!checkCarAvailability(carId)) {
            assignAttribIfNewOrderFails(model);
            model.addAttribute(CAR_NOT_AVAIL, true);
            log.error("... addOrder: car {} not avail", carId);
        } else*/

        if (!orderService.calculateOrderPayment(order, carId, userId, paymentId, auxNeeded)) {
            return REDIRECT_USER_ACCOUNT_ORDERS + userId + "/lowBalanceError";
        } else if (orderService.setOrder(order, carId, userId)) {
            order.setRegisterDate(LocalDateTime.now());
            log.info("...user has chosen {} % payment", paymentId);
            orderRepository.save(order);
            log.info("...add Order: persisting order of {}",
                    order.getUser().getName());
        } else {
            assignAttribIfNewOrderFailsUserAccOrder(model, userId);
            log.error("... addOrder: dates assignation error");
            return REDIRECT_USER_ACCOUNT_ORDERS + userId + "/dateError";
        }
        return REDIRECT_USER_ACCOUNT_ORDERS + userId;
    }

    @PostMapping("/checkout/{id}")
    public String getOrderCheckout(Order order, Model model,
                                   @PathVariable("id") Long userId,
                                   @ModelAttribute("car_id") Long carId,
                                   @ModelAttribute("payment") Integer paymentId,
                                   HttpServletRequest req
                                   //@ModelAttribute("auxNeeded") Boolean auxNeeded) {
    ) {
        Boolean auxNeeded = order.getAuxNeeded();
        if (auxNeeded == null) auxNeeded = false;
        log.info("...getOrderCheckout:: orderService : auxNeeded is {}", auxNeeded);
        if (orderService.estimateOrderPayment(order, paymentId, auxNeeded, carId, req)) {
            assignModelAttributesCheckout(model, userId);
            log.info("...order checkout for user {}", userId);
            return REDIRECT_USER_ACCOUNT_ORDERS + userId + "/checkout";
        }
        assignAttribIfNewOrderFails(model);
        model.addAttribute(ERROR_DATES_ASSIGN, true);
        log.error("... addOrder: dates assignation error");
        return REDIRECT_USER_ACCOUNT_ORDERS + userId + "/dateError";
    }

    @PostMapping ("/saveCheckoutOrder/{id}")
    public String saveOrderCheckout (@PathVariable("id") Long userId){

        //todo implement

        return REDIRECT_USER_ACCOUNT_ORDERS + userId;
    }

    private void assignModelAttributesCheckout(Model model, Long userId) {
        model.addAllAttributes(Map.of(
                USER_ORDERS_LIST, orderService.getOrdersByUserId(userId),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc(),
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1)
        ));
    }

    private void assignAttribIfNewOrderFailsUserAccOrder(Model model, Long userId) {
        model.addAllAttributes(Map.of(
                USER_ORDERS_LIST, orderService.getOrdersByUserId(userId),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc()));
        assignCommonModelAttribWhenOrderFails(model);
    }

    private void assignAttribIfNewOrderFails(Model model) {
        model.addAllAttributes(Map.of(
                ORDER_LIST, orderRepository.findAll(),
                USER_LIST, userRepository.findAll(),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc()));
        assignCommonModelAttribWhenOrderFails(model);
    }

    private void assignCommonModelAttribWhenOrderFails(Model model) {
        model.addAttribute(CURRENT_DATE, LocalDate.now());
        model.addAttribute(CURRENT_NEXT_DATE,
                LocalDate.now().plusDays(1));
    }

    public boolean checkCarAvailability(Long carId) {
        Car car = carRepository.findById(carId).orElseThrow();
        if (car.getAvailable() == 0) {
            log.error("... car {} is not available", carId);
            throw new CarNotAvailableException("car " + carId + " is not available");
        }
        return true;
        //todo this method is redundant second-stage check and intended for finding bugs
        // todo if code allows user to book unavailable car
    }


    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            orderRepository.deleteById(orderId);
            log.info("... order {} successfully deleted", orderId);
            incrementCarAvailability(order.get());
            refundAllPaymentsToUser(order.get().getUser().getId(), order.get());
        } else log.info("..... Order " + orderId + " does not EXIST");
        return REDIRECT_ORDERS;
    }

    @DeleteMapping("/{userId}/cancel/{orderId}")
    public String cancelOrder(
            @PathVariable(name = "userId") Long userId,
            @PathVariable(name = "orderId") Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            orderRepository.deleteById(orderId);
            log.info("... order {} successfully deleted", orderId);
            incrementCarAvailability(order.get());
            refundAllPaymentsToUser(userId, order.get());
        } else log.info("..... Order " + orderId + " does not EXIST");
        return REDIRECT_USER_ACCOUNT_ORDERS + userId;
    }

    private void refundAllPaymentsToUser(Long userId, Order order) {
        User user = userRepository.findById(userId).orElseThrow();
        Double deposit = order.getDeposit();
        Double rentalPayment = order.getRentalPayment();
        Double auxPayment = order.getAuxPayment() == null ? 0d : order.getAuxPayment();
        Double totalRefundPayment = orderService.doubleRound(
                user.getBalance() + deposit + rentalPayment + auxPayment);
        user.setBalance(totalRefundPayment);
        log.info("... deposit {}, rental {} and aux refunded for user {}",
                deposit,
                rentalPayment,
                auxPayment);
        userRepository.save(user);
    }

    private void incrementCarAvailability(Order order) {
        Long carId = order.getCar().getId();
        carRepository.incrementCarAvailableWhenOrderComplete(carId);
        log.info("... car {} availability incremented", carId);
    }

    @DeleteMapping
    public String deleteAll() {
        orderRepository
                .findAll()
                .forEach(orderRepository::delete);
        return REDIRECT_ORDERS;
    }

    @PatchMapping("/{id}")
    public String editOrder(@PathVariable Long id,
                            Order updatedOrder,
                            @ModelAttribute("car_id") Long carId, Model model) {
        String edit = editOrderImpl(id, updatedOrder, carId, model);
        if (edit != null) return edit;
        return REDIRECT_ORDERS;
    }

    private String editOrderImpl(Long id, Order updatedOrder, Long carId, Model model) {
        if (updatedOrder.getId().equals(id)) {
            Order prevOrder = orderRepository.findById(id).orElseThrow();
            boolean setOrderSuccess = orderService.setOrder(updatedOrder, carId, prevOrder.getUser().getId());
            if (!setOrderSuccess) {
                setModelAttributesWhenOrderFails(model, prevOrder);
                log.error("... editOrder: dates assignation error");
                return "edit";
            } else {
                updatedOrder.setRegisterDate(prevOrder.getRegisterDate());
                updatedOrder.setUpdateDate(LocalDateTime.now());
                log.info("...updating order = " + updatedOrder);
                orderRepository.save(updatedOrder);
            }
        } else log.info("id does not correspond to order id");
        return null;
    }

    private void setModelAttributesWhenOrderFails(Model model, Order prevOrder) {
        assignAttribIfNewOrderFails(model);
        model.addAllAttributes(Map.of(
                ORDER, prevOrder,
                CAR_LIST, carRepository.findAll(),
                ACTION, ORDER,
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1)));
    }


    //sorting block
    @GetMapping("/sortByName")
    public String getAllSortName(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("user.name");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByModel")
    public String getAllSortByModel(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("car.model");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByDateBegin")
    public String getAllSortByDateBegin(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("dateBegin");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByDateEnd")
    public String getAllSortByDateEnd(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("dateEnd");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByDuration")
    public String getAllSortByDuration(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("duration");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByAmount")
    public String getAllSortByAmount(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("amount");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByRegister")
    public String getAllSortByRegister(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("registerDate");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByUpdate")
    public String getAllSortByUpdate(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("updateDate");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByPayment")
    public String getAllSortByPayment(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("payment");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByDeposit")
    public String getAllSortByDeposit(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("deposit");
        prepareDataForSortingDisplay(model, orderList);
        return ORDERS;
    }

}


