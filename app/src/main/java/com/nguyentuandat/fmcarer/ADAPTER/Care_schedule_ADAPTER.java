package com.nguyentuandat.fmcarer.ADAPTER;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log; // Import Log để debug
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.checkbox.MaterialCheckBox; // Đảm bảo import đúng MaterialCheckBox nếu dùng
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;
import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.RESPONSE.ApiResponse;
import com.nguyentuandat.fmcarer.RESPONSE.SingleCareScheludeResponse;

import org.json.JSONObject; // Để phân tích JSON error body nếu cần

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
    private List<Care_Schelude> displayList; // Danh sách hiển thị đã được sắp xếp/lọc
    private final List<Children> childList; // Danh sách trẻ em để điền Spinner
    private final ApiService apiService;
    private final String bearerToken;

    // Định dạng ngày giờ cho hiển thị (dd/MM/yyyy, HH:mm)
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    // Định dạng ngày giờ chuẩn cho Backend (yyyy-MM-dd, HH:mm)
    private static final DateTimeFormatter BACKEND_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter BACKEND_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    public Care_schedule_ADAPTER(Context context, List<Care_Schelude> scheduleList, List<Children> childList, String token) {
        this.context = context;
        this.childList = childList;
        this.bearerToken = token;
        this.apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        setData(scheduleList); // Gọi setData để khởi tạo và sắp xếp
    }

    // Cập nhật dữ liệu cho adapter
    public void setData(List<Care_Schelude> newData) {
        this.displayList = new ArrayList<>(newData); // Tạo bản sao để tránh thay đổi trực tiếp list gốc
        sortDisplayList(); // Sắp xếp lại danh sách
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật
    }

    // Sắp xếp danh sách hiển thị: Hoàn thành xuống dưới, chưa hoàn thành lên trên
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
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Care_Schelude schedule = displayList.get(position);

        // Hiển thị tên trẻ hoặc thông báo nếu trẻ bị xóa
        holder.tvChildName.setText(schedule.getChild() != null ? schedule.getChild().getName() : "Trẻ đã bị xóa");

        // Hiển thị loại nhắc nhở (custom_type nếu là "other")
        String typeText = schedule.getType().equals("other") && schedule.getCustomType() != null && !schedule.getCustomType().isEmpty()
                ? schedule.getCustomType()
                : getTypeLabel(schedule.getType()); // Lấy nhãn tiếng Việt cho các loại mặc định
        holder.tvType.setText("Loại nhắc: " + typeText);

        // Định dạng và hiển thị ngày giờ
        String formattedDate = "";
        String formattedTime = "";
        try {
            // Cố gắng parse ngày giờ từ backend và định dạng lại cho hiển thị
            LocalDate date = LocalDate.parse(schedule.getReminderDate(), BACKEND_DATE_FORMATTER);
            LocalTime time = LocalTime.parse(schedule.getReminderTime(), BACKEND_TIME_FORMATTER);
            formattedDate = date.format(DISPLAY_DATE_FORMATTER);
            formattedTime = time.format(DISPLAY_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Nếu parse lỗi, dùng giá trị gốc hoặc xử lý riêng
            Log.e("CareScheduleAdapter", "Error parsing date/time: " + e.getMessage());
            formattedDate = schedule.getReminderDate(); // Dùng nguyên nếu không parse được
            formattedTime = schedule.getReminderTime();
            // Xử lý trường hợp có "T" nếu reminder_date là ISO 8601 DateTime
            if (formattedDate != null && formattedDate.contains("T")) {
                formattedDate = formattedDate.substring(0, formattedDate.indexOf("T"));
            }
        }
        holder.tvDateTime.setText(formattedDate + " - " + formattedTime);

        // Hiển thị thông tin lặp lại
        String repeatText = schedule.isRepeat()
                ? "Lặp lại: " + getRepeatTypeLabel(schedule.getRepeatType())
                : "Không lặp lại";
        holder.tvRepeat.setText(repeatText);

        // Load ảnh đại diện của trẻ
        Glide.with(context)
                .load(schedule.getChild() != null && schedule.getChild().getAvatar_url() != null
                        ? schedule.getChild().getAvatar_url()
                        : R.drawable.taikhoan) // Ảnh mặc định nếu không có avatar
                .placeholder(R.drawable.taikhoan) // Placeholder khi đang tải
                .error(R.drawable.taikhoan) // Ảnh hiển thị nếu lỗi tải
                .circleCrop() // Cắt thành hình tròn
                .into(holder.imgChild);

        // Xử lý nhấn giữ (long click) để hiện dialog Sửa/Xóa
        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(schedule, position);
            return true; // Trả về true để consume sự kiện, không kích hoạt click thường
        });

        // Xử lý nút "Hoàn thành"
        holder.tvComplete.setText(schedule.isCompleted() ? "Đã hoàn thành" : "Hoàn thành");
        holder.tvComplete.setEnabled(!schedule.isCompleted()); // Vô hiệu hóa nút nếu đã hoàn thành
        if (schedule.isCompleted()) {
            // Nếu đã hoàn thành: đặt màu xanh lá và giảm độ mờ
            holder.tvComplete.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_completed));
            holder.tvComplete.setAlpha(0.6f);
        } else {
            // Nếu chưa hoàn thành: đặt lại màu mặc định (ví dụ: đen) và độ mờ đầy đủ
            holder.tvComplete.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.default_text_color)); // Hoặc màu đen trực tiếp: Color.BLACK
            holder.tvComplete.setAlpha(1f);
        }
        holder.tvComplete.setOnClickListener(v -> {
            if (!schedule.isCompleted()) { // Chỉ cho phép hoàn thành nếu chưa hoàn thành
                // Hiển thị dialog xác nhận trước khi hoàn thành
                showCompleteConfirmationDialog(schedule, position);
            }
        });
    }

    // Hiển thị dialog lựa chọn Sửa hoặc Xóa
    private void showOptionsDialog(Care_Schelude schedule, int position) {
        List<String> optionsList = new ArrayList<>();
        // Luôn hiển thị tùy chọn xóa
        optionsList.add("Xóa nhắc nhở");

        // Chỉ thêm tùy chọn sửa nếu nhắc nhở chưa hoàn thành
        if (!schedule.isCompleted()) {
            optionsList.add(0, "Sửa nhắc nhở"); // Thêm vào đầu danh sách nếu chưa hoàn thành
        }

        String[] options = optionsList.toArray(new String[0]);

        new AlertDialog.Builder(context)
                .setTitle("Chọn thao tác")
                .setItems(options, (dialog, which) -> {
                    if (schedule.isCompleted()) { // Nếu đã hoàn thành, chỉ có 1 tùy chọn là Xóa (index 0)
                        showDeleteConfirmationDialog(schedule.getId(), position);
                    } else { // Nếu chưa hoàn thành, có cả Sửa (index 0) và Xóa (index 1)
                        if (which == 0) { // Sửa
                            openEditDialog(schedule, position);
                        } else { // Xóa
                            showDeleteConfirmationDialog(schedule.getId(), position);
                        }
                    }
                })
                .show();
    }

    // Hàm mới: Hiển thị dialog xác nhận xóa
    private void showDeleteConfirmationDialog(String scheduleId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa nhắc nhở này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    confirmDelete(scheduleId, position); // Gọi hàm xóa sau khi xác nhận
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss(); // Đóng dialog xác nhận
                })
                .show();
    }

    // Gọi API xóa (được gọi từ showDeleteConfirmationDialog)
    private void confirmDelete(String scheduleId, int position) {
        apiService.deleteReminder(bearerToken, scheduleId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    displayList.remove(position); // Xóa khỏi danh sách hiển thị
                    notifyItemRemoved(position); // Thông báo cho RecyclerView item đã bị xóa
                    Toast.makeText(context, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = "Xóa thất bại.";
                    try {
                        if (response.errorBody() != null) {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());
                            if (jObjError.has("message")) {
                                errorMessage += " " + jObjError.getString("message");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ADAPTER_DELETE", "Error parsing errorBody: ", e);
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Log.e("ADAPTER_DELETE", "API call failed: ", t);
                Toast.makeText(context, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm mới: Hiển thị dialog xác nhận hoàn thành
    private void showCompleteConfirmationDialog(Care_Schelude schedule, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận hoàn thành")
                .setMessage("Bạn có chắc chắn muốn đánh dấu nhắc nhở này là hoàn thành không?")
                .setPositiveButton("Hoàn thành", (dialog, which) -> {
                    performCompleteAction(schedule, position); // Gọi hàm thực hiện hoàn thành sau khi xác nhận
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss(); // Đóng dialog xác nhận
                })
                .show();
    }

    // Hàm thực hiện API hoàn thành nhắc nhở (được gọi từ showCompleteConfirmationDialog)
    private void performCompleteAction(Care_Schelude schedule, int position) {
        apiService.completeReminder(bearerToken, schedule.getId())
                .enqueue(new Callback<SingleCareScheludeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Response<SingleCareScheludeResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Care_Schelude updatedSchedule = response.body().getData();
                            if (updatedSchedule != null) {
                                displayList.set(position, updatedSchedule);
                                sortDisplayList();
                                notifyDataSetChanged();
                            }
                            Toast.makeText(context, "Đã đánh dấu hoàn thành", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMessage = "Cập nhật thất bại.";
                            try {
                                if (response.errorBody() != null) {
                                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                                    if (jObjError.has("message")) {
                                        errorMessage += " " + jObjError.getString("message");
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("ADAPTER_COMPLETE", "Error parsing errorBody: ", e);
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Throwable t) {
                        Log.e("ADAPTER_COMPLETE", "API call failed: ", t);
                        Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- Hàm mở dialog Sửa nhắc nhở (Đã hoàn thiện) ---
    private void openEditDialog(Care_Schelude schedule, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.diglog_create_care_schelude, null); // Tái sử dụng layout
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Ánh xạ các view từ dialog layout
        Spinner spinnerChild = dialogView.findViewById(R.id.spinner_child);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);
        Spinner spinnerRepeatType = dialogView.findViewById(R.id.spinner_repeat_type);
        TextInputEditText edtNote = dialogView.findViewById(R.id.edt_note);
        TextInputEditText edtCustomType = dialogView.findViewById(R.id.edt_custom_type);
        TextInputEditText edtDate = dialogView.findViewById(R.id.edt_date);
        TextInputEditText edtTime = dialogView.findViewById(R.id.edt_time);
        TextInputLayout layoutCustomType = dialogView.findViewById(R.id.layout_custom_type);
        MaterialCheckBox checkboxRepeat = dialogView.findViewById(R.id.checkbox_repeat);
        TextView btnAction = dialogView.findViewById(R.id.btn_create_reminder); // Nút "Tạo nhắc nhở" sẽ đổi thành "Cập nhật"
        btnAction.setText("Cập nhật nhắc nhở"); // Thay đổi text cho nút

        // --- Cài đặt Spinners và pre-populate dữ liệu hiện có ---

        // Loại nhắc nhở
        String[] types = {"eat", "sleep", "bathe", "vaccine", "other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(typeAdapter.getPosition(schedule.getType())); // Chọn loại hiện tại

        // Loại lặp lại
        String[] repeats = {"none", "daily", "weekly", "monthly"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, repeats);
        spinnerRepeatType.setAdapter(repeatAdapter);
        spinnerRepeatType.setSelection(repeatAdapter.getPosition(schedule.getRepeatType())); // Chọn loại lặp hiện tại

        // Danh sách trẻ em
        List<String> childNames = new ArrayList<>();
        int selectedChildPosition = 0;
        for (int i = 0; i < childList.size(); i++) {
            Children child = childList.get(i);
            childNames.add(child.getName());
            // Tìm vị trí của trẻ hiện tại trong danh sách để chọn trên spinner
            if (schedule.getChild() != null && child.get_id().equals(schedule.getChild().get_id())) {
                selectedChildPosition = i;
            }
        }
        ArrayAdapter<String> childAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, childNames);
        spinnerChild.setAdapter(childAdapter);
        spinnerChild.setSelection(selectedChildPosition); // Chọn trẻ hiện tại


        // --- Pre-populate các trường TextInputEditText ---
        edtNote.setText(schedule.getNote());
        edtDate.setText(schedule.getReminderDate());
        edtTime.setText(schedule.getReminderTime());
        checkboxRepeat.setChecked(schedule.isRepeat());

        // Xử lý hiển thị custom_type nếu loại là "other"
        if (schedule.getType().equals("other")) {
            layoutCustomType.setVisibility(View.VISIBLE);
            edtCustomType.setText(schedule.getCustomType());
        } else {
            layoutCustomType.setVisibility(View.GONE);
        }

        // Listener cho spinner Type để ẩn/hiện layout_custom_type
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                layoutCustomType.setVisibility(types[pos].equals("other") ? View.VISIBLE : View.GONE);
                // Xóa custom_type nếu chuyển sang loại khác
                if (!types[pos].equals("other")) {
                    edtCustomType.setText("");
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // DatePickerDialog cho edtDate
        edtDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            // Cố gắng đặt ngày khởi tạo dialog theo ngày nhắc nhở hiện tại
            try {
                LocalDate currentReminderDate = LocalDate.parse(schedule.getReminderDate(), BACKEND_DATE_FORMATTER);
                calendar.set(currentReminderDate.getYear(), currentReminderDate.getMonthValue() - 1, currentReminderDate.getDayOfMonth());
            } catch (DateTimeParseException e) {
                // Nếu lỗi, dùng ngày hiện tại
                Log.e("ADAPTER_EDIT_DIALOG", "Error parsing reminder date for dialog: " + e.getMessage());
            }
            new DatePickerDialog(context, (view, year, month, day) -> {
                edtDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)); // Định dạng yyyy-MM-dd
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // TimePickerDialog cho edtTime
        edtTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            // Cố gắng đặt giờ khởi tạo dialog theo giờ nhắc nhở hiện tại
            try {
                LocalTime currentReminderTime = LocalTime.parse(schedule.getReminderTime(), BACKEND_TIME_FORMATTER);
                calendar.set(Calendar.HOUR_OF_DAY, currentReminderTime.getHour());
                calendar.set(Calendar.MINUTE, currentReminderTime.getMinute());
            } catch (DateTimeParseException e) {
                // Nếu lỗi, dùng giờ hiện tại
                Log.e("ADAPTER_EDIT_DIALOG", "Error parsing reminder time for dialog: " + e.getMessage());
            }
            new TimePickerDialog(context, (view, hour, minute) -> {
                edtTime.setText(String.format("%02d:%02d", hour, minute)); // Định dạng HH:mm
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show(); // "true" cho chế độ 24 giờ
        });

        // --- Listener cho nút "Cập nhật nhắc nhở" ---
        btnAction.setOnClickListener(v -> {
            // Lấy dữ liệu mới từ các trường
            String updatedType = spinnerType.getSelectedItem().toString();
            String updatedNote = edtNote.getText().toString().trim();
            String updatedCustomType = edtCustomType.getText().toString().trim();
            String updatedDate = edtDate.getText().toString().trim();
            String updatedTime = edtTime.getText().toString().trim();
            String updatedRepeatType = spinnerRepeatType.getSelectedItem().toString();
            boolean updatedRepeat = checkboxRepeat.isChecked();
            String updatedChildId = childList.get(spinnerChild.getSelectedItemPosition()).get_id();

            // Kiểm tra validation cơ bản (tương tự Fragment)
            if (updatedType.equals("other") && updatedCustomType.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập loại khác", Toast.LENGTH_SHORT).show();
                return;
            }
            if (updatedDate.isEmpty() || updatedTime.isEmpty()) {
                Toast.makeText(context, "Vui lòng chọn ngày và giờ", Toast.LENGTH_SHORT).show();
                return;
            }


            // Chuẩn bị dữ liệu gửi lên API
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("child_id", updatedChildId);
            updateData.put("type", updatedType);
            updateData.put("custom_type", updatedType.equals("other") ? updatedCustomType : null); // null nếu không phải other
            updateData.put("note", updatedNote);
            updateData.put("reminder_date", updatedDate);
            updateData.put("reminder_time", updatedTime);
            updateData.put("repeat", updatedRepeat);
            updateData.put("repeat_type", updatedRepeatType);
            updateData.put("is_completed", schedule.isCompleted()); // Giữ nguyên trạng thái hoàn thành ban đầu

            Log.d("UPDATE_REMINDER_DATA", "Body gửi API: " + updateData.toString());
            Log.d("UPDATE_REMINDER_DATA", "Token: " + bearerToken);
            Log.d("UPDATE_REMINDER_DATA", "Reminder ID: " + schedule.getId());


            // Gọi API cập nhật
            apiService.updateReminder(bearerToken, schedule.getId(), updateData).enqueue(new Callback<SingleCareScheludeResponse>() {
                @Override
                public void onResponse(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Response<SingleCareScheludeResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Cập nhật item trong displayList với dữ liệu mới từ response
                        // Sau đó gọi setData để sắp xếp và refresh RecyclerView
                        Care_Schelude updatedSchedule = response.body().getData();
                        if (updatedSchedule != null) {
                            displayList.set(position, updatedSchedule);
                            setData(displayList); // Gọi setData để kích hoạt sort và notifyDataSetChanged
                        }
                        Toast.makeText(context, "Đã cập nhật nhắc nhở", Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // Đóng dialog
                    } else {
                        String errorMessage = "Cập nhật thất bại.";
                        try {
                            if (response.errorBody() != null) {
                                String errorBodyString = response.errorBody().string();
                                Log.e("UPDATE_REMINDER_API", "Lỗi phản hồi API: " + errorBodyString);
                                JSONObject jObjError = new JSONObject(errorBodyString);
                                if (jObjError.has("message")) {
                                    errorMessage += " " + jObjError.getString("message");
                                }
                            }
                        } catch (Exception e) {
                            Log.e("UPDATE_REMINDER_API", "Lỗi khi đọc errorBody: ", e);
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Throwable t) {
                    Log.e("UPDATE_REMINDER_API", "Lỗi kết nối API: ", t);
                    Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    // ViewHolder class
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

    // Hàm tiện ích để lấy nhãn tiếng Việt cho loại nhắc nhở
    private String getTypeLabel(String type) {
        switch (type) {
            case "eat": return "Ăn uống";
            case "sleep": return "Ngủ";
            case "bathe": return "Tắm";
            case "vaccine": return "Tiêm chủng";
            default: return "Khác";
        }
    }

    // Hàm tiện ích để lấy nhãn tiếng Việt cho loại lặp lại
    private String getRepeatTypeLabel(String repeatType) {
        switch (repeatType) {
            case "daily": return "Hàng ngày";
            case "weekly": return "Hàng tuần";
            case "monthly": return "Hàng tháng";
            default: return "Không";
        }
    }
}