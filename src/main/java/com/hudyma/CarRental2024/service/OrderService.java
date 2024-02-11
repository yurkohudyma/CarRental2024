package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.constants.OrderStatus;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public Double getAllOrdersAmount() {
        Double result = orderRepository
                .findAll()
                .stream()
                .map(Order::getAmount)
                .reduce(Double::sum)
                .orElse(0d);
        return Math.round(result * 100d) / 100d;
    }

    public List<Order> getOrdersByUserId(Long id) {
        return orderRepository.findAllByUserId(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getAllOrdersSortedByFieldAsc(String sortField) {
        return orderRepository.findAll(Sort.by(
                Sort.Direction.ASC, sortField));
    }

    public List<Order> getAllOrdersSortedByFieldDesc(String sortField) {
        return orderRepository.findAll(Sort.by(
                Sort.Direction.DESC, sortField));
    }


    public List<Order> getOrdersByCarId(Long id) {
        return orderRepository.findAllByCarId(id);
    }

    public boolean setOrder(Order order, Long carId, Long userId) {
        Double setOrderAmount = calculateOrderAmount(order, carId);
        if (setOrderAmount == 0d) {
            log.error("...set order: computed amount is {}", setOrderAmount);
            return false;
        }
        order.setAmount(setOrderAmount);
        if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
        if (order.getStatus() == null) order.setStatus(OrderStatus.REQUESTED);
        Car car = carRepository.findById(carId).orElseThrow();
        log.info("...setting car " + carId + " to order " + order.getId());
        order.setCar(car);
        User user = userRepository.findById(userId).orElseThrow();
        order.setUser(user);
        log.info("...adding order to user " + userId);
        if (!user.getOrderList().contains(order)) {
            user.addOrder(order);
        } else {
            user.updateOrder(order);
        }
        return true;
    }

    private Double calculateOrderAmount(Order order, Long carId) {
        long days = ChronoUnit.DAYS.between(
                order.getDateBegin(),
                order.getDateEnd());
        if (days <= 0) {
            log.info("...DATES OF RENTAL DIFFER BY " + days + " days");
            return 0d;
        }
        order.setDuration(days);
        Double price = carRepository.findById(carId)
                .orElseThrow()
                .getPrice();
        return Math.round(days * price * 100d) / 100d;
    }

    public void recalculateOrdersAmountUponCarEdit(Long carId) {
        List<Order> orderListByCarId = orderRepository.findAllByCarId(carId);
        orderListByCarId.forEach(order -> {
            order.setAmount(calculateOrderAmount(
                    order, order.getCar().getId()));
            orderRepository.save(order);
        });
    }
}
