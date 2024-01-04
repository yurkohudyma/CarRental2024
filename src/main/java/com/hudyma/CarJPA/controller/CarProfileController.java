package com.hudyma.CarJPA.controller;

import com.hudyma.CarJPA.dto.CarProfilesDto;
import com.hudyma.CarJPA.model.CarProfile;
import com.hudyma.CarJPA.repository.CarProfileRepository;
import com.hudyma.CarJPA.service.CarProfilesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/car-profiles")
@RequiredArgsConstructor
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
        System.out.println("...........UPSERTING PROFILE ID = " + id);
        System.out.println("............with NEW PROFILE: " + newCarProfile);
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
