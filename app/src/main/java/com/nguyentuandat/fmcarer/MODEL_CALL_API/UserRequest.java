package com.nguyentuandat.fmcarer.MODEL_CALL_API;

public class UserRequest {
    private String email;
    private String numberphone;
    private String password;

    public UserRequest(String email, String numberphone, String password) {
        this.email = email;
        this.numberphone = numberphone;
        this.password = password;
    }

    // Constructor linh hoạt: chỉ dùng email
    public UserRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Constructor linh hoạt: chỉ dùng số điện thoại
    public UserRequest(String numberphone) {
        this.numberphone = numberphone;
    }

    // Getter
    public String getEmail() {
        return email;
    }

    public String getNumberphone() {
        return numberphone;
    }

    public String getPassword() {
        return password;
    }

    // Setter
    public void setEmail(String email) {
        this.email = email;
    }

    public void setNumberphone(String numberphone) {
        this.numberphone = numberphone;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
