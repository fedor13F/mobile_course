package com.example.lab2;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab2.databinding.ActivityAddressBinding;

public class AddressActivity extends AppCompatActivity {
    private ActivityAddressBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnDone.setOnClickListener(v -> {
            Toast.makeText(this, "Адрес выбран (заглушка)", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

