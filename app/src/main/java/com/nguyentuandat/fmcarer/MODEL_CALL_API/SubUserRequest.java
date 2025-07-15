package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubUserRequest {

    @SerializedName("_id")
    @Expose
    private String id;

    @SerializedName("fullname")
    @Expose
    private String fullname;

    @SerializedName("numberphone")
    @Expose
    private String numberphone;

    @SerializedName("password")
    @Expose
    private String password;

    // *** THAY ĐỔI Ở ĐÂY: Đổi "created_by" thành "parentId" để khớp với Backend ***
    @SerializedName("parentId") // Matches backend's 'parentId' field
    @Expose
    private String createdBy; // Giữ nguyên tên biến Java là 'createdBy' nếu bạn muốn,
    // hoặc đổi thành 'parentId' để nhất quán hơn.

    @SerializedName("image")
    @Expose
    private String image;

    @SerializedName("role")
    @Expose
    private String role; // Role is sent to backend for creation

    // Default constructor (required by Gson)
    public SubUserRequest() {
        this.role = "subuser"; // Default role
        this.image = ""; // Default empty image URL
    }

    // --- CONSTRUCTOR CHO TẠO MỚI SUBUSER ---
    // Backend: numberphone, password, fullname, image, parentId
    public SubUserRequest(String fullname, String numberphone, String password, String createdBy, String image) {
        this(); // Calls default constructor to set default role and image
        this.fullname = fullname;
        this.numberphone = numberphone;
        this.password = password;
        this.createdBy = createdBy; // Đây sẽ map tới trường "parentId" trong JSON
        this.image = image;
        this.id = null; // Ensure ID is null for new creation
    }

    // --- CONSTRUCTOR CHO CẬP NHẬT SUBUSER ---
    // Bao gồm ID vì đây là cập nhật một đối tượng hiện có
    public SubUserRequest(String id, String fullname, String numberphone, String password, String createdBy, String image) {
        this(); // Calls default constructor to set default role and image
        this.id = id;
        this.fullname = fullname;
        this.numberphone = numberphone;
        this.password = password;
        this.createdBy = createdBy; // Đây sẽ map tới trường "parentId" trong JSON
        this.image = image;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getNumberphone() { return numberphone; }
    public void setNumberphone(String numberphone) { this.numberphone = numberphone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // *** Getter và Setter vẫn dùng 'createdBy' trong Java,
    // nhưng khi Gson serialize/deserialize, nó sẽ dùng "parentId" ***
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}