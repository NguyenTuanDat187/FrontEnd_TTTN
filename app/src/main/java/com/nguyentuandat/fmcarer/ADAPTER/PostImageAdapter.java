package com.nguyentuandat.fmcarer.ADAPTER;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.nguyentuandat.fmcarer.R;

import java.util.List;

public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.PostImageViewHolder> {

    private final Context context;
    private final List<String> imageUrls; // Danh sách các URL ảnh

    public PostImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public PostImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_post_image cho mỗi item ảnh
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_image, parent, false);
        return new PostImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position); // Lấy URL ảnh
        Glide.with(context)
                .load(imageUrl)
                .centerCrop() // Hoặc fitCenter, tùy theo mong muốn hiển thị
                .placeholder(R.drawable.taikhoan) // Ảnh placeholder
                .error(R.drawable.caidat) // Ảnh hiển thị khi tải lỗi
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size(); // Trả về số lượng ảnh
    }

    public static class PostImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PostImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewPost); // Ánh xạ đúng ID trong item_post_image.xml
        }
    }
}