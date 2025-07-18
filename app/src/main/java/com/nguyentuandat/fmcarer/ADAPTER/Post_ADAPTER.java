package com.nguyentuandat.fmcarer.ADAPTER;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
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
import com.nguyentuandat.fmcarer.MODEL.User;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.RESPONSE.ApiResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Post_ADAPTER extends RecyclerView.Adapter<Post_ADAPTER.PostViewHolder> {

    private static final String TAG = "Post_ADAPTER";

    private final Context context;
    private final List<Post> postList;
    private final ApiService apiService;
    private final String currentUserId;

    private static final String PREF_USER_SESSION = "user_session";
    private static final String KEY_USER_ID = "_id";

    public Post_ADAPTER(Context context, List<Post> postList, ApiService apiService) {
        this.context = context;
        this.postList = postList;
        this.apiService = apiService;

        SharedPreferences prefs = context.getSharedPreferences(PREF_USER_SESSION, Context.MODE_PRIVATE);
        currentUserId = prefs.getString(KEY_USER_ID, "");
        Log.d(TAG, "Post_ADAPTER initialized. Current User ID: " + currentUserId);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        Log.d(TAG, "onCreateViewHolder: Creating new ViewHolder.");
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        if (position >= postList.size() || position < 0) {
            Log.e(TAG, "onBindViewHolder: Invalid position: " + position + ". List size: " + postList.size());
            return;
        }

        Post post = postList.get(position);
        Log.d(TAG, "onBindViewHolder: Binding post at position " + position + ". Post ID: " + post.get_id());

        // Lấy thông tin người tạo bài viết từ trường 'user' đã được populate
        User postCreator = post.getUser();

        // Lấy fullname và image an toàn, ƯU TIÊN TỪ ĐỐI TƯỢNG USER ĐÃ POPULATE
        String displayUserName;
        String displayUserAvatarUrl;

        if (postCreator != null) {
            displayUserName = getSafe(postCreator.getFullname(), postCreator.getFullname()); // Ưu tiên fullname, nếu không có thì username
            if (displayUserName.isEmpty()) { // Nếu fullname và username đều rỗng, thử email
                displayUserName = getSafe(postCreator.getEmail(), "Ẩn danh");
            }
            displayUserAvatarUrl = getSafe(postCreator.getImage(), ""); // Lấy image từ User object
        } else {
            // Fallback nếu postCreator là null (có thể do lỗi populate hoặc data cũ)
            Log.w(TAG, "onBindViewHolder: Post creator (User object) is null for post ID: " + post.get_id() + ". Using direct fields as fallback.");
            displayUserName = getSafe(post.getFullname(), "Ẩn danh");
            displayUserAvatarUrl = getSafe(post.getImage(), "");
        }

        Log.d(TAG, "onBindViewHolder: Display User Name: " + displayUserName + ", Display Avatar URL: " + displayUserAvatarUrl);

        holder.tvUserName.setText(displayUserName);
        holder.tvContent.setText(getSafe(post.getContent(), ""));
        holder.tvCreatedAt.setText(formatDate(post.getCreated_at()));

        String statusText;
        int statusColor;

        String postStatus = post.getStatus();
        if (postStatus == null) {
            Log.w(TAG, "onBindViewHolder: Post status is null for post ID: " + post.get_id() + ". Defaulting to active for display.");
            postStatus = "active";
        }

        switch (postStatus) {
            case "pending":
                statusText = "Đang chờ duyệt";
                statusColor = Color.parseColor("#FFA500"); // Màu cam
                break;
            case "rejected":
                statusText = "Bị từ chối";
                statusColor = Color.RED;
                String rejectionReason = getSafe(post.getRejectionReason(), "");
                if (!rejectionReason.isEmpty()) {
                    statusText += " (" + rejectionReason + ")";
                    Log.d(TAG, "onBindViewHolder: Post rejected with reason: " + rejectionReason);
                }
                break;
            case "active":
            default:
                statusText = "Đã đăng";
                statusColor = Color.parseColor("#4CAF50"); // Màu xanh lá
                break;
        }
        Log.d(TAG, "onBindViewHolder: Post Status: " + postStatus + ", Display Text: " + statusText);

        String visibilityConverted = convertVisibility(post.getVisibility());
        holder.tvVisibility.setText("Chế độ: " + visibilityConverted + " (" + statusText + ")");
        holder.tvVisibility.setTextColor(statusColor);

        // Load avatar image
        if (displayUserAvatarUrl.isEmpty() || displayUserAvatarUrl.equals("null")) {
            holder.imgAvatar.setImageResource(R.drawable.taikhoan);
            Log.d(TAG, "onBindViewHolder: Using default avatar for post ID: " + post.get_id());
        } else {
            Glide.with(context)
                    .load(displayUserAvatarUrl)
                    .placeholder(R.drawable.taikhoan)
                    .error(R.drawable.taikhoan)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imgAvatar);
            Log.d(TAG, "onBindViewHolder: Glide loading avatar from: " + displayUserAvatarUrl);
        }

        // Handle media URLs RecyclerView
        if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
            holder.rvPostImages.setVisibility(View.VISIBLE);
            if (context != null) {
                PostImageAdapter postImageAdapter = new PostImageAdapter(context, post.getMediaUrls());
                holder.rvPostImages.setAdapter(postImageAdapter);
                Log.d(TAG, "onBindViewHolder: Post has " + post.getMediaUrls().size() + " media URLs.");
            } else {
                Log.e(TAG, "onBindViewHolder: Context is null, cannot set up PostImageAdapter.");
                holder.rvPostImages.setVisibility(View.GONE);
            }
        } else {
            holder.rvPostImages.setVisibility(View.GONE);
            Log.d(TAG, "onBindViewHolder: Post has no media URLs.");
        }

        // Ownership check for long click to edit/delete
        String creatorId = (postCreator != null) ? postCreator.getId() : null;
        Log.d(TAG, "onBindViewHolder: Current User ID: " + currentUserId + ", Post Creator ID: " + creatorId);

        // Chỉ cho phép long click nếu người dùng hiện tại là người tạo bài viết
        if (currentUserId != null && creatorId != null && currentUserId.equals(creatorId)) {
            holder.itemView.setOnLongClickListener(v -> {
                Log.d(TAG, "onBindViewHolder: Long click enabled for own post ID: " + post.get_id());
                showOptionsDialog(post, position);
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
            Log.d(TAG, "onBindViewHolder: Long click disabled for non-own post ID: " + post.get_id());
        }
    }

    @Override
    public int getItemCount() {
        int size = postList.size();
        Log.d(TAG, "getItemCount: Returning " + size + " items.");
        return size;
    }

    private void showOptionsDialog(Post post, int position) {
        Log.d(TAG, "showOptionsDialog: Showing options for post ID: " + post.get_id());
        // Có thể thêm tùy chọn "Sửa" và xử lý theo trạng thái bài viết (ví dụ: không cho sửa khi đang chờ duyệt)
        String[] options = {"Sửa bài viết", "Xóa bài viết"};

        new AlertDialog.Builder(context)
                .setTitle("Tùy chọn bài viết")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(context, "Tính năng sửa đang phát triển", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "showOptionsDialog: 'Sửa bài viết' selected for post ID: " + post.get_id());
                    } else {
                        confirmDelete(post.get_id(), position);
                        Log.i(TAG, "showOptionsDialog: 'Xóa bài viết' selected for post ID: " + post.get_id());
                    }
                })
                .show();
    }

    private void confirmDelete(String postId, int position) {
        Log.d(TAG, "confirmDelete: Confirming delete for post ID: " + postId);
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> deletePost(postId, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deletePost(String postId, int position) {
        Log.d(TAG, "deletePost: Attempting to delete post ID: " + postId + " at position " + position);
        apiService.deletePost(postId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.i(TAG, "deletePost: Post deleted successfully. Post ID: " + postId);
                    postList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Xóa bài viết thất bại: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += " - " + errorBody;
                            Log.e(TAG, "deletePost: API Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "deletePost: Error reading errorBody", e);
                    }
                    Log.e(TAG, "deletePost: " + errorMsg);
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "deletePost: API Call Failure: " + t.getMessage(), t);
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String convertVisibility(String raw) {
        String result;
        if (raw == null || raw.trim().isEmpty()) {
            result = "Không xác định";
        } else {
            switch (raw) {
                case "private":
                    result = "Riêng tư";
                    break;
                case "public":
                    result = "Công khai";
                    break;
                case "friends":
                    result = "Bạn bè"; // Hoặc "Gia đình" nếu bạn muốn hiển thị rõ hơn
                    break;
                case "community":
                    result = "Cộng đồng";
                    break;
                default:
                    result = "Không xác định";
                    break;
            }
        }
        Log.d(TAG, "convertVisibility: Raw: '" + raw + "' -> Converted: '" + result + "'");
        return result;
    }

    private String getSafe(String input, String fallback) {
        return (input != null && !input.trim().isEmpty()) ? input.trim() : fallback;
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            Log.w(TAG, "formatDate: Raw date is null or empty.");
            return "";
        }

        // Các định dạng có thể xuất hiện từ MongoDB ISODate
        SimpleDateFormat[] parsers = {
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()),
        };

        Date date = null;
        for (SimpleDateFormat parser : parsers) {
            try {
                parser.setLenient(false);
                parser.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); // Parse đúng múi giờ gốc là UTC
                date = parser.parse(rawDate);
                break;
            } catch (ParseException ignored) {
            }
        }

        if (date != null) {
            // Hiển thị theo giờ Việt Nam
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            formatter.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh")); // Chuyển sang giờ VN
            String formatted = formatter.format(date);
            Log.d(TAG, "formatDate: Raw: '" + rawDate + "' -> Formatted (VN): '" + formatted + "'");
            return formatted;
        } else {
            Log.e(TAG, "formatDate: Không parse được ngày '" + rawDate + "'");
            return rawDate;
        }
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUserName, tvContent, tvCreatedAt, tvVisibility;
        RecyclerView rvPostImages;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvVisibility = itemView.findViewById(R.id.tvVisibility);
            rvPostImages = itemView.findViewById(R.id.recyclerImages);
            if (itemView.getContext() != null) {
                rvPostImages.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            } else {
                Log.e(TAG, "PostViewHolder: Context is null, cannot set up RecyclerView LayoutManager.");
            }
            Log.d(TAG, "PostViewHolder: Views initialized.");
        }
    }
}