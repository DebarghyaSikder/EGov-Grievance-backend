package com.grievance.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

import com.grievance.api_gateway.filter.JwtAuthFilter;

@SpringBootApplication
public class ApiGatewayApplication {

	@Bean
	public JwtAuthFilter jwtFilter(JwtAuthFilter filter) {
	    return filter;
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
