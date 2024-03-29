package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.constants.CarClass;
import com.hudyma.CarRental2024.constants.CarPropulsion;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import com.hudyma.CarRental2024.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/edit")
@RequiredArgsConstructor
@Log4j2
public class EditController {

    public static final String EDIT = "edit", ACTION = "action";
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final OrderRepository orderRepository;
    private final CarService carService;

    @GetMapping("/{id}/user")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute(ACTION, "user");
        return EDIT;
    }

    @GetMapping("/{id}/car")
    public String editCar(@PathVariable Long id, Model model) {
        Car car = carRepository.findById(id).orElseThrow();
        model.addAllAttributes(Map.of("car", car,
                                                ACTION, "car",
                                                //"carColorArr", CarColor.values(),
                                                "carClassArr", CarClass.values(),
                                                "carPropulsionArr", CarPropulsion.values()));
        return EDIT;
    }

    @GetMapping("/{id}/order")
    public String editOrder (@PathVariable Long id, Model model){
        Order order = orderRepository.findById(id).orElseThrow();
        model.addAllAttributes(Map.of(
        "carList", carService.getAllAvailableCarsSortedByFieldAsc(),
        "order", order,
        ACTION, "order",
        "currentDate", order.getDateBegin(),
        "currentNextDate", LocalDate.now().plusDays(1)));
        return EDIT;
    }
}
