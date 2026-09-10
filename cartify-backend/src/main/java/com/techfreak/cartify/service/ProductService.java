package com.techfreak.cartify.service;

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
