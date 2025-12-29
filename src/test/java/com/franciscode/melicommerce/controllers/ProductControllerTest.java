package com.franciscode.melicommerce.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.franciscode.melicommerce.dto.ProductDTO;
import com.franciscode.melicommerce.services.ProductService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService service;

    @InjectMocks
    private ProductController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();

        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setCustomArgumentResolvers(pageableResolver)
                .build();
    }

    @Test
    void compareProducts_shouldReturnListAndStatus200() throws Exception {
        List<ProductDTO> expected = List.of(
                new ProductDTO(1L, "A", "Desc A long enough", 10.0, "http://img/a", 4.5, "specs"),
                new ProductDTO(2L, "B", "Desc B long enough", 20.0, "http://img/b", 4.0, "specs")
        );

        when(service.compareProductsByIds("1,2")).thenReturn(expected);

        mockMvc.perform(get("/products/compare").param("ids", "1,2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));

        verify(service, times(1)).compareProductsByIds("1,2");
        verifyNoMoreInteractions(service);
    }

    @Test
    void findById_shouldReturnProductAndStatus200() throws Exception {
        ProductDTO dto = new ProductDTO(3L, "Macbook Pro", "Description long enough", 1250.0,
                "http://img/3", 4.8, "Apple M1");

        when(service.findById(3L)).thenReturn(dto);

        mockMvc.perform(get("/products/{id}", 3L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Macbook Pro"))
                .andExpect(jsonPath("$.price").value(1250.0));

        verify(service, times(1)).findById(3L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void findAll_shouldReturnPageAndStatus200() throws Exception {
        ProductDTO p1 = new ProductDTO(1L, "P1", "Desc1 long enough", 10.0, "url1", 4.0, "s1");
        ProductDTO p2 = new ProductDTO(2L, "P2", "Desc2 long enough", 20.0, "url2", 4.1, "s2");

        Pageable pageable = PageRequest.of(0, 2);
        PageImpl<ProductDTO> page = new PageImpl<>(List.of(p1, p2), pageable, 2L);

        when(service.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "price,asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("P1"))
                .andExpect(jsonPath("$.content[1].name").value("P2"));

        verify(service, times(1)).findAll(any(Pageable.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void insert_shouldReturnCreated_withLocation_andBody() throws Exception {
        ProductDTO input = new ProductDTO(null, "New Product", "Description long enough", 199.99,
                "http://img/new", 4.2, "specs");
        ProductDTO created = new ProductDTO(10L, input.getName(), input.getDescription(), input.getPrice(),
                input.getImgUrl(), input.getRating(), input.getSpecifications());

        when(service.insert(any(ProductDTO.class))).thenReturn(created);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString("/products/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("New Product"));

        verify(service, times(1)).insert(any(ProductDTO.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void insert_withInvalidPayload_shouldReturn400() throws Exception {
        ProductDTO invalid = new ProductDTO(null, "A", "short", -5.0, null, null, null);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_shouldReturnUpdatedAndStatus200() throws Exception {
        ProductDTO input = new ProductDTO(null, "Changed", "Description long enough", 150.0,
                "http://img/changed", 4.0, "specs");
        ProductDTO updated = new ProductDTO(5L, input.getName(), input.getDescription(), input.getPrice(),
                input.getImgUrl(), input.getRating(), input.getSpecifications());

        when(service.update(eq(5L), any(ProductDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/products/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Changed"));

        verify(service, times(1)).update(eq(5L), any(ProductDTO.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void delete_shouldReturnNoContentAndCallService() throws Exception {
        doNothing().when(service).delete(7L);

        mockMvc.perform(delete("/products/{id}", 7L))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(7L);
        verifyNoMoreInteractions(service);
    }
}