package com.nguyentuandat.fmcarer.RESPONSE;

import com.google.gson.annotations.SerializedName;
import com.nguyentuandat.fmcarer.MODEL.Payment; // Đảm bảo đã import Payment Model

import java.util.List;

public class PaymentHistoryResponse {
    @SerializedName("total")
    private int total;
    @SerializedName("limit")
    private int limit;
    @SerializedName("skip")
    private int skip;
    @SerializedName("data")
    private List<Payment> data;
    @SerializedName("message") // Có thể có message lỗi nếu backend gửi
    private String message;

    // Getters and Setters
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public List<Payment> getData() {
        return data;
    }

    public void setData(List<Payment> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}