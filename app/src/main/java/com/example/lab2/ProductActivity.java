package com.example.lab2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab2.data.DeliveryRepository;
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
        if (productId == null) {
            finish();
            return;
        }

        DeliveryRepository.get().loadProduct(productId, new DeliveryRepository.ResultCallback<Product>() {
            @Override
            public void onSuccess(Product p) {
                product = p;
                bindProductUi();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProductActivity.this, message, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        binding.ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.ivLocation.setOnClickListener(v -> startActivity(new Intent(this, AddressActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (product != null) {
            DeliveryRepository.get().refreshCart(this::updateAddButtonVisibility, null);
        }
    }

    private void bindProductUi() {
        binding.ivLeft.setImageResource(product.getImageResId());
        binding.ivRight.setImageResource(product.getImageResId());
        binding.tvName.setText(product.getName());
        binding.tvPrice.setText(product.getPriceRub() + "₽");
        binding.tvDescription.setText(product.getDescription());

        updateAddButtonVisibility();

        binding.btnAdd.setOnClickListener(v -> DeliveryRepository.get().addToCart(product.getId(),
                new DeliveryRepository.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ProductActivity.this, "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                        updateAddButtonVisibility();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ProductActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }));
    }

    private void updateAddButtonVisibility() {
        if (product == null) {
            return;
        }
        if (DeliveryRepository.get().isInCart(product.getId())) {
            binding.btnAdd.setVisibility(android.view.View.GONE);
        } else {
            binding.btnAdd.setVisibility(android.view.View.VISIBLE);
        }
    }
}
