package com.nguyentuandat.fmcarer.RESPONSE;

import com.google.gson.annotations.SerializedName;
import com.nguyentuandat.fmcarer.MODEL.User;
import java.util.List;

public class UserListResponse {
    private boolean success;
    private String message;

    @SerializedName("subusers") // 🔥 Phải đúng với key JSON trả về
    private List<User> subusers;

    public UserListResponse() {}

    public UserListResponse(boolean success, String message, List<User> subusers) {
        this.success = success;
        this.message = message;
        this.subusers = subusers;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<User> getSubusers() { // 🔥 sửa lại getUsers -> getSubusers
        return subusers;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSubusers(List<User> subusers) {
        this.subusers = subusers;
    }
}
