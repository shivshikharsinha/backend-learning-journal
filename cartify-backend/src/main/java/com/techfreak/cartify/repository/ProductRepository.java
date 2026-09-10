package com.techfreak.cartify.repository;

import com.techfreak.cartify.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}