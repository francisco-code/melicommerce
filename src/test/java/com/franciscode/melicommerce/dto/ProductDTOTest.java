package com.franciscode.melicommerce.dto;

import com.franciscode.melicommerce.entities.Product;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductDTOTest {

    @Test
    void defaultConstructor_shouldCreateDtoWithNullFields() {
        ProductDTO dto = new ProductDTO();

        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getPrice());
        assertNull(dto.getImgUrl());
        assertNull(dto.getRating());
        assertNull(dto.getSpecifications());
    }

    @Test
    void allArgsConstructor_andGetters_shouldReturnProvidedValues() {
        Long id = 42L;
        String name = "Product Name";
        String desc = "A sufficiently long description";
        Double price = 199.99;
        String img = "http://example.com/img.png";
        Double rating = 4.7;
        String specs = "SpecA; SpecB";

        ProductDTO dto = new ProductDTO(id, name, desc, price, img, rating, specs);

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(desc, dto.getDescription());
        assertEquals(price, dto.getPrice());
        assertEquals(img, dto.getImgUrl());
        assertEquals(rating, dto.getRating());
        assertEquals(specs, dto.getSpecifications());
    }

    @Test
    void entityConstructor_shouldMapEntityFieldsToDto() {
        Product entity = new Product(7L, "EntName", "Entity description long enough", 55.0,
                "http://img/e.png", 3.9, "ent-specs");

        ProductDTO dto = new ProductDTO(entity);

        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getName(), dto.getName());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getPrice(), dto.getPrice());
        assertEquals(entity.getImgUrl(), dto.getImgUrl());
        assertEquals(entity.getRating(), dto.getRating());
        assertEquals(entity.getSpecifications(), dto.getSpecifications());
    }

    @Test
    void beanValidation_withInvalidValues_shouldProduceConstraintViolations() {
        ProductDTO invalid = new ProductDTO(null, "A", "short", -5.0, null, null, null);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<ProductDTO>> violations = validator.validate(invalid);

            assertFalse(violations.isEmpty(), "Expected validation violations for invalid DTO");

            boolean hasNameViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
            boolean hasDescriptionViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("description"));
            boolean hasPriceViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("price"));

            assertTrue(hasNameViolation, "Expected violation on 'name' property");
            assertTrue(hasDescriptionViolation, "Expected violation on 'description' property");
            assertTrue(hasPriceViolation, "Expected violation on 'price' property");
        }
    }

    @Test
    void beanValidation_withValidValues_shouldProduceNoViolations() {
        ProductDTO valid = new ProductDTO(null,
                "Valid Name",
                "This description is long enough",
                10.0,
                "http://img.png",
                4.0,
                "specs");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<ProductDTO>> violations = validator.validate(valid);

            assertTrue(violations.isEmpty(), "Expected no validation violations for a valid DTO");
        }
    }
}