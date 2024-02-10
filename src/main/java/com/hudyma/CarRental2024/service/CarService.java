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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Log4j2
public class CarService {
    public static final String IS_MISSING_FOR_USER = "{} is missing for User {}";
    private final CarRepository carRepository;


    public List<Car> getAllCarsSortedByFieldAsc(String sortField) {
        return carRepository.findAll(Sort.by(
                Sort.Direction.ASC, sortField));
    }

    @Transactional(readOnly = true)
    public List<CarDto> getAll() {
        return carRepository.findAll().stream()
                .map(s -> new CarDto(
                        s.getCarClass(),
                        s.getPropulsion(),
                        s.getPrice()))
                .toList();

    }

    public int getAllCarsQuantity() {
        return carRepository.findAll().size();
    }

    public Car ifNullableMergeOldValues(Car car, Car prvCar) {
        if (car.getModel().equals("")) {
            car.setModel(prvCar.getModel());
            log.error(IS_MISSING_FOR_USER, "model", car.getId());
        }
        if (car.getDescription().equals("")) {
            car.setDescription(prvCar.getDescription());
            log.error(IS_MISSING_FOR_USER, "description", car.getId());
        }
        if (car.getPrice() == null) {
            car.setPrice(prvCar.getPrice());
            log.error(IS_MISSING_FOR_USER, "price", car.getId());
        }

        if (car.getSeatsQuantity() == null) {
            car.setSeatsQuantity(prvCar.getSeatsQuantity());
            log.error(IS_MISSING_FOR_USER, "seatsQuantity", car.getId());
        }
        return car;
    }

}
