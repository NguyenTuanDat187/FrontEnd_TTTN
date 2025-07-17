// com.nguyentuandat.fmcarer.MODEL.Post.java

package com.nguyentuandat.fmcarer.MODEL;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Post {
    @SerializedName("_id")
    private String _id;

    // RẤT QUAN TRỌNG: Đây phải là đối tượng User, không phải String
    // và tên trường JSON phải khớp với tên bạn gửi từ backend (ví dụ: "user")
    @SerializedName("user") // Đây là tên trường mà controller backend đã sửa để trả về
    private User user; // Đảm bảo đây là đối tượng User

    @SerializedName("fullname") // Có thể giữ lại làm fallback hoặc cho mục đích hiển thị nhanh
    private String fullname;

    @SerializedName("image") // Có thể giữ lại làm fallback hoặc cho mục đích hiển thị nhanh
    private String image;

    @SerializedName("content")
    private String content;

    @SerializedName("media_urls")
    private List<String> mediaUrls;

    @SerializedName("visibility")
    private String visibility;

    @SerializedName("status")
    private String status; // "pending", "active", "rejected"

    @SerializedName("rejectionReason")
    private String rejectionReason;

    @SerializedName("created_at")
    private String created_at; // Date string

    @SerializedName("updated_at")
    private String updated_at; // Date string

    // Constructor (optional, depending on your needs for object creation)
    public Post(String _id, User user, String fullname, String image, String content, List<String> mediaUrls, String visibility, String status, String rejectionReason, String created_at, String updated_at) {
        this._id = _id;
        this.user = user;
        this.fullname = fullname;
        this.image = image;
        this.content = content;
        this.mediaUrls = mediaUrls;
        this.visibility = visibility;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // Getters
    public String get_id() {
        return _id;
    }

    public User getUser() { // Getter cho đối tượng User
        return user;
    }

    public String getFullname() {
        return fullname;
    }

    public String getImage() {
        return image;
    }

    public String getContent() {
        return content;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    // Setters (if needed, but usually not for received data)
    // ...
}