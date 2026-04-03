package com.example.lab2.model;

import com.example.lab2.R;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static Product fromJson(JSONObject o) throws JSONException {
        return new Product(
                o.getString("id"),
                o.getString("name"),
                o.getInt("price_rub"),
                o.optString("description", ""),
                R.drawable.ic_product
        );
    }
}

