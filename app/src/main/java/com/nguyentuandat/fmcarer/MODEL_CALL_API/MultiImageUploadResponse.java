package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import java.util.List;

public class MultiImageUploadResponse {
    private boolean success; // Thêm trường này để biểu thị trạng thái thành công
    private List<String> imageUrls;
    private String message; // Có thể thêm trường message để nhận thông báo từ server

    public boolean isSuccess() { // Thêm phương thức getter cho 'success'
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}