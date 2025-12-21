package com.java.bala.springboot.spring_zero_config.controller;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.bala.springboot.spring_zero_config.model.Order;

import io.micrometer.observation.ObservationRegistry;

@RestController
@RequestMapping ("/orders")
public class OrderController {
	
	private final ObservationRegistry observationRegistry;
	
	public OrderController(ObservationRegistry observationRegistry) {
	    this.observationRegistry = observationRegistry;
	}

	@GetMapping ("/{id}")
	public Order findbyId(@PathVariable Long id) {
		return new Order(id, 1L, ZonedDateTime.now(), BigDecimal.TEN);
	}
	
}
