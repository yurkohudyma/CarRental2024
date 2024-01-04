package com.hudyma.CarJPA.controller;

import com.hudyma.CarJPA.dto.CarDto;
import com.hudyma.CarJPA.model.Car;
import com.hudyma.CarJPA.repository.CarRepository;
import com.hudyma.CarJPA.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {
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
        System.out.println("............Getting car by ID = " + id);
        System.out.println(car);
        return car;
    }

    @GetMapping("/Dto")
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
        System.out.println("............Deleting car " + id);
        Car car = carRepository.findById(id).orElseThrow();
        carRepository.delete(car);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteAll() {
        System.out.println("............Deleting All cars");
        carRepository.findAll()
        .forEach(carRepository::delete);
    }

    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    @PatchMapping("/{id}")
    public void patchCar(@PathVariable("id") Long id, @RequestBody Car updateCar){
        System.out.println("............Trying to update car " + carRepository.findById(id));
        carRepository.save(updateCar);
    }
}
