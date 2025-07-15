package com.nguyentuandat.fmcarer.MODEL; // Hoặc package con của bạn

import com.google.gson.annotations.SerializedName;

// Tên lớp này phản ánh đúng kiểu dữ liệu Decimal128 từ MongoDB
public class BalanceDecimal128 {
    @SerializedName("$numberDecimal") // Tên trường JSON chính xác mà server trả về cho Decimal128
    private String numberDecimal; // Lấy nó dưới dạng chuỗi

    // Constructor mặc định (cần thiết cho GSON)
    public BalanceDecimal128() {}

    // Getter
    public String getNumberDecimal() {
        return numberDecimal;
    }

    // Setter (nếu cần)
    public void setNumberDecimal(String numberDecimal) {
        this.numberDecimal = numberDecimal;
    }

    // Phương thức tiện ích để lấy giá trị số dưới dạng double hoặc float
    public double toDouble() {
        try {
            if (numberDecimal == null || numberDecimal.isEmpty()) {
                return 0.0; // Trả về 0 nếu chuỗi rỗng/null
            }
            return Double.parseDouble(numberDecimal);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0.0; // Trả về giá trị mặc định hoặc ném exception
        }
    }

    public float toFloat() {
        try {
            if (numberDecimal == null || numberDecimal.isEmpty()) {
                return 0.0f; // Trả về 0 nếu chuỗi rỗng/null
            }
            return Float.parseFloat(numberDecimal);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0.0f;
        }
    }
}