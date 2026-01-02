package com.grievance.grievance_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GrievanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrievanceServiceApplication.class, args);
    }
}