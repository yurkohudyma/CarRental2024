package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.constants.CarColor;
import com.hudyma.CarRental2024.constants.CarPropulsion;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.constants.CarClass;
import com.hudyma.CarRental2024.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequestMapping("/cars")
@RequiredArgsConstructor
@Controller
public class CarController {
    private static final String REDIRECT_CARS = "redirect:/cars";
    private final CarRepository carRepository;

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("carList", carRepository.findAll());
        model.addAttribute("carClassArr", CarClass.values());
        model.addAttribute("carColorArr", CarColor.values());
        model.addAttribute("carPropulsionArr", CarPropulsion.values());
        log.info("...Retrieving all available cars");
        return "cars";
    }

    /*@ResponseStatus(HttpStatus.NO_CONTENT) - setting CUSTOM HTTP statuses will deter REDIRECTING */
    @PostMapping
    public String addCar(Car car) {
        log.info("...adding a car = " + car);
        carRepository.save(car);
        return REDIRECT_CARS;
    }

    @DeleteMapping("/{id}")
    public String deleteCar(@PathVariable("id") Long id) {
        if (carRepository.findById(id).isPresent()) {
            log.info("...Deleting car " + id);
            Car car = carRepository.findById(id).orElseThrow();
            carRepository.delete(car);
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
        if (carRepository.findById(id).isPresent()){
            log.info("............Trying to update car " + id);
            carRepository.save(updateCar);
        } else log.info("...Car id = " + id + " does not EXIST");
        return REDIRECT_CARS;
    }

    /*@GetMapping("/{id}")
    public Optional<Car> getCarById(@PathVariable("id") Long id) {
        var car = carRepository.findById(id);
        log.info("...Getting car by ID = " + id);
        log.info(car);
        return car;
    }*/

    /*@GetMapping("/dto")
    public List<CarDto> getAllDto() {
        return carService.getAll();
    }*/

}
