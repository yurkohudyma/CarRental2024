package com.hudyma.CarRental2024.dto;

import com.hudyma.CarRental2024.constants.CarClass;
import com.hudyma.CarRental2024.constants.CarPropulsion;

public record CarDto (CarClass carClass, CarPropulsion propulsion, Double price) {
}
