package com.hudyma.CarRental2024;

import com.hudyma.CarRental2024.auth.RegisterRequest;
import com.hudyma.CarRental2024.constants.UserAccessLevel;
import com.hudyma.CarRental2024.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static com.hudyma.CarRental2024.model.Role.*;

@SpringBootApplication
public class CarRental2024Application {

    public static void main(String[] args) {
        SpringApplication.run(CarRental2024Application.class, args);
    }

    //@Bean
    public CommandLineRunner commandLineRunner(AuthService authService) {
        return args -> {
            var admin = RegisterRequest.builder()
                    .name("Юрко Гудима")
                    .email("admin@hudyma.com")
                    .password("password123")
                    .role(ADMIN)
                    .accessLevel(UserAccessLevel.ADMIN)
                    .build();
            System.out.println("Admin token: " + authService.register(admin).getAccessToken());

            var manager = RegisterRequest.builder()
                    .name("Юля Севостьян")
                    .email("manager@hudyma.com")
                    .password("password456")
                    .role(MANAGER)
                    .accessLevel(UserAccessLevel.MANAGER)
                    .build();
            System.out.println("Manager token: " + authService.register(manager).getAccessToken());

            var user = RegisterRequest.builder()
                    .name("Степан Жменя")
                    .email("zhmemia@hudyma.com")
                    .password("password789")
                    .role(USER)
                    .accessLevel(UserAccessLevel.USER)
                    .build();
            System.out.println("User token: " + authService.register(user).getAccessToken());
        };

    }


}
