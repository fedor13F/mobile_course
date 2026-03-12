package com.example.lab2.model;

public class CartLine {
    private final Product product;
    private final int quantity;

    public CartLine(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getLineTotalRub() {
        return product.getPriceRub() * quantity;
    }
}

