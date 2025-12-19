package com.java.bala.springboot.order_service_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.bala.springboot.order_service_jpa.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
