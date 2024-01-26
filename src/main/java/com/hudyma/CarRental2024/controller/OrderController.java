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

import java.time.LocalDate;
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
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("orderList", orderRepository.findAll());
        model.addAttribute("userList", userRepository.findAll());
        model.addAttribute("carList", carRepository.findAll());
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("currentNextDate", LocalDate.now().plusDays(1));
        return "orders";
    }

    @PostMapping
    public String addOrder(Order order,
                           @ModelAttribute("user_id") String userIdStr,
                           @ModelAttribute("car_id") String carIdStr) {
        Long userId = Long.parseLong(userIdStr), carId = Long.parseLong(carIdStr);
        orderService.setOrder(order, carId, userId);
        if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
        orderRepository.save(order);
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

    @PatchMapping("/{id}")
    public String editOrder(@PathVariable Long id,
                            Order updatedOrder,
                            @ModelAttribute("car_id") String carIdStr) {
        if (updatedOrder.getId().equals(id)) {
            log.info("...updating order = " + updatedOrder);
            long carId = Long.parseLong(carIdStr);
            Order prevOrder = orderRepository.findById(id).orElseThrow();
            orderService.setOrder(updatedOrder, carId, prevOrder.getUser().getId());
            orderRepository.save(updatedOrder);
        } else log.info("id does not correspond to order id");
        return REDIRECT_ORDERS;
    }
}


