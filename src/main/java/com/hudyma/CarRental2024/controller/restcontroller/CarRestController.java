package com.hudyma.CarRental2024.controller.restcontroller;

import com.hudyma.CarRental2024.dto.CarDto;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@Log4j2
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarRestController {
    private final CarRepository carRepository;
    private final CarService carService;

    //todo gives JSON of car and profile
    @GetMapping
    public List<Car> getAll() {
        return carRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Car> getCarById(@PathVariable("id") Long id) {
        var car = carRepository.findById(id);
        log.info("...Getting car by ID = " + id);
        log.info(car);
        return car;
    }

    @GetMapping("/dto")
    public List<CarDto> getAllDto() {
        return carService.getAll();
    }

    @PostMapping("/one")
    @ResponseStatus(HttpStatus.CREATED)
    public void addCar(@RequestBody Car car) {
        carRepository.save(car);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addCars(@RequestBody Car[] car) {
        Arrays.stream(car)
                .forEach(carRepository::save);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable("id") Long id) {
        log.info("...Deleting car " + id);
        Car car = carRepository.findById(id).orElseThrow();
        carRepository.delete(car);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteAll() {
        log.info("............Deleting All cars");
        carRepository.findAll()
        .forEach(carRepository::delete);
    }

    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    @PatchMapping("/{id}")
    public void patchCar(@PathVariable("id") Long id, @RequestBody Car updateCar){
        log.info("............Trying to update car " + carRepository.findById(id));
        carRepository.save(updateCar);
    }
}
