package com.franciscode.melicommerce.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CustomErrorTest {

    @Test
    void constructorAndGetters_shouldReturnProvidedValues() {
        Instant now = Instant.now();
        Integer status = 404;
        String error = "Not Found";
        String path = "/products/1";

        CustomError customError = new CustomError(now, status, error, path);

        assertSame(now, customError.getTimestamp(), "Timestamp should be the same instance passed in");
        assertEquals(status, customError.getStatus(), "Status should match the provided value");
        assertEquals(error, customError.getError(), "Error message should match the provided value");
        assertEquals(path, customError.getPath(), "Path should match the provided value");
    }

    @Test
    void constructor_allowsNulls_andGettersReturnNulls() {
        CustomError customError = new CustomError(null, null, null, null);

        assertNull(customError.getTimestamp(), "Timestamp should be null when constructed with null");
        assertNull(customError.getStatus(), "Status should be null when constructed with null");
        assertNull(customError.getError(), "Error should be null when constructed with null");
        assertNull(customError.getPath(), "Path should be null when constructed with null");
    }
}