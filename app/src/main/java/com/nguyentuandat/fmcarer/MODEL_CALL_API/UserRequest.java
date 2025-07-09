package com.nguyentuandat.fmcarer.MODEL_CALL_API;

public class UserRequest {
    private String email;
    private String numberphone;
    private String password;

    // Constructor chung: bao gồm email, số điện thoại và mật khẩu
    // Thích hợp khi bạn có thể cung cấp cả email và số điện thoại, hoặc một trong hai là null
    public UserRequest(String email, String numberphone, String password) {
        this.email = email;
        this.numberphone = numberphone;
        this.password = password;
    }

    // Constructor linh hoạt: chỉ dùng email và mật khẩu (ví dụ: cho đăng ký/đăng nhập parent)
    public UserRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.numberphone = null; // Đảm bảo numberphone là null nếu không được cung cấp
    }

    // Constructor linh hoạt: chỉ dùng số điện thoại (ví dụ: cho gửi OTP qua số điện thoại)
    public UserRequest(String numberphone) {
        this.numberphone = numberphone;
        this.email = null;    // Đảm bảo email là null
        this.password = null; // Đảm bảo password là null
    }

    // Thêm constructor cho trường hợp đăng nhập tài khoản phụ (numberphone + password)
    // Nếu bạn muốn phân biệt rõ ràng hơn thay vì dùng constructor (email, password) với email là null
    public UserRequest(String numberphone, String password, boolean isSubuserLogin) {
        this.numberphone = numberphone;
        this.password = password;
        this.email = null; // Đảm bảo email là null cho subuser
    }


    // Getters
    public String getEmail() {
        return email;
    }

    public String getNumberphone() {
        return numberphone;
    }

    public String getPassword() {
        return password;
    }

    // Setters
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