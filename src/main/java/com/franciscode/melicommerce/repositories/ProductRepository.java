package com.franciscode.melicommerce.repositories;

import com.franciscode.melicommerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}