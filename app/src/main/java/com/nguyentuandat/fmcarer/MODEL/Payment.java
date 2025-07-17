package com.nguyentuandat.fmcarer.MODEL;

import com.google.gson.annotations.SerializedName;

public class Payment {
    @SerializedName("_id")
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("amount")
    private long amount;
    @SerializedName("currency")
    private String currency;
    @SerializedName("payment_method")
    private String paymentMethod;
    @SerializedName("transaction_id")
    private String transactionId;
    @SerializedName("order_info")
    private String orderInfo;
    @SerializedName("status")
    private String status; // Pending, Completed, Failed
    @SerializedName("payment_date")
    private String paymentDate; // Hoặc Date nếu bạn sử dụng Date object và Gson converter
    @SerializedName("gateway_transaction_id")
    private String gatewayTransactionId;
    @SerializedName("pay_url")
    private String payUrl;
    @SerializedName("raw_gateway_response")
    private Object rawGatewayResponse; // Có thể là Map<String, Object> hoặc một class riêng
    @SerializedName("completed_at")
    private String completedAt;
    @SerializedName("failed_reason")
    private String failedReason;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructors (optional, but good practice)
    public Payment(String id, String userId, long amount, String currency, String paymentMethod,
                   String transactionId, String orderInfo, String status, String paymentDate,
                   String gatewayTransactionId, String payUrl, Object rawGatewayResponse,
                   String completedAt, String failedReason, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.orderInfo = orderInfo;
        this.status = status;
        this.paymentDate = paymentDate;
        this.gatewayTransactionId = gatewayTransactionId;
        this.payUrl = payUrl;
        this.rawGatewayResponse = rawGatewayResponse;
        this.completedAt = completedAt;
        this.failedReason = failedReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getOrderInfo() { return orderInfo; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    public Object getRawGatewayResponse() { return rawGatewayResponse; }
    public void setRawGatewayResponse(Object rawGatewayResponse) { this.rawGatewayResponse = rawGatewayResponse; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public String getFailedReason() { return failedReason; }
    public void setFailedReason(String failedReason) { this.failedReason = failedReason; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}