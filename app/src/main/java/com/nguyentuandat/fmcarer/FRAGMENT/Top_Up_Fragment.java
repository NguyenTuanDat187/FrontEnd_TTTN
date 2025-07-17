package com.nguyentuandat.fmcarer.FRAGMENT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nguyentuandat.fmcarer.MODEL_CALL_API.TopUpInitiateRequest;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.RESPONSE.PaymentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Top_Up_Fragment extends Fragment {

    private EditText edtAmount;
    private RadioGroup radioGroupPayment;
    private RadioButton radioMomo, radioZaloPay, radioVNPay;
    private Button btnProceedPayment;

    private ApiService apiService; // Khai báo Retrofit service

    // Định nghĩa giới hạn số tiền nạp
    private static final long MIN_AMOUNT = 10000; // 10,000 VND
    private static final long MAX_AMOUNT = 50000000; // 50,000,000 VND

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo Retrofit
        // Thay YOUR_BASE_URL bằng địa chỉ backend của bạn (ví dụ: http://192.168.1.X:3000)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/") // Sử dụng 10.0.2.2 cho emulator để trỏ về localhost của máy tính
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.top_up_fragment, container, false);

        // Ánh xạ các view
        edtAmount = view.findViewById(R.id.editAmount);
        radioGroupPayment = view.findViewById(R.id.radioGroupPayment);
        radioMomo = view.findViewById(R.id.radioMomo);
        radioZaloPay = view.findViewById(R.id.radioZaloPay);
        radioVNPay = view.findViewById(R.id.radioVNPay);
        btnProceedPayment = view.findViewById(R.id.btnTopUp);

        // Ban đầu, không có phương thức nào được chọn, nút thanh toán bị vô hiệu hóa
        btnProceedPayment.setEnabled(false);

        // Thiết lập listener cho RadioGroup để quản lý lựa chọn
        // RadioGroup sẽ tự động đảm bảo chỉ một RadioButton được chọn trong nhóm của nó.
        radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMomo) {
                btnProceedPayment.setEnabled(true);
            } else if (checkedId == R.id.radioZaloPay || checkedId == R.id.radioVNPay) {
                // Nếu chọn ZaloPay hoặc VNPay, hiển thị Toast và vô hiệu hóa nút
                Toast.makeText(getContext(), "Phương thức thanh toán này chưa được hỗ trợ.", Toast.LENGTH_SHORT).show();
                btnProceedPayment.setEnabled(false);
                // Bỏ chọn RadioButton vừa được chọn (để chỉ Momo được chọn hoặc không có gì)
                group.clearCheck(); // Xóa tất cả lựa chọn trong group
            } else {
                // Không có lựa chọn nào hoặc lựa chọn không hợp lệ
                btnProceedPayment.setEnabled(false);
            }
        });

        // Xử lý sự kiện khi bấm nút thanh toán
        btnProceedPayment.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền.", Toast.LENGTH_SHORT).show();
                return;
            }

            long amount;
            try {
                amount = Long.parseLong(amountStr);
                // Validate số tiền
                if (amount < MIN_AMOUNT) {
                    Toast.makeText(getContext(), "Số tiền nạp tối thiểu là " + MIN_AMOUNT + " VND.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (amount > MAX_AMOUNT) {
                    Toast.makeText(getContext(), "Số tiền nạp tối đa là " + MAX_AMOUNT + " VND.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (radioMomo.isChecked()) {
                // Gọi API nạp tiền Momo
                initiateMomoTopUp(amount);
            } else {
                // Trường hợp này không nên xảy ra nếu logic RadioGroup hoạt động đúng,
                // nhưng vẫn thêm để đảm bảo
                Toast.makeText(getContext(), "Vui lòng chọn phương thức thanh toán hợp lệ.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void initiateMomoTopUp(long amount) {
        // Lấy token xác thực của người dùng.
        // TRONG THỰC TẾ: Bạn cần lấy token này từ SharedPreferences, Session, hoặc nơi bạn lưu trữ token sau khi đăng nhập.
        // Đây là token giả lập cho mục đích test với backend mock authMiddleware.
        String authToken = "Bearer mock-token"; // Thay thế bằng token thật của người dùng

        TopUpInitiateRequest request = new TopUpInitiateRequest(amount, "Momo");

        apiService.initiateTopUp(authToken, request).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentResponse> call, @NonNull Response<PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PaymentResponse paymentResponse = response.body();
                    String payUrl = paymentResponse.getPayUrl();
                    if (payUrl != null && !payUrl.isEmpty()) {
                        Toast.makeText(getContext(), "Khởi tạo Momo thành công. Chuyển hướng thanh toán...", Toast.LENGTH_LONG).show();
                        // Chuyển hướng người dùng đến URL của Momo
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(getContext(), "Lỗi: Không nhận được URL thanh toán từ Momo.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Xử lý lỗi từ backend (ví dụ: 400 Bad Request, 500 Internal Server Error)
                    String errorMessage = "Khởi tạo Momo thất bại. Vui lòng thử lại.";
                    if (response.errorBody() != null) {
                        try {
                            // Cố gắng đọc thông báo lỗi từ response.errorBody()
                            // Tùy thuộc vào cấu trúc lỗi của backend, bạn có thể parse JSON ở đây
                            errorMessage = response.errorBody().string();
                            // Ví dụ: nếu backend trả về JSON {"message": "Invalid amount"}, bạn có thể parse nó
                            // JSONObject errorJson = new JSONObject(errorMessage);
                            // errorMessage = errorJson.optString("message", "Lỗi không xác định.");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentResponse> call, @NonNull Throwable t) {
                // Xử lý lỗi mạng hoặc lỗi không mong muốn
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}
