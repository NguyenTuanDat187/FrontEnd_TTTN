package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.nguyentuandat.fmcarer.MODEL.Children;

import java.util.List;

public class ChildrenResponse {
    private boolean success;
    private List<Children> data;

    public boolean isSuccess() {
        return success;
    }

    public List<Children> getData() {
        return data;
    }
}
