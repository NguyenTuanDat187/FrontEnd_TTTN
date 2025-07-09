package com.nguyentuandat.fmcarer.RESPONSE;

import com.google.gson.annotations.SerializedName;

public class UserResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private UserData user;

    @SerializedName("imageUrl") // (nếu dùng cho upload avatar)
    private String imageUrl;

    @SerializedName("token") // ✅ Quan trọng: để nhận token
    private String accessToken;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserData getUser() {
        return user;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    // ✅ Lớp con chứa dữ liệu người dùng
    public static class UserData {
        @SerializedName("_id")
        private String id;

        @SerializedName("email")
        private String email;

        @SerializedName("role")
        private String role;

        @SerializedName("isVerified")
        private boolean verified;

        @SerializedName("fullname")
        private String fullname;

        @SerializedName("numberphone")
        private String numberphone;

        @SerializedName("image")
        private String image;

        @SerializedName("created_by")
        private String createdBy;

        // Getters
        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public boolean isVerified() {
            return verified;
        }

        public String getFullname() {
            return fullname;
        }

        public String getNumberphone() {
            return numberphone;
        }

        public String getImage() {
            return image;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        // Setters
        public void setId(String id) {
            this.id = id;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public void setNumberphone(String numberphone) {
            this.numberphone = numberphone;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }
    }
}
