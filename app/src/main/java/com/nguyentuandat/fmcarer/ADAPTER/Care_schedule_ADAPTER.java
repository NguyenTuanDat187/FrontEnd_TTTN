// File: Care_schedule_ADAPTER.java

package com.nguyentuandat.fmcarer.ADAPTER;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;
import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.RESPONSE.ApiResponse;
import com.nguyentuandat.fmcarer.RESPONSE.SingleCareScheludeResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Care_schedule_ADAPTER extends RecyclerView.Adapter<Care_schedule_ADAPTER.ViewHolder> {

    private final Context context;
    private List<Care_Schelude> displayList;
    private final List<Children> childList;
    private final ApiService apiService;
    private final String bearerToken;

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public Care_schedule_ADAPTER(Context context, List<Care_Schelude> scheduleList, List<Children> childList, String token) {
        this.context = context;
        this.childList = childList;
        this.bearerToken = token;
        this.apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        setData(scheduleList);
    }

    public void setData(List<Care_Schelude> newData) {
        this.displayList = new ArrayList<>(newData);
        sortDisplayList();
        notifyDataSetChanged();
    }

    private void sortDisplayList() {
        Collections.sort(displayList, (s1, s2) -> Boolean.compare(s1.isCompleted(), s2.isCompleted()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_care_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Care_Schelude schedule = displayList.get(position);

        holder.tvChildName.setText(schedule.getChild() != null ? schedule.getChild().getName() : "Trẻ đã bị xóa");

        String type = schedule.getType().equals("other") && schedule.getCustomType() != null
                ? schedule.getCustomType()
                : getTypeLabel(schedule.getType());
        holder.tvType.setText("Loại nhắc: " + type);

        String formattedDate = schedule.getReminderDate();
        String formattedTime = schedule.getReminderTime();
        try {
            LocalDateTime reminderDateTime = LocalDateTime.of(
                    LocalDate.parse(schedule.getReminderDate()),
                    LocalTime.parse(schedule.getReminderTime()));
            formattedDate = reminderDateTime.format(DISPLAY_DATE_FORMATTER);
            formattedTime = reminderDateTime.format(DISPLAY_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            if (formattedDate != null && formattedDate.contains("T")) {
                formattedDate = formattedDate.substring(0, formattedDate.indexOf("T"));
            }
        }
        holder.tvDateTime.setText(formattedDate + " - " + formattedTime);

        String repeatText = schedule.isRepeat()
                ? "Lặp lại: " + getRepeatTypeLabel(schedule.getRepeatType())
                : "Không lặp lại";
        holder.tvRepeat.setText(repeatText);

        Glide.with(context)
                .load(schedule.getChild() != null && schedule.getChild().getAvatar_url() != null
                        ? schedule.getChild().getAvatar_url()
                        : R.drawable.taikhoan)
                .placeholder(R.drawable.taikhoan)
                .circleCrop()
                .into(holder.imgChild);

        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(schedule, position);
            return true;
        });

        holder.tvComplete.setText(schedule.isCompleted() ? "Đã hoàn thành" : "Hoàn thành");
        holder.tvComplete.setEnabled(!schedule.isCompleted());
        holder.tvComplete.setAlpha(schedule.isCompleted() ? 0.6f : 1f);

        holder.tvComplete.setOnClickListener(v -> {
            if (!schedule.isCompleted()) {
                apiService.completeReminder(bearerToken, schedule.getId())
                        .enqueue(new Callback<SingleCareScheludeResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Response<SingleCareScheludeResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    schedule.setCompleted(true);
                                    sortDisplayList();
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Đã đánh dấu hoàn thành", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Throwable t) {
                                Toast.makeText(context, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    private void showOptionsDialog(Care_Schelude schedule, int position) {
        String[] options = {"Sửa nhắc nhở", "Xóa nhắc nhở"};
        new AlertDialog.Builder(context)
                .setTitle("Chọn thao tác")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openEditDialog(schedule, position);
                    else confirmDelete(schedule.getId(), position);
                })
                .show();
    }

    private void confirmDelete(String scheduleId, int position) {
        apiService.deleteReminder(bearerToken, scheduleId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    displayList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openEditDialog(Care_Schelude schedule, int position) {
        // Viết nội dung sửa tương tự như bạn đã làm, đảm bảo sửa đúng child, time, type,...
        // Nếu cần chi tiết phần này, bạn nhắn thêm nhé!
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgChild;
        TextView tvChildName, tvType, tvDateTime, tvRepeat, tvComplete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgChild = itemView.findViewById(R.id.imgChild);
            tvChildName = itemView.findViewById(R.id.tvChildName);
            tvType = itemView.findViewById(R.id.tvType);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvRepeat = itemView.findViewById(R.id.tvRepeat);
            tvComplete = itemView.findViewById(R.id.tvComplete);
        }
    }

    private String getTypeLabel(String type) {
        switch (type) {
            case "eat": return "Ăn uống";
            case "sleep": return "Ngủ";
            case "bathe": return "Tắm";
            case "vaccine": return "Tiêm chủng";
            default: return "Khác";
        }
    }

    private String getRepeatTypeLabel(String repeatType) {
        switch (repeatType) {
            case "daily": return "Hàng ngày";
            case "weekly": return "Hàng tuần";
            case "monthly": return "Hàng tháng";
            default: return "Không";
        }
    }
}
