package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.constants.CarColor;
import com.hudyma.CarRental2024.constants.CarPropulsion;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.constants.CarClass;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.service.CarService;
import com.hudyma.CarRental2024.service.OrderService;
import com.hudyma.CarRental2024.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@RequestMapping("/cars")
@RequiredArgsConstructor
@Controller
public class CarController {
    private static final String REDIRECT_CARS = "redirect:/cars";
    public static final String CARS = "cars";
    private final CarRepository carRepository;
    private final OrderService orderService;
    private final CarService carService;
    private final UserService userService;

    @GetMapping
    public String getAll(Model model) {
        model.addAllAttributes(Map.of(
                "carList", carRepository.findAll(),
                "carClassArr", CarClass.values(),
                "carColorArr", CarColor.values(),
                "carPropulsionArr", CarPropulsion.values(),
                "showAddCarForm", true,
                "soleCarCard", false,
                "carOrdersList", new ArrayList<>()));
        assignAttributesForStats(model);
        log.info("...Retrieving all available cars");
        return CARS;
    }

    private void assignAttributesForStats(Model model) {
        model.addAllAttributes(
                Map.of("ordersQuantity", orderService.getAllOrders().size(),
                        "ordersAmount", orderService.getAllOrdersRentalPayments(),
                        "carsQuantity", carService.getAllCarsQuantity(),
                        "usersQuantity", userService.getAllUsersQuantity()));
    }

    @GetMapping("/{id}")
    public String getCar(@PathVariable Long id, Model model) {
        addModelAttributesForSoleCarView(id, model);
        log.info("...Retrieving car " + id);
        return CARS;
    }

    private void addModelAttributesForSoleCarView(Long id, Model model) {
        model.addAttribute("carList", List.of(carRepository
                .findById(id)
                .orElseThrow()));
        model.addAttribute("showAddCarForm", false);
        model.addAttribute("soleCarCard", true);
        model.addAttribute("carOrdersList",
                orderService.getOrdersByCarId(id));
    }

    @PostMapping
    public String addCar(Car car) {
        log.info("...adding a car = " + car);
        car.setRegisterDate(LocalDateTime.now());
        car.setAvailable(car.getTotalQty());
        carRepository.save(car);
        return REDIRECT_CARS;
    }

    @DeleteMapping("/{id}")
    public String deleteCar(@PathVariable("id") Long id, Model model) {
        List<Order> carOrdersList = orderService.getOrdersByCarId(id);
        if (!carOrdersList.isEmpty()) {
            log.error("...Car {} is currently attached " +
                    "to existing order(s)", id);
            model.addAttribute("showErrorCarUsedInOrder", true);
            addModelAttributesForSoleCarView(id, model);
            return CARS;
        } else {
            log.info("...Deleting car " + id);
            carRepository.deleteById(id);
        }
        return REDIRECT_CARS;
    }

    @DeleteMapping
    public String deleteAll(Model model) {
        List<Order> orderList = orderService.getAllOrders();
        if (orderList.isEmpty()) {
            carRepository.findAll()
                    .forEach(carRepository::delete);
            log.info("............Deleting All cars");
            return REDIRECT_CARS;
        } else {
            log.error("...Cars to be deleted are attached to existing Orders, " +
                    "first remove Orders");
            model.addAttribute("showErrorCarUsedInOrder", true);
            model.addAttribute("blockOrderEntryFormDisplay", true);
            model.addAttribute("orderList", orderList);
            return "orders";
        }
    }

    @PatchMapping("/{carId}")
    public String patchCar(@PathVariable Long carId, Car updateCar) {
        Car prvCar = carRepository.findById(carId).orElseThrow();
        updateCar = carService.ifNullableMergeOldValues(updateCar, prvCar);
        updateCar.setUpdateDate(LocalDateTime.now());
        updateCar.setAvailable(prvCar.getTotalQty());
        updateCar.setId(prvCar.getId());
        carRepository.save(updateCar);
        log.info("............Car {} has been updated", prvCar.getModel());
        orderService.recalculateOrdersAmountUponCarEdit(carId);
        return REDIRECT_CARS + "/" + carId;
    }
}
