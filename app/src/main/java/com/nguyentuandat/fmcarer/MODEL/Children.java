package com.nguyentuandat.fmcarer.MODEL;

/**
 * Model đại diện cho đối tượng "Trẻ em" (Child)
 * Dùng để ánh xạ dữ liệu giữa Android và server (MongoDB)
 */
public class Children {

    // ID của đối tượng trong MongoDB (_id)
    private String _id;

    // ID của người dùng (cha/mẹ) gắn với trẻ này
    private String user_id;

    // Họ tên trẻ
    private String name;

    // Ngày sinh (định dạng: yyyy-MM-dd)
    private String dob;

    // Giới tính: "male", "female", "other"
    private String gender;

    // Đường dẫn URL của ảnh đại diện (nếu có)
    private String avatar_url;

    // Ngày tạo bản ghi (auto bởi server)
    private String created_at;

    // Constructor rỗng (bắt buộc cho Firebase/Retrofit/Gson)
    public Children() {
    }

    // Constructor đầy đủ tham số

    public Children(String _id, String user_id, String name, String dob, String gender, String avatar_url, String created_at) {
        this._id = _id;
        this.user_id = user_id;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.avatar_url = avatar_url;
        this.created_at = created_at;
    }
    public Children(String userId, String name, String dob, String gender) {
        this.user_id = userId;
        this.name = name;
        this.gender = gender;
        this.dob = dob;
    }

    public Children(String name, String dob, String gender) {
        this.name = name;
        this.dob = dob;
        this.gender = gender;
    }
    // Getter & Setter cho từng trường

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
