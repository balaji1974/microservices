package com.java.bala.springboot.order_service.controller;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.bala.springboot.order_service.model.Order;

@RestController
@RequestMapping ("/orders")
public class OrderController {
	
	@GetMapping ("/{id}")
	public Order findbyId(@PathVariable Long id) {
		return new Order(id, 1L, ZonedDateTime.now(), BigDecimal.TEN);
	}
	

}
