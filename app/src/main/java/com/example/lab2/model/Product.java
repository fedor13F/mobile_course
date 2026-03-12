package com.example.lab2.model;

public class Product {
    private final String id;
    private final String name;
    private final int priceRub;
    private final String description;
    private final int imageResId;

    public Product(String id, String name, int priceRub, String description, int imageResId) {
        this.id = id;
        this.name = name;
        this.priceRub = priceRub;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPriceRub() {
        return priceRub;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}

