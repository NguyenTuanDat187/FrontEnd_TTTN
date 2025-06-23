package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubUserRequest {

    @SerializedName("fullname")
    @Expose
    private String fullname;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("numberphone")
    @Expose
    private String numberphone;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("parentId")
    @Expose
    private String parentId;

    @SerializedName("relationship")
    @Expose
    private String relationship;

    // ✅ Constructor mặc định (bắt buộc với Gson)
    public SubUserRequest() {}

    // ✅ Constructor đầy đủ
    public SubUserRequest(String fullname, String email, String numberphone, String password, String parentId, String relationship) {
        this.fullname = fullname;
        this.email = (email != null && !email.trim().isEmpty()) ? email : null;  // 👈 Không gửi email nếu rỗng
        this.numberphone = numberphone;
        this.password = password;
        this.parentId = parentId;
        this.relationship = relationship;
    }

    // ✅ Getters
    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getNumberphone() {
        return numberphone;
    }

    public String getPassword() {
        return password;
    }

    public String getParentId() {
        return parentId;
    }

    public String getRelationship() {
        return relationship;
    }

    // ✅ Setters
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setEmail(String email) {
        this.email = (email != null && !email.trim().isEmpty()) ? email : null;
    }

    public void setNumberphone(String numberphone) {
        this.numberphone = numberphone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
