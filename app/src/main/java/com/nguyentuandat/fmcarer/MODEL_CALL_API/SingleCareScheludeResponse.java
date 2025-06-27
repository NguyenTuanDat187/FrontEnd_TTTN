package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;

public class SingleCareScheludeResponse {

    private boolean success;
    private Care_Schelude data;

    public boolean isSuccess() {
        return success;
    }

    public Care_Schelude getData() {
        return data;
    }
}
