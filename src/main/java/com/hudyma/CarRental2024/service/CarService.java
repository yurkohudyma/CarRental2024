package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.dto.CarDto;
import com.hudyma.CarRental2024.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public List<CarDto> getAll (){
        return carRepository.findAll().stream()
                .map(s -> new CarDto(
                        s.getCarClass(),
                        s.getPropulsion(),
                        s.getPrice()))
                .toList();

    }

}
