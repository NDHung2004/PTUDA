package com.example.sotaysv_nhom2.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sotaysv_nhom2.R;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private List<String> listFilters;
    private int selectedPosition = 0; // Mặc định chọn cái đầu tiên
    private OnFilterClickListener listener;

    public interface OnFilterClickListener {
        void onFilterClick(int position);
    }

    public FilterAdapter(List<String> listFilters, OnFilterClickListener listener) {
        this.listFilters = listFilters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng lại layout item_semester.xml vì nó giống hệt nhau (Card bo tròn)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_semester, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        holder.tvName.setText(listFilters.get(position));

        if (selectedPosition == position) {
            // Đang chọn: Màu xanh, chữ trắng
            holder.cardView.setCardBackgroundColor(Color.parseColor("#2196F3"));
            holder.tvName.setTextColor(Color.WHITE);
        } else {
            // Không chọn: Màu xám nhạt
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
            holder.tvName.setTextColor(Color.parseColor("#757575"));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousItem = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousItem);
            notifyItemChanged(selectedPosition);
            listener.onFilterClick(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return listFilters.size();
    }

    public class FilterViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CardView cardView;

        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_semester_name);
            cardView = itemView.findViewById(R.id.card_semester);
        }
    }
}