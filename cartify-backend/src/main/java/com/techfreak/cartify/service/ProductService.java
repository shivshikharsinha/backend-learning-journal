package com.techfreak.cartify.service;

import com.techfreak.cartify.exception.InvalidProductException;
import com.techfreak.cartify.exception.ProductNotFoundException;
import com.techfreak.cartify.model.Product;
import com.techfreak.cartify.repository.ProductRepository;
import org.springframework.stereotype.Service;
import com.techfreak.cartify.exception.ProductAlreadyExistsException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /*
    *** Part of earlier lesson :  Now Obsolete *****
    *
    public Product getProduct(){
        return new Product(1L, "iPhone 17 Pro", 99999);
    }*/
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() ->
                new ProductNotFoundException("Product not found with id : " + id));
    }

    public Product createProduct(Product product) {

        if (product == null) {
            throw new RuntimeException("Product cannot be null.");
        }

        String productName = product.getName();
        Long productId = product.getId();
        Double productPrice = product.getPrice();

        if (productId == null) {
            throw new InvalidProductException("Product ID cannot be null.");
        }

        if (productName == null || productName.isBlank()) {
            throw new InvalidProductException("Product name cannot be blank.");
        }

        if (productPrice == null) {
            throw new InvalidProductException("Product price cannot be null.");
        }

        if (productPrice < 0) {
            throw new InvalidProductException("Product price cannot be negative.");
        }

        if (productRepository.findById(productId).isPresent()) {
            throw new ProductAlreadyExistsException("Product ID already exists.");
        }

        return productRepository.save(product);
    }

    public void deleteProduct(Long id){

        if (id == null) {
            throw new RuntimeException("Product ID cannot be null.");
        }
        getProductById(id);
        productRepository.deleteById(id);
    }
}
