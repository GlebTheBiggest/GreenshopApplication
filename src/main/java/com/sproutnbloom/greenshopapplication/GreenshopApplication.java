package com.sproutnbloom.greenshopapplication;

import com.sproutnbloom.greenshopapplication.request.RegisterRequest;
import com.sproutnbloom.greenshopapplication.service.auth.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static com.sproutnbloom.greenshopapplication.enums.Role.ADMIN;
import static com.sproutnbloom.greenshopapplication.enums.Role.MANAGER;

@SpringBootApplication
public class GreenshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenshopApplication.class, args);
    }

    /*@Bean
    public CommandLineRunner commandLineRunner(AuthService service) {
        return args -> {
            var admin = RegisterRequest.builder()
                    .firstname("Admin")
                    .lastname("Admin")
                    .email("admin@mail.com")
                    .password("password")
                    .role(ADMIN)
                    .build();
            System.out.println("Admin token: " + service.register(admin).getAccessToken());

            var manager = RegisterRequest.builder()
                    .firstname("Admin")
                    .lastname("Admin")
                    .email("manager@mail.com")
                    .password("password")
                    .role(MANAGER)
                    .build();
            System.out.println("Manager token: " + service.register(manager).getAccessToken());

        };
    }*/
}