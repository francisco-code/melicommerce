package com.franciscode.melicommerce.dto;

import com.franciscode.melicommerce.entities.Product;

public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imgUrl;
    private Double rating;
    private String specifications;

    public ProductDTO(Long id, String name, String description, Double price, String imgUrl, Double rating, String specifications) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imgUrl = imgUrl;
        this.rating = rating;
        this.specifications = specifications;
    }

    public ProductDTO(Product entity) {
        id = entity.getId();
        name = entity.getName();
        description = entity.getDescription();
        price = entity.getPrice();
        imgUrl = entity.getImgUrl();
        rating = entity.getRating();
        specifications = entity.getSpecifications();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public Double getRating() {
        return rating;
    }

    public String getSpecifications() {
        return specifications;
    }
}