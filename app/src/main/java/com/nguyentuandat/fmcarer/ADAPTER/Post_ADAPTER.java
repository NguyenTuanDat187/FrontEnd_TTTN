package com.nguyentuandat.fmcarer.ADAPTER;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.nguyentuandat.fmcarer.MODEL.Post;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ApiResponse; // Đảm bảo lớp ApiResponse tồn tại và đúng

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Post_ADAPTER extends RecyclerView.Adapter<Post_ADAPTER.PostViewHolder> {

    private final Context context;
    private final List<Post> postList;
    private final ApiService apiService;
    private final String currentUserId;

    public Post_ADAPTER(Context context, List<Post> postList, ApiService apiService) {
        this.context = context;
        this.postList = postList;
        this.apiService = apiService;

        // Lấy ID người dùng hiện tại từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("USER", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("_id", "");
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho từng item bài viết
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Đặt nội dung cho TextViews, sử dụng getSafe để tránh null/empty string
        holder.tvUserName.setText(getSafe(post.getFullname(), "Ẩn danh"));
        holder.tvContent.setText(getSafe(post.getContent(), ""));
        holder.tvCreatedAt.setText(formatDate(post.getCreated_at()));
        holder.tvVisibility.setText("Chế độ: " + convertVisibility(post.getVisibility()));

        // Tải ảnh avatar của người đăng bài một cách an toàn
        String avatarUrl = post.getImage(); // Đây là URL avatar của người dùng đăng bài
        if (avatarUrl == null || avatarUrl.equals("null") || avatarUrl.trim().isEmpty()) {
            holder.imgAvatar.setImageResource(R.drawable.taikhoan); // Đặt ảnh mặc định nếu URL không hợp lệ
        } else {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.taikhoan) // Ảnh placeholder khi đang tải
                    .error(R.drawable.taikhoan) // Ảnh lỗi nếu không tải được
                    .transition(DrawableTransitionOptions.withCrossFade()) // Hiệu ứng chuyển tiếp mượt mà
                    .into(holder.imgAvatar);
        }

        // Hiển thị danh sách ảnh trong bài viết (nếu có)
        if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
            holder.rvPostImages.setVisibility(View.VISIBLE); // Hiển thị RecyclerView ảnh
            // Khởi tạo và thiết lập PostImageAdapter cho RecyclerView con
            PostImageAdapter postImageAdapter = new PostImageAdapter(context, post.getMediaUrls());
            holder.rvPostImages.setAdapter(postImageAdapter);
        } else {
            holder.rvPostImages.setVisibility(View.GONE); // Ẩn RecyclerView nếu không có ảnh
        }

        // Đặt OnLongClickListener chỉ khi bài viết thuộc về người dùng hiện tại
        if (currentUserId.equals(post.getUserId())) {
            holder.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(post, position); // Hiển thị tùy chọn Sửa/Xóa
                return true; // Xử lý sự kiện long click
            });
        } else {
            holder.itemView.setOnLongClickListener(null); // Gỡ bỏ listener nếu không phải chủ bài viết
        }
    }

    @Override
    public int getItemCount() {
        return postList.size(); // Trả về số lượng bài viết trong danh sách
    }

    // Hiển thị hộp thoại tùy chọn (Sửa/Xóa)
    private void showOptionsDialog(Post post, int position) {
        String[] options = {"Sửa bài viết", "Xóa bài viết"};

        new AlertDialog.Builder(context)
                .setTitle("Tùy chọn bài viết")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(context, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                    } else {
                        confirmDelete(post.get_id(), position); // Xác nhận xóa
                    }
                })
                .show();
    }

    // Hiển thị hộp thoại xác nhận xóa
    private void confirmDelete(String postId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> deletePost(postId, position)) // Gọi hàm xóa bài viết
                .setNegativeButton("Hủy", null) // Không làm gì khi hủy
                .show();
    }

    // Gửi yêu cầu xóa bài viết lên API
    private void deletePost(String postId, int position) {
        apiService.deletePost(postId, currentUserId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    postList.remove(position); // Xóa bài viết khỏi danh sách
                    notifyItemRemoved(position); // Thông báo cho Adapter để cập nhật RecyclerView
                    Toast.makeText(context, "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Xóa bài viết thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Chuyển đổi chế độ hiển thị từ tiếng Anh sang tiếng Việt
    private String convertVisibility(String raw) {
        if (raw == null) return "Không xác định";
        switch (raw) {
            case "private": return "Riêng tư";
            case "family": return "Gia đình";
            case "public": return "Cộng đồng";
            default: return "Không xác định";
        }
    }

    // Hàm tiện ích để trả về chuỗi an toàn (tránh null hoặc rỗng)
    private String getSafe(String input, String fallback) {
        return input != null && !input.trim().isEmpty() ? input : fallback;
    }

    // Định dạng lại ngày/giờ từ chuỗi ISO 8601 sang định dạng dễ đọc
    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "";
        try {
            // Parser cho định dạng ISO 8601
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = parser.parse(rawDate);
            // Formatter cho định dạng mong muốn
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return formatter.format(date);
        } catch (Exception e) {
            return rawDate; // Trả về chuỗi gốc nếu có lỗi parse
        }
    }

    // ViewHolder class để giữ các view của mỗi item bài viết
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUserName, tvContent, tvCreatedAt, tvVisibility;
        RecyclerView rvPostImages; // RecyclerView để hiển thị các ảnh của bài viết

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view từ item_post.xml
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvVisibility = itemView.findViewById(R.id.tvVisibility);
            // Ánh xạ RecyclerView cho ảnh bài viết (Đảm bảo ID này khớp với XML: recyclerImages)
            rvPostImages = itemView.findViewById(R.id.recyclerImages);

            // Cấu hình LayoutManager cho RecyclerView ảnh bài viết
            // Sử dụng LinearLayoutManager theo chiều ngang để cuộn ảnh
            rvPostImages.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        }
    }
}