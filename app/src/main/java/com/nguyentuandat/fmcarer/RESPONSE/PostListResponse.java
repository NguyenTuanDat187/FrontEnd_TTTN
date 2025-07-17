package com.nguyentuandat.fmcarer.RESPONSE;

import com.google.gson.annotations.SerializedName;
import com.nguyentuandat.fmcarer.MODEL.Post;

import java.util.List;

public class PostListResponse {
    @SerializedName("success") // Khớp với trường 'success' trong JSON
    private boolean success;

    @SerializedName("message") // Khớp với trường 'message' trong JSON
    private String message;

    @SerializedName("posts") // Khớp với tên key chứa mảng bài viết trong JSON
    private List<Post> posts;

    // Constructor mặc định (Gson yêu cầu)
    public PostListResponse() {
    }

    // Getters cho các trường
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Post> getPosts() {
        return posts;
    }

    // Setters (thường không cần thiết cho lớp response, nhưng có thể thêm nếu muốn)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}