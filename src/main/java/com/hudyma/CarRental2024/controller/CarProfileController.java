package com.hudyma.CarRental2024.controller;

import com.hudyma.CarRental2024.dto.CarProfilesDto;
import com.hudyma.CarRental2024.model.CarProfile;
import com.hudyma.CarRental2024.repository.CarProfileRepository;
import com.hudyma.CarRental2024.service.CarProfilesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/car-profiles")
@RequiredArgsConstructor
@Log4j2
@Controller
public class CarProfileController {

    private final CarProfileRepository carProfileRepository;

    private final CarProfilesService carProfilesService;

    @GetMapping
    public List<CarProfile> getAll() {
        return carProfileRepository.findAll();
    }

    @GetMapping("/Dto")
    public List<CarProfilesDto> getAllDto() {
        return carProfilesService.getAll();
    }

    @GetMapping("/{id}")
    public Optional<CarProfile> getById(@PathVariable("id") Long id) {
        return carProfileRepository.findById(id);
    }

    @PatchMapping("/{id}")
    public void upsertProfile(@PathVariable("id") Long id, @RequestBody CarProfile newCarProfile) {
        log.info("...UPSERTING PROFILE ID = " + id);
        log.info("..with NEW PROFILE: " + newCarProfile);
        CarProfile carProfile = carProfileRepository.findById(id).orElseGet(CarProfile::new);
        carProfile.setColor(newCarProfile.getColor());
        carProfile.setSeatsQuantity(newCarProfile.getSeatsQuantity());
        carProfile.setColor(newCarProfile.getColor());
        carProfile.setPropulsion(newCarProfile.getPropulsion());

        carProfilesService.saveProfileWithCar(id, carProfile);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteProfile(@PathVariable("id") Long id) {
        carProfileRepository.deleteById(id);
    }
}
