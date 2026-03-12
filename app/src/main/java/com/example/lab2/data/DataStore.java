package com.example.lab2.data;

import com.example.lab2.R;
import com.example.lab2.model.CartLine;
import com.example.lab2.model.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataStore {
    private static final List<Product> PRODUCTS = new ArrayList<>();
    private static final Map<String, Integer> CART = new LinkedHashMap<>();

    static {
        PRODUCTS.add(new Product(
                "milk_1l",
                "Молоко 1л",
                100,
                "Здесь дополнительное описание товара",
                R.drawable.ic_product
        ));
        PRODUCTS.add(new Product(
                "kefir_1l",
                "Кефир 1л",
                129,
                "Здесь дополнительное описание товара",
                R.drawable.ic_product
        ));
        PRODUCTS.add(new Product(
                "bread",
                "Хлеб",
                55,
                "Здесь дополнительное описание товара",
                R.drawable.ic_product
        ));
        PRODUCTS.add(new Product(
                "eggs",
                "Яйца 10шт",
                140,
                "Здесь дополнительное описание товара",
                R.drawable.ic_product
        ));
        PRODUCTS.add(new Product(
                "cheese",
                "Сыр 200г",
                260,
                "Здесь дополнительное описание товара",
                R.drawable.ic_product
        ));
        PRODUCTS.add(new Product(
                "apples",
                "Яблоки 1кг",
                120,
                "Здесь дополнительное описание товара",
                R.drawable.ic_product
        ));

        // Статическая корзина как на макете
        CART.put("milk_1l", 1);
        CART.put("kefir_1l", 2);
    }

    private DataStore() {
    }

    public static List<Product> getProducts() {
        return new ArrayList<>(PRODUCTS);
    }

    public static Product getProductById(String id) {
        for (Product product : PRODUCTS) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    public static void addToCart(String productId) {
        Integer qty = CART.get(productId);
        if (qty == null) qty = 0;
        CART.put(productId, qty + 1);
    }

    public static boolean isInCart(String productId) {
        Integer qty = CART.get(productId);
        return qty != null && qty > 0;
    }

    public static List<CartLine> getCartLines() {
        List<CartLine> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : CART.entrySet()) {
            Product product = getProductById(entry.getKey());
            if (product == null) continue;
            lines.add(new CartLine(product, entry.getValue()));
        }
        return lines;
    }

    public static int getCartTotalRub() {
        int sum = 0;
        for (CartLine line : getCartLines()) {
            sum += line.getLineTotalRub();
        }
        return sum;
    }
}

