package com.techfreak.cartify.repository;

import com.techfreak.cartify.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByName(String name);
    List<Product> findByPriceGreaterThan(Double price);

}