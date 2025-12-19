package com.java.bala.springboot.order_service_jpa.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="orders")
public class Order {
	
	@Id
	private Long id; 
	
	@Column(name="customer_id")
	private Long customerId;
	
	@Column(name="order_date")
	private ZonedDateTime orderTime;
	
	@Column(name="total_amount")
	private BigDecimal totalAmount;
	
	public Long getId() {
		return id;
	}
	public Long getCustomerId() {
		return customerId;
	}
	public ZonedDateTime getOrderTime() {
		return orderTime;
	}
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
	
	

}
