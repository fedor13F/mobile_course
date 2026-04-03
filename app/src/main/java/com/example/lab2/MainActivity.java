package com.example.lab2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.lab2.data.DeliveryRepository;
import com.example.lab2.databinding.ActivityMainBinding;
import com.example.lab2.model.Product;
import com.example.lab2.ui.ProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ProductAdapter adapter;
    private final ArrayList<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new ProductAdapter(products, new ProductAdapter.Listener() {
            @Override
            public void onOpen(Product product) {
                Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                intent.putExtra(ProductActivity.EXTRA_PRODUCT_ID, product.getId());
                startActivity(intent);
            }

            @Override
            public void onAdd(Product product, int position) {
                DeliveryRepository.get().addToCart(product.getId(), new DeliveryRepository.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, productId -> DeliveryRepository.get().isInCart(productId));

        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setAdapter(adapter);

        binding.btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        binding.ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.ivLocation.setOnClickListener(v -> startActivity(new Intent(this, AddressActivity.class)));

        DeliveryRepository.get().loadProducts(new DeliveryRepository.ResultCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> list) {
                products.clear();
                products.addAll(list);
                adapter.notifyDataSetChanged();
                DeliveryRepository.get().refreshCart(() -> adapter.notifyDataSetChanged(), null);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, "Не удалось загрузить каталог: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            DeliveryRepository.get().refreshCart(() -> adapter.notifyDataSetChanged(), null);
        }
    }
}
