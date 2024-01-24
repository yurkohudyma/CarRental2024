package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

    public void setOrder(Order order, Long carId, Long userId) {
        order.setAmount(calculateOrderAmount(order, carId));
        if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
        if (order.getStatus() == null) order.setStatus("REQUESTED");
        Car car = carRepository.findById(carId).orElseThrow();
        log.info("...setting car " + carId + " to order " + order.getId());
        order.setCar(car);
        User user = userRepository.findById(userId).orElseThrow();
        order.setUser(user);
        log.info("...adding order to user " + userId);
        if (!user.getOrderList().contains(order)) {
            user.addOrder(order);
        } else user.updateOrder(order);
    }

    private Double calculateOrderAmount(Order order, Long carId) {
        long days = ChronoUnit.DAYS.between(
                order.getDateBegin(),
                order.getDateEnd());
        if (days <= 0) {
            log.info("...DATES OF RENTAL DIFFER BY " + days + " days");
            return 0.0;
        }
        order.setDurability(days);
        Double price = carRepository.findById(carId)
                .orElseThrow()
                .getPrice();
        return Math.round(days * price * 100d) / 100d;
    }

    public void recalculateOrdersAmountUponCarEdit(Long carId) {
        List<Order> orderListByCarId = orderRepository.findAllByCarId(carId);
        for (Order entry : orderListByCarId) {
            entry.setAmount(calculateOrderAmount(entry, entry.getCar().getId()));
            orderRepository.save(entry);
        }
    }
}