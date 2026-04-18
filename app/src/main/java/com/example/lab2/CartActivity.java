package com.example.lab2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lab2.data.DeliveryRepository;
import com.example.lab2.databinding.ActivityCartBinding;
import com.example.lab2.model.CartLine;
import com.example.lab2.ui.CartAdapter;

import java.util.List;

public class CartActivity extends AppCompatActivity {
    private ActivityCartBinding binding;
    private String selectedAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));

        binding.btnOrder.setOnClickListener(v -> DeliveryRepository.get().loadSelectedAddress(
                new DeliveryRepository.ResultCallback<DeliveryRepository.AddressInfo>() {
                    @Override
                    public void onSuccess(DeliveryRepository.AddressInfo addressInfo) {
                        String address = selectedAddress != null
                                ? selectedAddress
                                : getString(R.string.default_delivery_address);
                        DeliveryRepository.get().checkout(
                                getString(R.string.default_customer_name),
                                address,
                                new DeliveryRepository.ResultCallback<String>() {
                                    @Override
                                    public void onSuccess(String orderId) {
                                        Toast.makeText(CartActivity.this, "Заказ оформлен: " + orderId, Toast.LENGTH_LONG).show();
                                        reloadCart();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Toast.makeText(CartActivity.this, message, Toast.LENGTH_LONG).show();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(CartActivity.this, "Адрес: " + message, Toast.LENGTH_LONG).show();
                    }
                }
        ));

        binding.ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.ivLocation.setOnClickListener(v -> startActivity(new Intent(this, AddressActivity.class)));
        binding.btnBack.setOnClickListener(v -> finish());

        reloadCart();
        reloadSelectedAddress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadCart();
        reloadSelectedAddress();
    }

    private void reloadCart() {
        DeliveryRepository.get().loadCartLines(new DeliveryRepository.ResultCallback<List<CartLine>>() {
            @Override
            public void onSuccess(List<CartLine> lines) {
                binding.rvCart.setAdapter(new CartAdapter(lines));
                int total = 0;
                for (CartLine line : lines) {
                    total += line.getLineTotalRub();
                }
                binding.tvTotal.setText(total + "₽");
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CartActivity.this, "Корзина: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void reloadSelectedAddress() {
        DeliveryRepository.get().loadSelectedAddress(new DeliveryRepository.ResultCallback<DeliveryRepository.AddressInfo>() {
            @Override
            public void onSuccess(DeliveryRepository.AddressInfo info) {
                if (info == null || info.label == null || info.label.trim().isEmpty()) {
                    selectedAddress = null;
                    binding.tvSelectedAddress.setText(getString(R.string.address_not_selected));
                    return;
                }
                selectedAddress = info.label;
                binding.tvSelectedAddress.setText(
                        getString(R.string.address_selected_prefix, selectedAddress)
                );
            }

            @Override
            public void onError(String message) {
                selectedAddress = null;
                binding.tvSelectedAddress.setText(getString(R.string.address_not_selected));
            }
        });
    }
}
