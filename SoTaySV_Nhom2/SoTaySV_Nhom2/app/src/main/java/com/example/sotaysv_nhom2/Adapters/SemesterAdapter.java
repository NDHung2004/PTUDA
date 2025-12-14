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

public class SemesterAdapter extends RecyclerView.Adapter<SemesterAdapter.SemesterViewHolder> {

    private List<String> listSemesters;
    private int selectedPosition = 0;
    private OnSemesterClickListener listener;

    public interface OnSemesterClickListener {
        void onSemesterClick(int position);
    }

    public SemesterAdapter(List<String> listSemesters, OnSemesterClickListener listener) {
        this.listSemesters = listSemesters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SemesterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_semester, parent, false);
        return new SemesterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SemesterViewHolder holder, int position) {
        holder.tvName.setText(listSemesters.get(position));

        if (selectedPosition == position) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#2196F3"));
            holder.tvName.setTextColor(Color.WHITE);
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
            holder.tvName.setTextColor(Color.parseColor("#757575"));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousItem = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousItem);
            notifyItemChanged(selectedPosition);
            listener.onSemesterClick(selectedPosition);
        });
    }

    @Override
    public int getItemCount() { return listSemesters.size(); }

    public class SemesterViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CardView cardView;
        public SemesterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_semester_name);
            cardView = itemView.findViewById(R.id.card_semester);
        }
    }
}