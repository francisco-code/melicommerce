package com.franciscode.melicommerce.services;

import com.franciscode.melicommerce.dto.ProductDTO;
import com.franciscode.melicommerce.entities.Product;
import com.franciscode.melicommerce.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = repository.findById(id).get();
        return new ProductDTO(product);
    }
}