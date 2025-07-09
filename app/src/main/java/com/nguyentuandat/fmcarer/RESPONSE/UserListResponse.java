package com.nguyentuandat.fmcarer.RESPONSE;

import java.util.List;

public class UserListResponse {
    private boolean success;
    private String message;
    private List<UserResponse.UserData> users;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<UserResponse.UserData> getUsers() {
        return users;
    }

    // Setters (nếu cần)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUsers(List<UserResponse.UserData> users) {
        this.users = users;
    }
}
