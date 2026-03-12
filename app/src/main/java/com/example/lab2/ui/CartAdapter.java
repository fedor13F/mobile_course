package com.example.lab2.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab2.databinding.ItemCartRowBinding;
import com.example.lab2.model.CartLine;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {
    private final List<CartLine> items;

    public CartAdapter(List<CartLine> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartRowBinding binding = ItemCartRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartLine line = items.get(position);
        holder.binding.tvName.setText(line.getProduct().getName().toLowerCase(Locale.getDefault()));
        holder.binding.tvQty.setText(String.valueOf(line.getQuantity()));
        holder.binding.tvPrice.setText(String.valueOf(line.getLineTotalRub()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemCartRowBinding binding;

        VH(ItemCartRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

