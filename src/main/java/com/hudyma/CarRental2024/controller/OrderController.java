package com.hudyma.CarRental2024.controller;

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

@Log4j2
@RequestMapping("/orders")
@RequiredArgsConstructor
@Controller
public class OrderController {

    private static final String REDIRECT_ORDERS = "redirect:/orders", ORDERS = "orders", ORDER_LIST = "orderList";
    public static final String ERROR_DATES_ASSIGN = "errorDatesAssign", USER_LIST = "userList", CAR_LIST = "carList";
    public static final String CURRENT_DATE = "currentDate", CURRENT_NEXT_DATE = "currentNextDate", ORDER = "order";
    public static final String ACTION = "action";
    private final OrderRepository orderRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final CarService carService;
    private final UserService userService;

    @GetMapping({"", "/sortById"})
    public String getAll(Model model) {
        List<Order> orderList = orderService.getAllOrders();
        model.addAttribute(ORDER_LIST,
                orderList);
        assignAttributesWhenSortingFields(model);
        assignAttributesForStats(model, orderList);
        return ORDERS;
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
        model.addAttribute(ORDER_LIST, orderList);
        assignAttributesWhenSortingFields(model);
        assignAttributesForStats(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByModel")
    public String getAllSortByModel(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("car.model");
        model.addAttribute(ORDER_LIST, orderList);
        assignAttributesWhenSortingFields(model);
        assignAttributesForStats(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByDateBegin")
    public String getAllSortByDateBegin(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("dateBegin");
        model.addAttribute(ORDER_LIST, orderList);
        assignAttributesWhenSortingFields(model);
        assignAttributesForStats(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByDateEnd")
    public String getAllSortByDateEnd(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("dateEnd");
        model.addAttribute(ORDER_LIST, orderList);
        assignAttributesWhenSortingFields(model);
        assignAttributesForStats(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByDuration")
    public String getAllSortByDuration(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("duration");
        model.addAttribute(ORDER_LIST, orderList);
        assignAttributesWhenSortingFields(model);
        assignAttributesForStats(model, orderList);
        return ORDERS;
    }

    @GetMapping("/sortByAmount")
    public String getAllSortByAmount(Model model) {
        List<Order> orderList = orderService.getAllOrdersSortedByFieldAsc("amount");
        model.addAttribute(ORDER_LIST, orderList);
        assignAttributesWhenSortingFields(model);
        assignAttributesForStats(model, orderList);
        return ORDERS;
    }

    private void assignAttributesWhenSortingFields(Model model) {
        model.addAllAttributes(Map.of(
                USER_LIST, userService.getAllUsersSortedByFieldAsc("name"),
                CAR_LIST, carService.getAllCarsSortedByFieldAsc("model"),
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1)));
    }

    @PostMapping
    public String addOrder(Order order,
                           @ModelAttribute("user_id") String userIdStr,
                           @ModelAttribute("car_id") String carIdStr, Model model) {
        Long userId = Long.parseLong(userIdStr), carId = Long.parseLong(carIdStr);
        if (orderService.setOrder(order, carId, userId)) {
            if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
            log.info("...add Order: persisting order of {}", order.getUser().getName());
            order.setRegisterDate(LocalDateTime.now());
            orderRepository.save(order);
            return REDIRECT_ORDERS;
        } else {
            assignAttributesWhenSetNewOrderFails(model);
            log.error("... addOrder: dates assignation error");
            return ORDERS;
        }
    }

    private void assignAttributesWhenSetNewOrderFails(Model model) {
        model.addAllAttributes(Map.of(
                ERROR_DATES_ASSIGN, true,
                ORDER_LIST, orderRepository.findAll(),
                USER_LIST, userRepository.findAll(),
                CAR_LIST, carRepository.findAll(),
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
                            @ModelAttribute("car_id") String carIdStr, Model model) {
        if (updatedOrder.getId().equals(id)) {
            long carId = Long.parseLong(carIdStr);
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
        return REDIRECT_ORDERS;
    }

    private void setModelAttributesWhenOrderFails(Model model, Order prevOrder) {
        assignAttributesWhenSetNewOrderFails(model);
        model.addAllAttributes(Map.of(
                ORDER, prevOrder,
                CAR_LIST, carRepository.findAll(),
                ACTION, ORDER,
                CURRENT_DATE, LocalDate.now(),
                CURRENT_NEXT_DATE, LocalDate.now().plusDays(1)));
    }
}


