package com.nguyentuandat.fmcarer.RESPONSE;

import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;

public class SingleCareScheludeResponse {

    private boolean success;
    private Care_Schelude data;
    private String message; // Thêm trường message
    private Object error;   // Thêm trường error (có thể là Map<String, Object> hoặc Object tùy cấu trúc lỗi)

    public boolean isSuccess() {
        return success;
    }

    public Care_Schelude getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public Object getError() { // Hoặc Map<String, Object> nếu lỗi là JSON object
        return error;
    }
}