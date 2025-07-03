package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PostRequest {

    @SerializedName("userId") // Đảm bảo khớp với tên trường backend mong đợi
    private String userId;

    // Đã loại bỏ fullname và image vì backend sẽ tự lấy từ User model
    // private String fullname;
    // private String image;

    @SerializedName("content") // Đảm bảo khớp với tên trường backend mong đợi
    private String content;

    @SerializedName("selectedVisibility") // Đảm bảo khớp với tên trường backend mong đợi
    private String selectedVisibility; // Đổi từ 'visibility' sang 'selectedVisibility' nếu đây là tên bạn đang gửi từ Android

    @SerializedName("mediaUrls") // Đảm bảo khớp với tên trường backend mong đợi ('media_urls' trong Post model, nhưng 'mediaUrls' khi gửi từ Android)
    private List<String> mediaUrls;

    // Constructor mới: KHÔNG CÓ fullname VÀ image
    public PostRequest(String userId, String content, String selectedVisibility, List<String> mediaUrls) {
        this.userId = userId;
        this.content = content;
        this.selectedVisibility = selectedVisibility;
        this.mediaUrls = mediaUrls;
    }

    // --- Getter & Setter cho các trường còn lại ---
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Đã loại bỏ Getter/Setter cho fullname và image

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSelectedVisibility() { // Đổi tên getter để khớp với selectedVisibility
        return selectedVisibility;
    }

    public void setSelectedVisibility(String selectedVisibility) { // Đổi tên setter
        this.selectedVisibility = selectedVisibility;
    }

    public List<String> getMediaUrls() { // Đổi tên getter để khớp với mediaUrls
        return mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) { // Đổi tên setter
        this.mediaUrls = mediaUrls;
    }
}