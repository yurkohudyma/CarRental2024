package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @GetMapping({"", "/sortById"})
    public String getAll(Model model) {
        model.addAttribute(ORDER_LIST,
                orderService.getAllOrders());
        assignAttributesWhenSortingFields(model);
        return ORDERS;
    }

    @GetMapping("/sortByName")
    public String getAllSortName(Model model) {
        model.addAttribute(ORDER_LIST,
                orderService.getAllOrdersSortedByFieldAsc("user.name"));
        assignAttributesWhenSortingFields(model);
        return ORDERS;
    }

    @GetMapping("/sortByModel")
    public String getAllSortByModel(Model model) {
        model.addAttribute(ORDER_LIST,
                orderService.getAllOrdersSortedByFieldAsc("car.model"));
        assignAttributesWhenSortingFields(model);
        return ORDERS;
    }

    @GetMapping("/sortByDateBegin")
    public String getAllSortByDateBegin(Model model) {
        model.addAttribute(ORDER_LIST,
                orderService.getAllOrdersSortedByFieldAsc("dateBegin"));
        assignAttributesWhenSortingFields(model);
        return ORDERS;
    }

    @GetMapping("/sortByDateEnd")
    public String getAllSortByDateEnd(Model model) {
        model.addAttribute(ORDER_LIST,
                orderService.getAllOrdersSortedByFieldAsc("dateEnd"));
        assignAttributesWhenSortingFields(model);
        return ORDERS;
    }

    @GetMapping("/sortByDurability")
    public String getAllSortByDurability(Model model) {
        model.addAttribute(ORDER_LIST,
                orderService.getAllOrdersSortedByFieldAsc("durability"));
        assignAttributesWhenSortingFields(model);
        return ORDERS;
    }

    @GetMapping("/sortByAmount")
    public String getAllSortByAmount(Model model) {
        model.addAttribute(ORDER_LIST,
                orderService.getAllOrdersSortedByFieldAsc("amount"));
        assignAttributesWhenSortingFields(model);
        return ORDERS;
    }

    private void assignAttributesWhenSortingFields(Model model) {
        model.addAttribute(USER_LIST, userRepository.findAll());
        model.addAttribute(CAR_LIST, carRepository.findAll());
        model.addAttribute(CURRENT_DATE, LocalDate.now());
        model.addAttribute(CURRENT_NEXT_DATE, LocalDate.now().plusDays(1));
    }

    @PostMapping
    public String addOrder(Order order,
                           @ModelAttribute("user_id") String userIdStr,
                           @ModelAttribute("car_id") String carIdStr, Model model) {
        Long userId = Long.parseLong(userIdStr), carId = Long.parseLong(carIdStr);
        if (orderService.setOrder(order, carId, userId)) {
            if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
            log.info ("...add Order: persisting order of {}", order.getUser().getName());
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
        model.addAttribute(ERROR_DATES_ASSIGN, true);
        model.addAttribute(ORDER_LIST, orderRepository.findAll());
        model.addAttribute(USER_LIST, userRepository.findAll());
        model.addAttribute(CAR_LIST, carRepository.findAll());
        model.addAttribute(CURRENT_DATE, LocalDate.now());
        model.addAttribute(CURRENT_NEXT_DATE, LocalDate.now().plusDays(1));
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
                log.info("...updating order = " + updatedOrder);
                updatedOrder.setRegisterDate(prevOrder.getRegisterDate());
                updatedOrder.setUpdateDate(LocalDateTime.now());
                orderRepository.save(updatedOrder);
            }
        } else log.info("id does not correspond to order id");
        return REDIRECT_ORDERS;
    }

    private void setModelAttributesWhenOrderFails(Model model, Order prevOrder) {
        assignAttributesWhenSetNewOrderFails(model);
        model.addAttribute(ORDER, prevOrder);
        model.addAttribute(CAR_LIST, carRepository.findAll());
        model.addAttribute(ACTION, ORDER);
        model.addAttribute(CURRENT_DATE, LocalDate.now());
        model.addAttribute(CURRENT_NEXT_DATE, LocalDate.now().plusDays(1));
    }
}


