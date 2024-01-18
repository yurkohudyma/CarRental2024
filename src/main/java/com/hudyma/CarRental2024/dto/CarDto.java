package com.hudyma.CarRental2024.dto;

import com.hudyma.CarRental2024.model.CarClass;

public record CarDto (CarClass carClass, String propulsion, Double price) {
}
