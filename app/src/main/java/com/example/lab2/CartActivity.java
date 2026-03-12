package com.example.lab2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lab2.data.DataStore;
import com.example.lab2.databinding.ActivityCartBinding;
import com.example.lab2.ui.CartAdapter;

public class CartActivity extends AppCompatActivity {
    private ActivityCartBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCart.setAdapter(new CartAdapter(DataStore.getCartLines()));
        binding.tvTotal.setText(DataStore.getCartTotalRub() + "₽");

        binding.btnOrder.setOnClickListener(v -> Toast.makeText(this, "Заказ оформлен (заглушка)", Toast.LENGTH_SHORT).show());

        binding.ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.ivLocation.setOnClickListener(v -> startActivity(new Intent(this, AddressActivity.class)));
        binding.btnBack.setOnClickListener(v -> finish());
    }
}

