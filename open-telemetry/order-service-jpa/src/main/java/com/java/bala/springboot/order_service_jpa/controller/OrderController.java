package com.java.bala.springboot.order_service_jpa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.bala.springboot.order_service_jpa.model.Order;
import com.java.bala.springboot.order_service_jpa.repository.OrderRepository;


@RestController
@RequestMapping ("/orders")
public class OrderController {
	
	private final OrderRepository orderRepository;
	
	public OrderController(OrderRepository orderRepository) {
		super();
		this.orderRepository = orderRepository;
	}

	@GetMapping ("/{id}")
	public Order findbyId(@PathVariable Long id) {
		return orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid id :"+ id));
	}
	

}
