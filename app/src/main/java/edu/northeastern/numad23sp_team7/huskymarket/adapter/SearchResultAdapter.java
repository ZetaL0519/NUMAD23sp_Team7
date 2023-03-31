package edu.northeastern.numad23sp_team7.huskymarket.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.northeastern.numad23sp_team7.databinding.ItemSearchResultCardBinding;
import edu.northeastern.numad23sp_team7.huskymarket.model.Product;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    private ArrayList<Product> arr = new ArrayList<>();

    public SearchResultAdapter(ArrayList<Product> arr) {
        this.arr = arr;
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemSearchResultCardBinding binding = ItemSearchResultCardBinding.inflate(layoutInflater, parent, false);
        return new SearchResultViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        holder.binding.setProduct(arr.get(position));
    }

    @Override
    public int getItemCount() {
        return arr.size();
    }

    static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        ItemSearchResultCardBinding binding;

        public SearchResultViewHolder(@NonNull ItemSearchResultCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}