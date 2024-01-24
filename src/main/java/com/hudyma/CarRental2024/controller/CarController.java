package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.constants.CarColor;
import com.hudyma.CarRental2024.constants.CarPropulsion;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.constants.CarClass;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Log4j2
@RequestMapping("/cars")
@RequiredArgsConstructor
@Controller
public class CarController {
    private static final String REDIRECT_CARS = "redirect:/cars";
    private final CarRepository carRepository;
    private final OrderService orderService;

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("carList", carRepository.findAll());
        model.addAttribute("carClassArr", CarClass.values());
        model.addAttribute("carColorArr", CarColor.values());
        model.addAttribute("carPropulsionArr", CarPropulsion.values());
        model.addAttribute("showAddCarForm", true);
        log.info("...Retrieving all available cars");
        return "cars";
    }

    @GetMapping("/{id}")
    public String getCar (@PathVariable Long id, Model model){
        model.addAttribute("carList", List.of(carRepository
                .findById(id)
                .orElseThrow()));
        model.addAttribute("showAddCarForm", false);
        log.info("...Retrieving car "+id);
        return "cars";
    }

    @PostMapping
    public String addCar(Car car) {
        log.info("...adding a car = " + car);
        carRepository.save(car);
        return REDIRECT_CARS;
    }

    @DeleteMapping("/{id}")
    public String deleteCar(@PathVariable("id") Long id) {
        Optional<Car> car = carRepository.findById(id);
        if (car.isPresent()) {
            log.info("...Deleting car " + id);
            carRepository.deleteById(id);
        } else log.info("...Car id = " + id + " does not EXIST");
        return REDIRECT_CARS;
    }

    @DeleteMapping
    public String deleteAll() {
        log.info("............Deleting All cars");
        carRepository.findAll()
                .forEach(carRepository::delete);
        return REDIRECT_CARS;
    }

    @PatchMapping("/{id}")
    public String patchCar(@PathVariable("id") Long id, Car updateCar) {
        if (updateCar.getId().equals(id)) {
            log.info("............Trying to update car " + id);
            carRepository.save(updateCar);
            orderService.recalculateOrdersAmountUponCarEdit(id);
        } else log.info("...Car id = " + id + " does not EXIST");
        return REDIRECT_CARS+"/"+id;
    }
}
