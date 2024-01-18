package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.dto.CarProfilesDto;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.CarProfile;
import com.hudyma.CarRental2024.repository.CarProfileRepository;
import com.hudyma.CarRental2024.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
public class CarProfilesService {

    private final CarProfileRepository carProfileRepository;

    @Autowired
    private final CarRepository carRepository;

    @Transactional (readOnly = true)
    public List<CarProfilesDto> getAll (){
        return carProfileRepository.findAll().stream()
                .map(n -> new CarProfilesDto(
                        n.getCar().getModel(),
                        n.getSeatsQuantity(),
                        n.getColor()))
                .toList();
    }

    @Transactional
    public void saveProfileWithCar (Long id, CarProfile carProfile){
        Car car = carRepository.findById(id).orElseThrow(() -> new NoSuchElementException("...NO CAR"));
        carProfile.setCar(car);
        car.setCarProfile(carProfile);
        car.setId(id);
        carRepository.save(car);
    }


    //todo this variant for slower performance but less impact on memory if downloading enormous amount of data from DB
    //todo this will download data from DB in batches (setFetchSize in Prepared Statement or QueryHints)
    /*@Transactional (readOnly = true)
    public void getAllByStream (){
        try (var profileStream = carProfileRepository.findAll()){
            profileStream.forEach(this::doSmth);

        }

    }*/

}
