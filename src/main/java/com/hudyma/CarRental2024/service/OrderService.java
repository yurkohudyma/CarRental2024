package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public void setOrder (Order order, Long carId, Long userId) {
        log.info("...calculating order amount order");
        order.setAmount(calculateOrderAmount(order, carId));
        log.info("...setting Order status");
        order.setStatus("REQUESTED");

        Car car = carRepository.findById(carId).orElseThrow();
        log.info("...setting car "+carId +" to order");
        order.setCar(car);

        log.info("...getting user by user id");
        User user = userRepository.findById(userId).orElseThrow();

        /*log.info("...setting Order user = "+user);
        order.setUser(user);*/
        log.info("...adding order to user "+userId);
        user.addOrder(order);
    }

    //todo implement current date validation as regex in front
    private Double calculateOrderAmount(Order order, Long carId) {
        long days = ChronoUnit.DAYS.between(
                order.getDateBegin(),
                order.getDateEnd());

        long daysBefore = ChronoUnit.DAYS.between(
                LocalDate.now(),
                order.getDateBegin());

        if (days <= 0 || daysBefore <= 0) throw new IllegalArgumentException
                (".........DATES OF RENTAL INCORRECT");
        order.setDurability(days);
        Double price = carRepository.findById(carId)
                .orElseThrow()
                .getPrice();
        return days * price;
    }
}
