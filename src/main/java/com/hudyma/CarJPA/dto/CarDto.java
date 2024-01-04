package com.hudyma.CarJPA.dto;

import com.hudyma.CarJPA.model.CarClass;

public record CarDto (CarClass carClass, String propulsion, Double price) {
}
