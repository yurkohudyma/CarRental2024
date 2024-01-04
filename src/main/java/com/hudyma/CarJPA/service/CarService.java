package com.hudyma.CarJPA.service;

import com.hudyma.CarJPA.dto.CarDto;
import com.hudyma.CarJPA.repository.CarRepository;
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
                        s.getCarProfile().getPropulsion(),
                        s.getPrice()))
                .toList();

    }

}
