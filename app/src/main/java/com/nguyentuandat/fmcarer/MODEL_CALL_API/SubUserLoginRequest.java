package com.nguyentuandat.fmcarer.MODEL_CALL_API;

// Lớp này đại diện cho cấu trúc dữ liệu yêu cầu đăng nhập của tài khoản phụ (subuser)
// Nó chứa số điện thoại và mật khẩu.
public class SubUserLoginRequest {

    // Tên trường phải khớp với tên mà API backend của bạn mong đợi
    // (trong trường hợp này là "numberphone" và "password" cho endpoint loginSubuser)
    private String numberphone;
    private String password;

    /**
     * Constructor để khởi tạo một đối tượng SubUserLoginRequest.
     *
     * @param numberphone Số điện thoại của tài khoản phụ.
     * @param password Mật khẩu của tài khoản phụ.
     */
    public SubUserLoginRequest(String numberphone, String password) {
        this.numberphone = numberphone;
        this.password = password;
    }

    /**
     * Lấy số điện thoại.
     * Retrofit/Gson sẽ sử dụng getter này (hoặc truy cập trực tiếp trường)
     * để serialize đối tượng thành JSON.
     *
     * @return Số điện thoại.
     */
    public String getNumberphone() {
        return numberphone;
    }

    /**
     * Lấy mật khẩu.
     *
     * @return Mật khẩu.
     */
    public String getPassword() {
        return password;
    }

    // Bạn có thể thêm các setters nếu cần thay đổi giá trị sau khi tạo đối tượng,
    // nhưng đối với request body, thường không cần thiết.
    // public void setNumberphone(String numberphone) {
    //     this.numberphone = numberphone;
    // }
    //
    // public void setPassword(String password) {
    //     this.password = password;
    // }
}