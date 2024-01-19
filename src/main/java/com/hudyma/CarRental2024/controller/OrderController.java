package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
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

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("orderList",orderRepository.findAll());
        return "orders";
    }

    @PostMapping
    public String addOrder(Order order) {
        order.setAmount(calculateOrderAmount(order));
        log.info("...getting user by order.user id");
        User user = userRepository.findById(order.getUser()
                        .getId())
                .orElseThrow(
                        () -> new NoSuchElementException
                                (".............User ID is not available in REQ BODY"));
        log.info("...getting car by order.car id");
        Car car = carRepository.findById(order
                        .getCar()
                        .getId())
                .orElseThrow(
                        () -> new NoSuchElementException
                                (".............Car ID is not available in REQ BODY"));
        order.setCar(car);
        user.addOrder(order);
        orderRepository.save(order);
        return REDIRECT_ORDERS;
    }

    private Double calculateOrderAmount(Order order) {
        long days = ChronoUnit.DAYS.between(
                order.getDateBegin(),
                order.getDateEnd());
        if (days <= 0) throw new IllegalArgumentException
                (".........DATES OF RENTAL INCORRECT");
        order.setDurability(days);
        Double price = carRepository.findById(order.getCar()
                        .getId())
                .orElseThrow()
                .getPrice();
        return days * price;
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

    /*@PostMapping
    public String addOrders (@RequestBody Order[] orders){
        Arrays.stream(orders)
                .forEach(this::addOrder);
        return REDIRECT_ORDERS;
    }*/

    /*@GetMapping("/{id}")
    public Optional<Order> getById(@PathVariable("id") Long id) {
        return orderRepository.findById(id);
    }*/
}


