package com.example.lab2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab2.data.DataStore;
import com.example.lab2.databinding.ActivityProductBinding;
import com.example.lab2.model.Product;

public class ProductActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUCT_ID = "product_id";

    private ActivityProductBinding binding;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        product = DataStore.getProductById(productId);
        if (product == null) {
            finish();
            return;
        }

        binding.ivLeft.setImageResource(product.getImageResId());
        binding.ivRight.setImageResource(product.getImageResId());
        binding.tvName.setText(product.getName());
        binding.tvPrice.setText(product.getPriceRub() + "₽");
        binding.tvDescription.setText(product.getDescription());

        if (DataStore.isInCart(product.getId())) {
            binding.btnAdd.setVisibility(android.view.View.GONE);
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAdd.setOnClickListener(v -> {
            DataStore.addToCart(product.getId());
            Toast.makeText(this, "Добавлено в корзину", Toast.LENGTH_SHORT).show();
            binding.btnAdd.setVisibility(android.view.View.GONE);
        });
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        binding.ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.ivLocation.setOnClickListener(v -> startActivity(new Intent(this, AddressActivity.class)));
    }
}

