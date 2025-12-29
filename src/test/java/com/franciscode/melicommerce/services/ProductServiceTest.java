package com.franciscode.melicommerce.services;

import com.franciscode.melicommerce.dto.ProductDTO;
import com.franciscode.melicommerce.entities.Product;
import com.franciscode.melicommerce.repositories.ProductRepository;
import com.franciscode.melicommerce.services.exceptions.BadRequestException;
import com.franciscode.melicommerce.services.exceptions.DatabaseException;
import com.franciscode.melicommerce.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "P1", "Desc1", 10.0, "url1", 4.0, "s1");
        product2 = new Product(2L, "P2", "Desc2", 20.0, "url2", 4.1, "s2");
    }

    @Test
    void findById_whenExists_shouldReturnDTO() {
        when(repository.findById(1L)).thenReturn(Optional.of(product1));

        ProductDTO dto = service.findById(1L);

        assertNotNull(dto);
        assertEquals(product1.getId(), dto.getId());
        assertEquals(product1.getName(), dto.getName());
        verify(repository, times(1)).findById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findById_whenNotFound_shouldThrowResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
        verify(repository, times(1)).findById(99L);
    }

    @Test
    void findAll_shouldReturnPageOfDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product1, product2), pageable, 2L);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<ProductDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("P1", result.getContent().get(0).getName());
        assertEquals("P2", result.getContent().get(1).getName());
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void insert_shouldCopyFieldsAndReturnSavedDTO() {
        ProductDTO dto = new ProductDTO(null, "New", "New Desc", 99.99, "img", 4.5, "specs");
        Product saved = new Product(10L, dto.getName(), dto.getDescription(), dto.getPrice(),
                dto.getImgUrl(), dto.getRating(), dto.getSpecifications());

        when(repository.save(any(Product.class))).thenReturn(saved);

        ProductDTO result = service.insert(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(dto.getName(), result.getName());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository, times(1)).save(captor.capture());
        Product entityPassed = captor.getValue();
        assertEquals(dto.getName(), entityPassed.getName());
        assertEquals(dto.getDescription(), entityPassed.getDescription());
    }

    @Test
    void update_whenExists_shouldReturnUpdatedDTO() {
        ProductDTO dto = new ProductDTO(null, "Updated", "Updated Desc", 55.0, "imgU", 4.2, "specU");
        Product existing = new Product(5L, "Old", "Old Desc", 10.0, "imgOld", 3.0, "specOld");
        Product saved = new Product(5L, dto.getName(), dto.getDescription(), dto.getPrice(),
                dto.getImgUrl(), dto.getRating(), dto.getSpecifications());

        when(repository.getReferenceById(5L)).thenReturn(existing);
        when(repository.save(existing)).thenReturn(saved);

        ProductDTO result = service.update(5L, dto);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Updated", result.getName());
        verify(repository, times(1)).getReferenceById(5L);
        verify(repository, times(1)).save(existing);
    }

    @Test
    void update_whenEntityNotFound_shouldThrowResourceNotFoundException() {
        ProductDTO dto = new ProductDTO(null, "X", "Y", 1.0, null, null, null);

        when(repository.getReferenceById(99L)).thenThrow(EntityNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, dto));
        verify(repository, times(1)).getReferenceById(99L);
    }

    @Test
    void delete_whenExists_shouldCallDelete() {
        when(repository.existsById(7L)).thenReturn(true);
        doNothing().when(repository).deleteById(7L);

        assertDoesNotThrow(() -> service.delete(7L));
        verify(repository, times(1)).existsById(7L);
        verify(repository, times(1)).deleteById(7L);
    }

    @Test
    void delete_whenNotExists_shouldThrowResourceNotFoundException() {
        when(repository.existsById(8L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(8L));
        verify(repository, times(1)).existsById(8L);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void delete_whenDataIntegrityViolation_shouldThrowDatabaseException() {
        when(repository.existsById(9L)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class).when(repository).deleteById(9L);

        assertThrows(DatabaseException.class, () -> service.delete(9L));
        verify(repository, times(1)).existsById(9L);
        verify(repository, times(1)).deleteById(9L);
    }

    @Test
    void compareProductsByIds_whenNullOrBlank_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> service.compareProductsByIds(null));
        assertThrows(BadRequestException.class, () -> service.compareProductsByIds(""));
        assertThrows(BadRequestException.class, () -> service.compareProductsByIds("   "));
        verifyNoInteractions(repository);
    }

    @Test
    void compareProductsByIds_whenInvalidNumber_shouldThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> service.compareProductsByIds("a,2"));
        assertThrows(BadRequestException.class, () -> service.compareProductsByIds("1,b"));
        verifyNoInteractions(repository);
    }

    @Test
    void compareProductsByIds_whenNoProductsFound_shouldThrowResourceNotFound() {
        when(repository.findAllById(List.of(100L, 101L))).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> service.compareProductsByIds("100,101"));
        verify(repository, times(1)).findAllById(List.of(100L, 101L));
    }

    @Test
    void compareProductsByIds_whenProductsFound_shouldReturnDTOList() {
        when(repository.findAllById(List.of(1L, 2L))).thenReturn(List.of(product1, product2));

        List<ProductDTO> result = service.compareProductsByIds("1,2");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("P1", result.get(0).getName());
        assertEquals("P2", result.get(1).getName());
        verify(repository, times(1)).findAllById(List.of(1L, 2L));
    }
}