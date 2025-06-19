package com.nguyentuandat.fmcarer.MODEL_CALL_API;

/**
 * Dùng để gửi thông tin cập nhật tài khoản người dùng lên server.
 * Không cần email, chỉ cần _id để định danh người dùng.
 */
public class UserUpdateRequest {
    private String _id;
    private String fullname;
    private String numberphone;
    private String image;

    // Constructor đầy đủ
    public UserUpdateRequest(String _id, String fullname, String numberphone, String image) {
        this._id = _id;
        this.fullname = fullname;
        this.numberphone = numberphone;
        this.image = image;
    }

    // Getter
    public String get_id() {
        return _id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getNumberphone() {
        return numberphone;
    }

    public String getImage() {
        return image;
    }

    // Setter
    public void set_id(String _id) {
        this._id = _id;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setNumberphone(String numberphone) {
        this.numberphone = numberphone;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
