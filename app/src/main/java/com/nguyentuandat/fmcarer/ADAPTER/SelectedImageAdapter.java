package com.nguyentuandat.fmcarer.ADAPTER;

import android.content.Context;
import android.net.Uri; // Quan trọng: import Uri
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyentuandat.fmcarer.R; // Đảm bảo đúng package R của bạn

import java.util.List;

public class SelectedImageAdapter extends RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder> {

    private final Context context;
    private final List<Uri> imageUris; // Danh sách các Uri của ảnh

    // Constructor nhận Context và danh sách Uri ảnh
    public SelectedImageAdapter(Context context, List<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho mỗi item ảnh
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);

        // Sử dụng Glide để tải ảnh từ Uri vào ImageView
        Glide.with(context)
                .load(imageUri)
                .centerCrop() // Cắt ảnh để vừa với ImageView
                .placeholder(R.drawable.congdong) // Ảnh placeholder khi đang tải (tùy chọn)
                .error(R.drawable.caidat) // Ảnh lỗi nếu không tải được (tùy chọn)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUris.size(); // Trả về số lượng ảnh trong danh sách
    }

    // ViewHolder class để giữ các view của mỗi item ảnh
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ImageView trong item layout
            imageView = itemView.findViewById(R.id.imageViewSelected); // ID của ImageView trong item_selected_image_preview.xml
        }
    }
}