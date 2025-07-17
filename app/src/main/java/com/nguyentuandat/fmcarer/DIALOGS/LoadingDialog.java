// Đảm bảo package này khớp với cấu trúc thư mục của bạn
package com.nguyentuandat.fmcarer.DIALOGS;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nguyentuandat.fmcarer.R;

public class LoadingDialog {

    public Dialog dialog;
    private TextView loadingMessage;
    private ProgressBar progressBar;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        loadingMessage = view.findViewById(R.id.loading_message);
        progressBar = view.findViewById(R.id.progress_bar); // Ánh xạ ProgressBar
        dialog.setContentView(view);
        dialog.setCancelable(false); // Không cho phép người dùng đóng bằng cách chạm bên ngoài
        if (dialog.getWindow() != null) {
            // Đặt nền trong suốt để chỉ hiển thị nội dung dialog
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setMessage(String message) {
        if (loadingMessage != null) {
            loadingMessage.setText(message);
        }
    }

    // Optional: Methods to control ProgressBar visibility if needed
    public void showProgressBar(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}