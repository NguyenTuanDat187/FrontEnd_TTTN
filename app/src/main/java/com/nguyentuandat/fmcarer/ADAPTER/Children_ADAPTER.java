// ------------------- Children_ADAPTER.java -------------------
package com.nguyentuandat.fmcarer.ADAPTER;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Children_ADAPTER extends RecyclerView.Adapter<Children_ADAPTER.ChildViewHolder> {

    private Context context;
    private List<Children> childrenList = new ArrayList<>();
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnChildActionListener {
        void onEditChild(Children child);
        void onDeleteChild(Children child);
    }

    private OnChildActionListener listener;

    public void setOnChildActionListener(OnChildActionListener listener) {
        this.listener = listener;
    }

    public Children_ADAPTER(Context context) {
        this.context = context;
    }

    public void setData(List<Children> list) {
        this.childrenList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Children child = childrenList.get(position);

        holder.tvName.setText(child.getName());
        holder.tvGender.setText(child.getGender().equals("male") ? "Nam" :
                child.getGender().equals("female") ? "Nữ" : "Khác");

        try {
            Date date = inputFormat.parse(child.getDob());
            holder.tvChildDOB.setText("Ngày sinh: " + outputFormat.format(date));
        } catch (ParseException e) {
            holder.tvChildDOB.setText("Ngày sinh: " + child.getDob());
        }

        Glide.with(context)
                .load(child.getAvatar_url())
                .placeholder(R.drawable.taikhoan)
                .into(holder.imgAvatar);

        holder.Cardview_itemChild.setOnLongClickListener(v -> {
            if (listener != null) {
                new AlertDialog.Builder(context)
                        .setTitle("Chọn hành động")
                        .setItems(new CharSequence[]{"Sửa", "Xóa"}, (dialog, which) -> {
                            if (which == 0) listener.onEditChild(child);
                            else listener.onDeleteChild(child);
                        })
                        .show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return childrenList != null ? childrenList.size() : 0;
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGender, tvChildDOB;
        ImageView imgAvatar;
        CardView Cardview_itemChild;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChildName);
            tvGender = itemView.findViewById(R.id.tvChildGender);
            tvChildDOB = itemView.findViewById(R.id.tvChildDOB);
            imgAvatar = itemView.findViewById(R.id.imgChildAvatar);
            Cardview_itemChild = itemView.findViewById(R.id.Cardview_itemChild);
        }
    }
}
