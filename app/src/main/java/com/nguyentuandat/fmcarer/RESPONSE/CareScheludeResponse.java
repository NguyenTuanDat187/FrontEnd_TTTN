package com.nguyentuandat.fmcarer.RESPONSE;

import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;
import java.util.List;

public class CareScheludeResponse {
    private boolean success;
    private List<Care_Schelude> data;
    private String message; // ⬅️ thêm message để dễ debug nếu cần

    public boolean isSuccess() {
        return success;
    }

    public List<Care_Schelude> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
