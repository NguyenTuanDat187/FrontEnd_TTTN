package com.nguyentuandat.fmcarer.MODEL_CALL_API;

public class UserRequest {
    private String email;
    private String password;

    public UserRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setter (nếu cần)
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
