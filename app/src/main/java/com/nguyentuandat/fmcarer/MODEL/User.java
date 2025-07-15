package com.nguyentuandat.fmcarer.MODEL;

import com.google.gson.annotations.SerializedName;
// import com.google.gson.annotations.Expose; // Thêm nếu bạn sử dụng @Expose

public class User {

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("balance") // <-- Đảm bảo kiểu dữ liệu là BalanceDecimal128
    private BalanceDecimal128 balance; // <-- Sử dụng lớp này

    @SerializedName("isVerified")
    private boolean isVerified; // Thêm trường này nếu nó có trong JSON response

    @SerializedName("fullname")
    private String fullname;

    @SerializedName("numberphone")
    private String numberphone;

    @SerializedName("image")
    private String image;

    @SerializedName("created_at")
    private String createdAt; // Hoặc java.util.Date nếu bạn cấu hình Gson để parse Date

    @SerializedName("relationship")
    private String relationship; // Giữ lại nếu nó có trong response

    @SerializedName("__v")
    private int v; // Thêm trường này nếu nó có trong JSON response

    public User() {
        // constructor mặc định
    }

    // --- Getters và Setters cho tất cả các trường ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public BalanceDecimal128 getBalance() { return balance; } // Getter cho BalanceDecimal128
    public void setBalance(BalanceDecimal128 balance) { this.balance = balance; } // Setter cho BalanceDecimal128

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean isVerified) { this.isVerified = isVerified; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getNumberphone() { return numberphone; }
    public void setNumberphone(String numberphone) { this.numberphone = numberphone; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public int getV() { return v; }
    public void setV(int v) { this.v = v; }
}