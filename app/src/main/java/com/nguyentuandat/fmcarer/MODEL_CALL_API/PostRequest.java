package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PostRequest {

    @SerializedName("userId") // Khớp với id_user ở backend
    private String userId;

    // ✅ Đã loại bỏ userName và userAvatar vì backend sẽ tự lấy từ User model
    // private String userName;
    // private String userAvatar;

    @SerializedName("content")
    private String content;

    @SerializedName("selectedVisibility") // Đảm bảo khớp với tên trường backend mong đợi
    private String selectedVisibility;

    @SerializedName("mediaUrls") // Khớp với media_urls ở backend
    private List<String> mediaUrls;

    public PostRequest(String userId, String content, String selectedVisibility, List<String> mediaUrls) {
        this.userId = userId;
        this.content = content;
        this.selectedVisibility = selectedVisibility;
        this.mediaUrls = mediaUrls;
    }

    // --- Getters & Setters ---
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // ✅ Đã loại bỏ Getters & Setters cho userName và userAvatar
    /*
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    */

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSelectedVisibility() {
        return selectedVisibility;
    }

    public void setSelectedVisibility(String selectedVisibility) {
        this.selectedVisibility = selectedVisibility;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }
}