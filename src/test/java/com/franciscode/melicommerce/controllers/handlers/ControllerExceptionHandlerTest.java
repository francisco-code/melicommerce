package com.franciscode.melicommerce.controllers.handlers;

import com.franciscode.melicommerce.dto.CustomError;
import com.franciscode.melicommerce.services.exceptions.BadRequestException;
import com.franciscode.melicommerce.services.exceptions.DatabaseException;
import com.franciscode.melicommerce.services.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    private ControllerExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new ControllerExceptionHandler();
    }

    @Test
    void resourceNotFound_shouldReturn404AndCustomError() {
        String uri = "/products/999";
        when(request.getRequestURI()).thenReturn(uri);

        String message = "Recurso não encontrado";
        ResourceNotFoundException ex = new ResourceNotFoundException(message);

        Instant before = Instant.now();
        ResponseEntity<CustomError> response = handler.resourceNotFound(ex, request);
        Instant after = Instant.now();

        assertNotNull(response, "ResponseEntity should not be null");
        // Use getStatusCodeValue() to get int (previously asserted against HttpStatus instance)
        assertEquals(404, response.getStatusCode().value(), "HTTP status should be 404");

        CustomError body = response.getBody();
        assertNotNull(body, "Body should not be null");

        assertEquals(404, body.getStatus(), "CustomError.status must match 404");
        assertEquals(message, body.getError(), "CustomError.error must contain exception message");
        assertEquals(uri, body.getPath(), "CustomError.path must match request URI");

        assertNotNull(body.getTimestamp(), "CustomError.timestamp must be present");
        // timestamp should be between before and after (inclusive)
        assertFalse(body.getTimestamp().isBefore(before), "timestamp should not be before method invocation");
        assertFalse(body.getTimestamp().isAfter(after), "timestamp should not be after method return");
    }

    @Test
    void database_shouldReturn400AndCustomError() {
        String uri = "/products";
        when(request.getRequestURI()).thenReturn(uri);

        String message = "Falha de integridade referencial";
        DatabaseException ex = new DatabaseException(message);

        Instant before = Instant.now();
        ResponseEntity<CustomError> response = handler.database(ex, request);
        Instant after = Instant.now();

        assertNotNull(response);
        // fix: compare integers with getStatusCodeValue()
        assertEquals(400, response.getStatusCode().value());

        CustomError body = response.getBody();
        assertNotNull(body);

        assertEquals(400, body.getStatus());
        assertEquals(message, body.getError());
        assertEquals(uri, body.getPath());

        assertNotNull(body.getTimestamp());
        assertFalse(body.getTimestamp().isBefore(before));
        assertFalse(body.getTimestamp().isAfter(after));
    }

    @Test
    void badRequest_shouldReturn400AndCustomError() {
        String uri = "/products/compare";
        when(request.getRequestURI()).thenReturn(uri);

        String message = "O parâmetro 'ids' é obrigatório.";
        BadRequestException ex = new BadRequestException(message);

        Instant before = Instant.now();
        ResponseEntity<CustomError> response = handler.badRequest(ex, request);
        Instant after = Instant.now();

        assertNotNull(response);
        // fix: compare integers with getStatusCodeValue()
        assertEquals(400, response.getStatusCode().value());

        CustomError body = response.getBody();
        assertNotNull(body);

        assertEquals(400, body.getStatus());
        assertEquals(message, body.getError());
        assertEquals(uri, body.getPath());

        assertNotNull(body.getTimestamp());
        assertFalse(body.getTimestamp().isBefore(before));
        assertFalse(body.getTimestamp().isAfter(after));
    }
}