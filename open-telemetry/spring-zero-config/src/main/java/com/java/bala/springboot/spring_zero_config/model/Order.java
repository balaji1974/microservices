package com.java.bala.springboot.spring_zero_config.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record Order(Long id, Long customerId, ZonedDateTime orderTime, BigDecimal totalAmount) {

}