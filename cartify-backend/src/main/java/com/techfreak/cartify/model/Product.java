package com.techfreak.cartify.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class Product {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    @PositiveOrZero
    private Double price;

    public Product(Long id, String name, Double price){
        this.id = id;
        this.name = name;
        this.price = price;
    }
    public Long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public Double getPrice(){
        return price;
    }
}
