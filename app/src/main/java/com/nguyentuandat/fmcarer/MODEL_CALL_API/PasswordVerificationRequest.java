// app/src/main/java/com.nguyentuandat.fmcarer.MODEL_CALL_API/PasswordVerificationRequest.java
package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.google.gson.annotations.SerializedName;

public class PasswordVerificationRequest {
    @SerializedName("userId")
    private String userId;

    @SerializedName("password")
    private String password;

    public PasswordVerificationRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}