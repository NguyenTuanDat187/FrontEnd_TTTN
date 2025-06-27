package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;
import java.util.List;

public class CareScheludeResponse {
// lấy danh sách
// lịch chăm sóc của trẻ em từ API
    private boolean success;
    private List<Care_Schelude> data;

    public boolean isSuccess() {
        return success;
    }

    public List<Care_Schelude> getData() {
        return data;
    }
}
