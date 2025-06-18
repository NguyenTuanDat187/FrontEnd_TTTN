package com.nguyentuandat.fmcarer.MODEL_CALL_API;

public class OtpRequest {
    private String email;

    public OtpRequest(String email) {
        this.email = email;
    }

    // Getter
    public String getEmail() {
        return email;
    }
}
