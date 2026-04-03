package com.example.lab2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab2.R;
import com.example.lab2.databinding.ItemProductGridBinding;
import com.example.lab2.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface Listener {
        void onOpen(Product product);

        void onAdd(Product product, int position);
    }

    public interface InCartChecker {
        boolean isInCart(String productId);
    }

    private final List<Product> items;
    private final Listener listener;
    private final InCartChecker inCartChecker;

    public ProductAdapter(List<Product> items, Listener listener, InCartChecker inCartChecker) {
        this.items = items;
        this.listener = listener;
        this.inCartChecker = inCartChecker;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductGridBinding binding = ItemProductGridBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product product = items.get(position);
        holder.binding.ivProduct.setImageResource(product.getImageResId());
        holder.binding.tvPrice.setText(product.getPriceRub() + "₽");

        boolean inCart = inCartChecker.isInCart(product.getId());
        holder.binding.btnAdd.setImageResource(inCart ? R.drawable.ic_check : R.drawable.ic_plus);
        holder.binding.btnAdd.setEnabled(!inCart);

        View.OnClickListener open = v -> listener.onOpen(product);
        holder.binding.getRoot().setOnClickListener(open);
        holder.binding.ivProduct.setOnClickListener(open);

        holder.binding.btnAdd.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            listener.onAdd(product, adapterPosition);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemProductGridBinding binding;

        VH(ItemProductGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

