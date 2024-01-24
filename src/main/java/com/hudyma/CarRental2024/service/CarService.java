package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.constants.CarClass;
import com.hudyma.CarRental2024.dto.CarDto;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Log4j2
public class CarService {

    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public List<CarDto> getAll() {
        return carRepository.findAll().stream()
                .map(s -> new CarDto(
                        s.getCarClass(),
                        s.getPropulsion(),
                        s.getPrice()))
                .toList();

    }
}
