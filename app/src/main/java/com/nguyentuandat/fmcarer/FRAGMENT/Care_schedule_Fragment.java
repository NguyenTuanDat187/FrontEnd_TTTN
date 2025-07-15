package com.nguyentuandat.fmcarer.FRAGMENT;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.checkbox.MaterialCheckBox; // Đảm bảo import đúng MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyentuandat.fmcarer.ADAPTER.Care_schedule_ADAPTER;
import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;
import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.RESPONSE.CareScheludeResponse;
import com.nguyentuandat.fmcarer.RESPONSE.ChildrenResponse;
import com.nguyentuandat.fmcarer.RESPONSE.SingleCareScheludeResponse;

import org.json.JSONObject; // Thêm import này để phân tích JSON error body

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Care_schedule_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    private Care_schedule_ADAPTER adapter;
    private ApiService apiService;
    private String bearerToken;

    private List<Care_Schelude> scheduleList = new ArrayList<>();
    private List<Children> childrenList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.care_schedule_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerCareList);
        btnAdd = view.findViewById(R.id.btnAddCare);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy token từ SharedPreferences
        bearerToken = getBearerToken();
        // Khởi tạo ApiService
        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        // Khởi tạo Adapter và gán cho RecyclerView.
        // childrenList được truyền vào adapter để nó có thể sử dụng cho việc hiển thị tên trẻ
        // và đặc biệt là trong dialog sửa/tạo nhắc nhở (spinner chọn trẻ).
        adapter = new Care_schedule_ADAPTER(getContext(), scheduleList, childrenList, bearerToken);
        recyclerView.setAdapter(adapter);

        // Xử lý sự kiện nhấn nút thêm mới
        btnAdd.setOnClickListener(v -> {
            openCreateReminderDialog();
        });

        // Tải danh sách trẻ em và sau đó tải danh sách nhắc nhở.
        // Việc tải trẻ em trước là quan trọng để `childrenList` có dữ liệu cho spinner
        // trong dialog tạo/sửa nhắc nhở.
        loadChildren();
        loadSchedules(); // Gọi song song, adapter sẽ tự cập nhật khi childrenList thay đổi

        return view;
    }

    // Lấy Bearer Token từ SharedPreferences
    private String getBearerToken() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        return null;
    }

    // Tải toàn bộ danh sách nhắc nhở từ API
    private void loadSchedules() {
        if (bearerToken == null) {
            Toast.makeText(getContext(), "Token không hợp lệ. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getAllReminders(bearerToken).enqueue(new Callback<CareScheludeResponse>() {
            @Override
            public void onResponse(@NonNull Call<CareScheludeResponse> call, @NonNull Response<CareScheludeResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Xóa dữ liệu cũ và thêm mới
                    scheduleList.clear();
                    scheduleList.addAll(response.body().getData());
                    adapter.setData(scheduleList); // Cập nhật dữ liệu và refresh RecyclerView
                } else {
                    String errorMessage = "Không lấy được lịch nhắc.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            Log.e("LOAD_SCHEDULES", "Error Body: " + errorBodyString);
                            JSONObject jObjError = new JSONObject(errorBodyString);
                            if (jObjError.has("message")) {
                                errorMessage += " " + jObjError.getString("message");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("LOAD_SCHEDULES", "Error parsing errorBody: ", e);
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CareScheludeResponse> call, @NonNull Throwable t) {
                Log.e("LOAD_SCHEDULES", "Lỗi kết nối API: ", t);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Tải danh sách trẻ em từ API
    private void loadChildren() {
        if (bearerToken == null) return; // Không có token thì không tải

        apiService.getChildrenByUser(bearerToken).enqueue(new Callback<ChildrenResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChildrenResponse> call, @NonNull Response<ChildrenResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    childrenList.clear(); // Xóa dữ liệu cũ
                    childrenList.addAll(response.body().getData()); // Thêm dữ liệu mới
                    adapter.notifyDataSetChanged(); // Cập nhật adapter (đặc biệt cho spinner nếu dùng)
                } else {
                    String errorMessage = "Không lấy được danh sách trẻ.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            Log.e("LOAD_CHILDREN", "Error Body: " + errorBodyString);
                            JSONObject jObjError = new JSONObject(errorBodyString);
                            if (jObjError.has("message")) {
                                errorMessage += " " + jObjError.getString("message");
                            }
                        }
                    } catch (Exception e) {
                        Log.e("LOAD_CHILDREN", "Error parsing errorBody: ", e);
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChildrenResponse> call, @NonNull Throwable t) {
                Log.e("LOAD_CHILDREN", "Lỗi kết nối API: ", t);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Mở dialog tạo nhắc nhở mới
    private void openCreateReminderDialog() {
        // Kiểm tra xem có trẻ em nào để tạo nhắc nhở không
        if (childrenList.isEmpty()) {
            Toast.makeText(requireContext(), " Vui lòng thêm trẻ trước khi tạo nhắc nhở.", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diglog_create_care_schelude, null);
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
        TextView btnCreate = dialogView.findViewById(R.id.btn_create_reminder); // Nút "Tạo nhắc nhở"

        // --- Cài đặt Spinners ---

        // Loại nhắc nhở
        String[] types = {"eat", "sleep", "bathe", "vaccine", "other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(typeAdapter);

        // Loại lặp lại
        String[] repeats = {"none", "daily", "weekly", "monthly"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, repeats);
        spinnerRepeatType.setAdapter(repeatAdapter);

        // Danh sách trẻ em cho Spinner (từ childrenList đã tải)
        List<String> childNames = new ArrayList<>();
        for (Children child : childrenList) {
            childNames.add(child.getName());
        }
        ArrayAdapter<String> childAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, childNames);
        spinnerChild.setAdapter(childAdapter);


        // Listener cho spinner Type để ẩn/hiện layout_custom_type
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layoutCustomType.setVisibility(types[position].equals("other") ? View.VISIBLE : View.GONE);
                // Xóa nội dung custom type nếu không phải loại "other"
                if (!types[position].equals("other")) {
                    edtCustomType.setText("");
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // DatePickerDialog cho edtDate
        edtDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, day) -> {
                edtDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)); // Định dạng yyyy-MM-dd
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // TimePickerDialog cho edtTime
        edtTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (view, hour, minute) -> {
                edtTime.setText(String.format("%02d:%02d", hour, minute)); // Định dạng HH:mm
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show(); // "true" cho chế độ 24 giờ
        });

        // --- Listener cho nút "Tạo nhắc nhở" ---
        btnCreate.setOnClickListener(v -> {
            // Lấy dữ liệu từ các trường nhập liệu
            String type = spinnerType.getSelectedItem().toString();
            String note = edtNote.getText().toString().trim();
            String customType = edtCustomType.getText().toString().trim();
            String date = edtDate.getText().toString().trim();
            String time = edtTime.getText().toString().trim();
            String repeatType = spinnerRepeatType.getSelectedItem().toString();
            boolean repeat = checkboxRepeat.isChecked();
            String childId = childrenList.get(spinnerChild.getSelectedItemPosition()).get_id(); // Lấy _id của trẻ

            // Kiểm tra validation cơ bản
            if (type.equals("other") && customType.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập loại khác", Toast.LENGTH_SHORT).show();
                return;
            }
            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn ngày và giờ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chuẩn bị dữ liệu gửi lên API (Map<String, Object>)
            Map<String, Object> data = new HashMap<>();
            data.put("child_id", childId);
            data.put("type", type);
            // Gửi null nếu không phải loại "other"
            data.put("custom_type", type.equals("other") && !customType.isEmpty() ? customType : null);
            data.put("note", note);
            data.put("reminder_date", date);
            data.put("reminder_time", time);
            data.put("repeat", repeat);
            data.put("repeat_type", repeatType);

            // Gọi API tạo nhắc nhở
            apiService.createReminder(bearerToken, data).enqueue(new Callback<SingleCareScheludeResponse>() {
                @Override
                public void onResponse(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Response<SingleCareScheludeResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(requireContext(), "Đã tạo nhắc nhở thành công!", Toast.LENGTH_SHORT).show();
                        loadSchedules(); // Tải lại danh sách để cập nhật RecyclerView
                        dialog.dismiss(); // Đóng dialog
                    } else {
                        String errorMessage = "Tạo nhắc nhở thất bại.";
                        try {
                            if (response.errorBody() != null) {
                                String errorBodyString = response.errorBody().string();
                                Log.e("CREATE_REMINDER_API", "Lỗi phản hồi API: " + errorBodyString);
                                // Cố gắng phân tích JSON để lấy message cụ thể từ backend
                                JSONObject jObjError = new JSONObject(errorBodyString);
                                if (jObjError.has("message")) {
                                    errorMessage += " " + jObjError.getString("message");
                                } else if (jObjError.has("error") && jObjError.getJSONObject("error").has("message")) {
                                    errorMessage += " " + jObjError.getJSONObject("error").getString("message");
                                }
                            }
                        } catch (Exception e) {
                            Log.e("CREATE_REMINDER_API", "Lỗi khi đọc hoặc phân tích errorBody: ", e);
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Throwable t) {
                    Log.e("CREATE_REMINDER_API", "Lỗi kết nối API: ", t);
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}