package com.hudyma.CarJPA.controller;

import com.hudyma.CarJPA.model.Car;
import com.hudyma.CarJPA.model.Order;
import com.hudyma.CarJPA.model.User;
import com.hudyma.CarJPA.repository.CarRepository;
import com.hudyma.CarJPA.repository.OrderRepository;
import com.hudyma.CarJPA.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@Log4j2
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;

    @Autowired
    private final CarRepository carRepository;

    @Autowired
    private final UserRepository userRepository;

    @GetMapping
    public List<Order> getAll (){
        return orderRepository.findAll();
    }
    @GetMapping("{id}")
    public Optional<Order> getById (@PathVariable("id") Long id){
        return orderRepository.findById(id);
    }

    @PostMapping("/one")
    @ResponseStatus(HttpStatus.CREATED)
    public void addOrder (@RequestBody Order order){
        order.setAmount(calculateOrderAmount(order));
        User user = userRepository.findById(order.getUser()
                .getId())
                .orElseThrow(
                        () -> new NoSuchElementException
                                (".............User ID is not available in REQ BODY"));
        Car car = carRepository.findById(order
                .getCar()
                .getId())
                .orElseThrow(
                        () -> new NoSuchElementException
                                (".............Car ID is not available in REQ BODY"));
        order.setCar(car);
        user.addOrder(order);
        orderRepository.save(order);
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addOrders (@RequestBody Order[] orders){
        Arrays.stream(orders)
                .forEach(this::addOrder);
    }

    //todo deletes users only without orders
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete (@PathVariable Long id){
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            orderRepository.deleteById(id);
        }
        else log.info("..... Order "+ id +" does not EXIST");
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll (){
        orderRepository
                .findAll()
                .forEach(orderRepository::delete);
    }
}
