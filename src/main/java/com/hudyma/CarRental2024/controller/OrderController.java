package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.CarService;
import com.hudyma.CarRental2024.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Optional;

@Log4j2
@RequestMapping("/orders")
@RequiredArgsConstructor
@Controller
public class OrderController {

    private static final String REDIRECT_ORDERS = "redirect:/orders";
    private final OrderRepository orderRepository;
    @Autowired
    private final CarRepository carRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final OrderService orderService;

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("orderList", orderRepository.findAll());
        model.addAttribute("userList", userRepository.findAll());
        model.addAttribute("carList", carRepository.findAll());
        return "orders";
    }

    @PostMapping
    public String addOrder(Order order,
                           @ModelAttribute("user_id") String userIdStr,
                           @ModelAttribute("car_id")  String carIdStr) {
        Long userId = Long.parseLong(userIdStr), carId = Long.parseLong(carIdStr);
        log.info("...Submitting order data = " + order);
        log.info("...with userId = " + userId + ", carId = " + carId);
        orderService.setOrder(order, carId, userId);
        log.info("...persisting order");
        if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
        orderRepository.save(order);
        log.info("... = "+order);
        return REDIRECT_ORDERS;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        //todo deletes users only without orders
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
}


