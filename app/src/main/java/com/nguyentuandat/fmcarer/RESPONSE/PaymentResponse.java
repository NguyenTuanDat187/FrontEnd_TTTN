package com.nguyentuandat.fmcarer.RESPONSE;

import com.google.gson.annotations.SerializedName;
import com.nguyentuandat.fmcarer.MODEL.Payment; // Giả định bạn có Payment Model tương ứng với Schema MongoDB

public class PaymentResponse {
    @SerializedName("message")
    private String message;
    @SerializedName("payment")
    private Payment payment; // Thông tin chi tiết giao dịch payment
    @SerializedName("payUrl")
    private String payUrl; // URL để chuyển hướng người dùng đến cổng thanh toán
    @SerializedName("error")
    private String error; // Trường lỗi nếu có

    // Getters
    public String getMessage() {
        return message;
    }

    public Payment getPayment() {
        return payment;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public String getError() {
        return error;
    }

    // Setters (nếu cần)
    public void setMessage(String message) {
        this.message = message;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public void setError(String error) {
        this.error = error;
    }
}