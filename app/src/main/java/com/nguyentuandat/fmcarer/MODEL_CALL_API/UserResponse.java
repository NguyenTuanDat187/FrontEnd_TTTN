package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    private boolean success;
    private String message;
    private UserData user;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserData getUser() {
        return user;
    }

    public static class UserData {
        @SerializedName("_id")
        private String id;

        private String email;
        private String role;

        @SerializedName("isVerified")
        private boolean verified;

        private String fullname;
        private String numberphone;
        private String image;

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
    }

}
