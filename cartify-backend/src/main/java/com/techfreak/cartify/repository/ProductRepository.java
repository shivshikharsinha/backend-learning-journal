package com.techfreak.cartify.repository;

import com.techfreak.cartify.model.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {


    /* Temporary in-memory storage. Will be replaced with MySQL via Spring Data JPA.*/

    private final List<Product> products = new ArrayList<>();

   /* public ProductRepository() {
        products.add(new Product(1L, "iPhone 17 Pro", 99999));
    }*/

    public List<Product> findAll() {
        return products;
    }

    public Product save(Product product) {
        products.add(product);
        return product;
    }

    public Optional<Product> findById(Long id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    public void deleteById(Long id) {
        products.removeIf(product -> product.getId().equals(id));

/*      Iterator<Product> iterator = products.iterator();

        while (iterator.hasNext()) {
            Product product = iterator.next();

            if (product.getId().equals(id)) {
                iterator.remove();
                return;
            }
        }
*/
    }
}
