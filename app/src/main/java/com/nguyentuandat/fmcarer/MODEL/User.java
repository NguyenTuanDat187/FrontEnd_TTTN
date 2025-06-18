package com.nguyentuandat.fmcarer.MODEL;

public class User {
    private String _id;
    private String email;
    private String password;
    private String role;
    private String created_by;
    private String balance;
    private String fullname;
    private String phone;
    private String image;
    private String created_at;

    public User(String _id, String email, String password, String role, String created_by, String balance, String fullname, String phone, String image, String created_at) {
        this._id = _id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.created_by = created_by;
        this.balance = balance;
        this.fullname = fullname;
        this.phone = phone;
        this.image = image;
        this.created_at = created_at;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
