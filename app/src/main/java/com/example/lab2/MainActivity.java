package com.example.lab2;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.lab2.data.DataStore;
import com.example.lab2.databinding.ActivityMainBinding;
import com.example.lab2.ui.ProductAdapter;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new ProductAdapter(DataStore.getProducts(), new ProductAdapter.Listener() {
            @Override
            public void onOpen(com.example.lab2.model.Product product) {
                Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                intent.putExtra(ProductActivity.EXTRA_PRODUCT_ID, product.getId());
                startActivity(intent);
            }

            @Override
            public void onAdd(com.example.lab2.model.Product product, int position) {
                DataStore.addToCart(product.getId());
                Toast.makeText(MainActivity.this, "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
            }
        });

        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setAdapter(adapter);

        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        binding.ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.ivLocation.setOnClickListener(v -> startActivity(new Intent(this, AddressActivity.class)));
    }
}