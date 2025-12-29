package com.franciscode.melicommerce.entities;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntitiesTest {

    @Test
    void category_constructor_getters_setters_equals_hashCode() {
        Category c1 = new Category();
        assertNull(c1.getId());
        assertNull(c1.getName());
        assertNotNull(c1.getProducts()); // default empty set

        c1.setId(10L);
        c1.setName("Books");

        assertEquals(10L, c1.getId());
        assertEquals("Books", c1.getName());

        Category c2 = new Category(10L, "Books");
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        Category c3 = new Category();
        Category c4 = new Category();
        // both ids null -> equals should be true because Objects.equals(null, null) is true in implementation
        assertEquals(c3, c4);
        assertEquals(0, c3.hashCode()); // id == null -> 0
    }

    @Test
    void product_getters_setters_collections_equals_hashCode_and_getOrders() {
        Product prod = new Product();
        assertNull(prod.getId());
        assertNotNull(prod.getCategories());
        assertNotNull(prod.getItems());

        prod.setId(5L);
        prod.setName("PC");
        prod.setDescription("Gaming PC");
        prod.setPrice(1200.0);
        prod.setImgUrl("http://img");
        prod.setRating(4.6);
        prod.setSpecifications("i7;16GB");

        assertEquals(5L, prod.getId());
        assertEquals("PC", prod.getName());
        assertEquals("Gaming PC", prod.getDescription());
        assertEquals(1200.0, prod.getPrice());
        assertEquals("http://img", prod.getImgUrl());
        assertEquals(4.6, prod.getRating());
        assertEquals("i7;16GB", prod.getSpecifications());

        Product other = new Product(5L, "PC", "Gaming PC", 1200.0, "http://img", 4.6, "i7;16GB");
        assertEquals(prod, other);
        assertEquals(prod.hashCode(), other.hashCode());

        // test orders aggregation via items
        Order order = new Order(100L, Instant.now(), OrderStatus.PAID, null, null);
        OrderItem oi = new OrderItem(order, prod, 1, 1200.0);

        // add to both sides' items sets to simulate relationship
        prod.getItems().add(oi);
        order.getItems().add(oi);

        List<Order> ordersFromProduct = prod.getOrders();
        assertEquals(1, ordersFromProduct.size());
        assertEquals(order, ordersFromProduct.get(0));
    }

    @Test
    void order_getters_setters_equals_hashCode_and_getProducts() {
        User client = new User(1L, "John", "john@example.com", "9999", LocalDate.now(), "pass");
        Payment payment = new Payment(50L, Instant.now(), null);

        Order order = new Order();
        order.setId(200L);
        order.setMoment(Instant.parse("2025-01-01T10:00:00Z"));
        order.setStatus(OrderStatus.WAITING_PAYMENT);
        order.setClient(client);
        order.setPayment(payment);

        assertEquals(200L, order.getId());
        assertEquals(Instant.parse("2025-01-01T10:00:00Z"), order.getMoment());
        assertEquals(OrderStatus.WAITING_PAYMENT, order.getStatus());
        assertEquals(client, order.getClient());
        assertEquals(payment, order.getPayment());

        Order other = new Order(200L, order.getMoment(), order.getStatus(), client, payment);
        assertEquals(order, other);
        assertEquals(order.hashCode(), other.hashCode());

        // build product and orderItem linking to populate getProducts
        Product p1 = new Product(1L, "A", "Desc A", 10.0, null, 4.0, null);
        Product p2 = new Product(2L, "B", "Desc B", 20.0, null, 4.1, null);

        OrderItem oi1 = new OrderItem(order, p1, 2, 10.0);
        OrderItem oi2 = new OrderItem(order, p2, 1, 20.0);

        order.getItems().add(oi1);
        order.getItems().add(oi2);

        List<Product> products = order.getProducts();
        assertEquals(2, products.size());
        assertTrue(products.contains(p1));
        assertTrue(products.contains(p2));
    }

    @Test
    void orderItem_and_orderItemPK_equals_hashCode_and_getters_setters() {
        Order order = new Order(1L, Instant.now(), OrderStatus.PAID, null, null);
        Product product = new Product(2L, "X", "Desc", 5.0, null, 4.0, null);

        OrderItemPK pk1 = new OrderItemPK();
        pk1.setOrder(order);
        pk1.setProduct(product);

        OrderItemPK pk2 = new OrderItemPK();
        pk2.setOrder(order);
        pk2.setProduct(product);

        assertEquals(pk1, pk2);
        assertEquals(pk1.hashCode(), pk2.hashCode());

        OrderItem oi1 = new OrderItem(order, product, 3, 5.0);
        OrderItem oi2 = new OrderItem();
        oi2.setOrder(order);
        oi2.setProduct(product);
        oi2.setQuantity(3);
        oi2.setPrice(5.0);

        assertEquals(oi1.getOrder(), order);
        assertEquals(oi1.getProduct(), product);
        assertEquals(3, oi1.getQuantity());
        assertEquals(5.0, oi1.getPrice());

        // embed id equality: as both have same order & product they should be equal
        assertEquals(oi1, oi2);
        assertEquals(oi1.hashCode(), oi2.hashCode());
    }

    @Test
    void orderStatus_enum_values() {
        OrderStatus[] values = OrderStatus.values();
        assertTrue(values.length >= 1);
        assertEquals("WAITING_PAYMENT", OrderStatus.WAITING_PAYMENT.name());
        assertEquals("PAID", OrderStatus.PAID.name());
    }

    @Test
    void payment_getters_setters_equals_hashCode() {
        Order order = new Order(3L, Instant.now(), OrderStatus.DELIVERED, null, null);
        Payment p = new Payment();
        p.setId(7L);
        p.setMoment(Instant.parse("2024-12-12T12:00:00Z"));
        p.setOrder(order);

        assertEquals(7L, p.getId());
        assertEquals(Instant.parse("2024-12-12T12:00:00Z"), p.getMoment());
        assertEquals(order, p.getOrder());

        Payment other = new Payment(7L, p.getMoment(), order);
        assertEquals(p, other);
        assertEquals(p.hashCode(), other.hashCode());

        Payment noId = new Payment();
        assertNotEquals(p, noId);
    }

    @Test
    void user_getters_setters_equals_hashCode() {
        User u = new User();
        assertNotNull(u.getOrders());

        u.setId(11L);
        u.setName("Alice");
        u.setEmail("alice@example.com");
        u.setPhone("123");
        u.setBirthDate(LocalDate.of(1990, 1, 1));
        u.setPassword("pwd");

        assertEquals(11L, u.getId());
        assertEquals("Alice", u.getName());
        assertEquals("alice@example.com", u.getEmail());
        assertEquals("123", u.getPhone());
        assertEquals(LocalDate.of(1990, 1, 1), u.getBirthDate());
        assertEquals("pwd", u.getPassword());

        User other = new User(11L, "Alice", "alice@example.com", "123", LocalDate.of(1990,1,1), "pwd");
        assertEquals(u, other);
        assertEquals(u.hashCode(), other.hashCode());

        User nullIds1 = new User();
        User nullIds2 = new User();
        // both null id -> considered equal by implementation
        assertEquals(nullIds1, nullIds2);
        assertEquals(0, nullIds1.hashCode());
    }

    @Test
    void product_equals_with_null_and_different_ids() {
        Product p1 = new Product();
        Product p2 = new Product();
        // both ids null -> equals true
        assertEquals(p1, p2);
        assertEquals(0, p1.hashCode());

        Product p3 = new Product(1L, "A", "D", 1.0, null, 4.0, null);
        Product p4 = new Product(2L, "B", "D2", 2.0, null, 4.5, null);
        assertNotEquals(p3, p4);
    }
}