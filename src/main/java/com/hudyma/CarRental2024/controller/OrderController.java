package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.exception.CarNotAvailableException;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.CarService;
import com.hudyma.CarRental2024.service.OrderService;
import com.hudyma.CarRental2024.service.UserService;
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

    private static final String REDIRECT_ORDERS = "redirect:/orders", ORDERS = "orders", ORDER_LIST = "orderList";
    public static final String ERROR_DATES_ASSIGN = "errorDatesAssign", USER_LIST = "userList", CAR_LIST = "carList";
    public static final String CURRENT_DATE = "currentDate", CURRENT_NEXT_DATE = "currentNextDate", ORDER = "order";
    public static final String ACTION = "action", REDIRECT_USER_ACCOUNT_ORDERS = "redirect:/users/account/";
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

    private void prepareDataForSortingDisplay(Model model, List<Order> orderList) {
        model.addAttribute(ORDER_LIST, orderList);
        assignAttributesWhenSortingFields(model);
        assignAttributesForStats(model, orderList);
    }

    @PostMapping
    public String addOrder(Order order,
                           @ModelAttribute("user_id") Long userId,
                           @ModelAttribute("car_id") Long carId, Model model) {
        Car car = carRepository.findById(carId).orElseThrow();
        if (car.getAvailable() == 0) {
            log.error("... car {} is not available", carId);
            throw new CarNotAvailableException("car is not available");
        }
        if (orderService.setOrder(order, carId, userId)) {
            if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
            log.info("...add Order: persisting order of {}", order.getUser().getName());
            order.setRegisterDate(LocalDateTime.now());
            orderRepository.save(order);
            return REDIRECT_ORDERS;
        } else {
            assignAttribIfNewOrderFails(model);
            log.error("... addOrder: dates assignation error");
            return ORDERS;
        }
    }

    @PostMapping("/user-acc/{id}")
    //todo IMPLEMENT
    public String addOrderUserAccount(Order order, Model model,
                                      @PathVariable ("id") Long userId,
                                      @ModelAttribute("car_id") Long carId,
                                      @ModelAttribute("payment") Integer paymentId) {
        Car car = carRepository.findById(carId).orElseThrow();
        order.setId(null); //todo orderId is somehow assigned to 1, if not nulled - overrites existing order
        log.info("...adding New User Account Order {}: ", order);
        if (car.getAvailable() == 0) {
            log.error("... car {} is not available", carId);
            throw new CarNotAvailableException("car is not available");
        }
        if (orderService.setOrder(order, carId, userId)) {
            if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
            log.info("...add Order: persisting order of {}",
                    order.getUser().getName());
            order.setRegisterDate(LocalDateTime.now());
            //todo introduce New Column in Orders for effected payments
            //todo if paid 30% - set Status approved
            //todo if paid 100% - give discount 10%
            log.info("...user has chosen {} % payment, with auxNeeded {}",
                    paymentId, order.getAuxNeeded());
            orderRepository.save(order);
        } else {
            assignAttribIfNewOrderFailsUserAccOrder(model, userId);
            log.error("... addOrder: dates assignation error");
            return REDIRECT_USER_ACCOUNT_ORDERS + userId + "/dateError";
        }
        return REDIRECT_USER_ACCOUNT_ORDERS + userId;
    }

    private void assignAttribIfNewOrderFailsUserAccOrder(Model model, Long id) {
        model.addAllAttributes(Map.of(
                ERROR_DATES_ASSIGN, true,
                USER_ORDERS_LIST,orderService.getOrdersByUserId(id),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc(),
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1)));
    }

    private void assignAttribIfNewOrderFails(Model model) {
        model.addAllAttributes(Map.of(
                ERROR_DATES_ASSIGN, true,
                ORDER_LIST, orderRepository.findAll(),
                USER_LIST, userRepository.findAll(),
                CAR_LIST, carService.getAllAvailableCarsSortedByFieldAsc(),
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1)));
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            orderRepository.deleteById(id);
        } else log.info("..... Order " + id + " does not EXIST");
        return REDIRECT_ORDERS;
    }

    @DeleteMapping("/{orderId}/user-acc/{userId}")
    public String userAccDelete(
            @PathVariable(name = "orderId") Long orderId,
            @PathVariable(name = "userId") Long userId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            orderRepository.deleteById(orderId);
            log.info("... order {} successfully deleted", orderId);
        } else log.info("..... Order " + orderId + " does not EXIST");
        return REDIRECT_USER_ACCOUNT_ORDERS + userId;
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
}


