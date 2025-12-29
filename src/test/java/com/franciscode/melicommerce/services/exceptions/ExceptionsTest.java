package com.franciscode.melicommerce.services.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void badRequestException_shouldKeepMessageAndBeRuntimeException() {
        String msg = "Invalid request";
        BadRequestException ex = new BadRequestException(msg);

        assertEquals(msg, ex.getMessage());

        assertTrue(ex instanceof RuntimeException);

        BadRequestException thrown = assertThrows(BadRequestException.class, () -> { throw ex; });
        assertEquals(msg, thrown.getMessage());
    }

    @Test
    void databaseException_shouldKeepMessageAndBeRuntimeException() {
        String msg = "DB integrity violation";
        DatabaseException ex = new DatabaseException(msg);

        assertEquals(msg, ex.getMessage());
        assertTrue(ex instanceof RuntimeException);

        DatabaseException thrown = assertThrows(DatabaseException.class, () -> { throw ex; });
        assertEquals(msg, thrown.getMessage());
    }

    @Test
    void resourceNotFoundException_shouldKeepMessageAndBeRuntimeException() {
        String msg = "Resource not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(msg);

        assertEquals(msg, ex.getMessage());
        assertTrue(ex instanceof RuntimeException);

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> { throw ex; });
        assertEquals(msg, thrown.getMessage());
    }
}