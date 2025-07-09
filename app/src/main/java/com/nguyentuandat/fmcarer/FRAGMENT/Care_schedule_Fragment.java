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
import android.widget.CheckBox;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

        // Lấy token chuẩn
        bearerToken = getBearerToken();
        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        adapter = new Care_schedule_ADAPTER(getContext(), scheduleList, childrenList, bearerToken);
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            // Có thể show dialog tạo mới ở đây (nếu bạn đã có sẵn dialog)
            openCreateReminderDialog();

        });

        loadChildren(); // Gọi trước để có danh sách trẻ
        loadSchedules(); // Sau đó load lịch

        return view;
    }

    private String getBearerToken() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        return null;
    }

    private void loadSchedules() {
        if (bearerToken == null) {
            Toast.makeText(getContext(), "Token không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getAllReminders(bearerToken).enqueue(new Callback<CareScheludeResponse>() {
            @Override
            public void onResponse(Call<CareScheludeResponse> call, Response<CareScheludeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    scheduleList = response.body().getData();
                    adapter.setData(scheduleList);
                } else {
                    Toast.makeText(requireContext(), "Không lấy được lịch nhắc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CareScheludeResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChildren() {
        if (bearerToken == null) return;

        apiService.getChildrenByUser(bearerToken).enqueue(new Callback<ChildrenResponse>() {
            @Override
            public void onResponse(Call<ChildrenResponse> call, Response<ChildrenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    childrenList = response.body().getData();
                    adapter.notifyDataSetChanged(); // cập nhật lại spinner nếu dùng
                } else {
                    Toast.makeText(requireContext(), "Không lấy được danh sách trẻ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChildrenResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openCreateReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diglog_create_care_schelude, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        Spinner spinnerChild = dialogView.findViewById(R.id.spinner_child);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);
        Spinner spinnerRepeatType = dialogView.findViewById(R.id.spinner_repeat_type);
        TextInputEditText edtNote = dialogView.findViewById(R.id.edt_note);
        TextInputEditText edtCustomType = dialogView.findViewById(R.id.edt_custom_type);
        TextInputEditText edtDate = dialogView.findViewById(R.id.edt_date);
        TextInputEditText edtTime = dialogView.findViewById(R.id.edt_time);
        TextInputLayout layoutCustomType = dialogView.findViewById(R.id.layout_custom_type);
        MaterialCheckBox checkboxRepeat = dialogView.findViewById(R.id.checkbox_repeat);
        TextView btnCreate = dialogView.findViewById(R.id.btn_create_reminder);

        // Setup spinners & date/time
        String[] types = {"eat", "sleep", "bathe", "vaccine", "other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(typeAdapter);

        String[] repeats = {"none", "daily", "weekly", "monthly"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, repeats);
        spinnerRepeatType.setAdapter(repeatAdapter);

        // Trẻ em từ danh sách đã có
        List<String> childNames = new ArrayList<>();
        for (Children child : childrenList) {
            childNames.add(child.getName());
        }
        ArrayAdapter<String> childAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, childNames);
        spinnerChild.setAdapter(childAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layoutCustomType.setVisibility(types[position].equals("other") ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        edtDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, day) -> {
                edtDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        edtTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (view, hour, minute) -> {
                edtTime.setText(String.format("%02d:%02d", hour, minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        // Nút tạo
        btnCreate.setOnClickListener(v -> {
            if (childrenList.isEmpty()) {
                Toast.makeText(requireContext(), "Chưa có trẻ em nào!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            String type = spinnerType.getSelectedItem().toString();
            String note = edtNote.getText().toString().trim();
            String customType = edtCustomType.getText().toString().trim();
            String date = edtDate.getText().toString().trim();
            String time = edtTime.getText().toString().trim();
            String repeatType = spinnerRepeatType.getSelectedItem().toString();
            boolean repeat = checkboxRepeat.isChecked();
            String childId = childrenList.get(spinnerChild.getSelectedItemPosition()).get_id();

            if (type.equals("other") && customType.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập loại khác", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("child_id", childId);
            data.put("type", type);
            data.put("custom_type", type.equals("other") ? customType : ""); // 🔥 Luôn có field này
            data.put("note", note);
            data.put("reminder_date", date);
            data.put("reminder_time", time);
            data.put("repeat", repeat);
            data.put("repeat_type", repeatType);

// 👉 Log toàn bộ dữ liệu gửi lên server
            Log.d("CREATE_REMINDER_DATA", "Body gửi API: " + data.toString());
            Log.d("CREATE_REMINDER_DATA", "Token: " + bearerToken);


            // 🛠️ Gọi API tạo lịch và log lỗi nếu có
            apiService.createReminder(bearerToken, data).enqueue(new Callback<SingleCareScheludeResponse>() {
                @Override
                public void onResponse(Call<SingleCareScheludeResponse> call, Response<SingleCareScheludeResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(requireContext(), "Đã tạo nhắc nhở", Toast.LENGTH_SHORT).show();
                        loadSchedules(); // refresh list
                        dialog.dismiss();
                    } else {
                        try {
                            String error = response.errorBody() != null ? response.errorBody().string() : "Không có lỗi cụ thể";
                            Log.e("CREATE_REMINDER", "Lỗi tạo nhắc nhở: " + error);
                            Toast.makeText(requireContext(), "Tạo thất bại: " + error, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e("CREATE_REMINDER", "Lỗi khi đọc errorBody: ", e);
                            Toast.makeText(requireContext(), "Tạo thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SingleCareScheludeResponse> call, Throwable t) {
                    Log.e("CREATE_REMINDER", "Lỗi kết nối API: ", t);
                    Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

}
