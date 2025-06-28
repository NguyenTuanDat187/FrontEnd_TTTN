package com.nguyentuandat.fmcarer.FRAGMENT;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyentuandat.fmcarer.ADAPTER.Care_schedule_ADAPTER;
import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;
import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.CareScheludeResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.ChildrenResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.SingleCareScheludeResponse;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Care_schedule_Fragment extends Fragment {

    private RecyclerView recyclerCareList;
    private FloatingActionButton btnAddCare;
    private Care_schedule_ADAPTER careScheduleAdapter;
    private final List<Care_Schelude> scheList = new ArrayList<>();
    private final List<Children> childList = new ArrayList<>();
    private ApiService apiService;
    private String selectedChildId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.care_schedule_fragment, container, false);

        apiService = RetrofitClient.getInstance().create(ApiService.class);
        recyclerCareList = view.findViewById(R.id.recyclerCareList);
        recyclerCareList.setLayoutManager(new LinearLayoutManager(requireContext()));
        careScheduleAdapter = new Care_schedule_ADAPTER(requireContext(), scheList, childList);
        recyclerCareList.setAdapter(careScheduleAdapter);

        btnAddCare = view.findViewById(R.id.btnAddCare);
        btnAddCare.setOnClickListener(v -> openCreateReminderDialog());

        loadChildren();
        return view;
    }

    private void loadChildren() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        String userId = prefs.getString("_id", "");

        apiService.getChildrenByUser(userId).enqueue(new Callback<ChildrenResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChildrenResponse> call, @NonNull Response<ChildrenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    childList.clear();
                    childList.addAll(response.body().getData());
                    careScheduleAdapter.notifyDataSetChanged();
                    loadSchedules();
                } else {
                    Toast.makeText(requireContext(), "Không lấy được danh sách trẻ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChildrenResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSchedules() {
        apiService.getAllReminders().enqueue(new Callback<CareScheludeResponse>() {
            @Override
            public void onResponse(@NonNull Call<CareScheludeResponse> call, @NonNull Response<CareScheludeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    scheList.clear();
                    scheList.addAll(response.body().getData());
                    careScheduleAdapter.setData(scheList);
                } else {
                    Toast.makeText(requireContext(), "Không lấy được lịch nhắc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CareScheludeResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCreateReminderDialog() {
        if (childList.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng thêm trẻ trước", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.diglog_create_care_schelude, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
    dialog.show();

        Spinner spinnerChild = dialogView.findViewById(R.id.spinner_child);
        TextInputEditText edtNote = dialogView.findViewById(R.id.edt_note);
        TextInputEditText edtCustomType = dialogView.findViewById(R.id.edt_custom_type);
        TextInputLayout layoutCustomType = dialogView.findViewById(R.id.layout_custom_type);
        TextInputEditText edtDate = dialogView.findViewById(R.id.edt_date);
        TextInputEditText edtTime = dialogView.findViewById(R.id.edt_time);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_type);
        Spinner spinnerRepeatType = dialogView.findViewById(R.id.spinner_repeat_type);
        CheckBox checkboxRepeat = dialogView.findViewById(R.id.checkbox_repeat);
        MaterialButton btnCreate = dialogView.findViewById(R.id.btn_create_reminder);

        List<String> types = Arrays.asList("eat", "sleep", "bathe", "vaccine", "other");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(typeAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layoutCustomType.setVisibility(types.get(position).equals("other") ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("none", "daily", "weekly", "monthly"));
        spinnerRepeatType.setAdapter(repeatAdapter);

        List<String> childNames = new ArrayList<>();
        for (Children child : childList) {
            childNames.add(child.getName());
        }
        ArrayAdapter<String> childAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, childNames);
        spinnerChild.setAdapter(childAdapter);

        selectedChildId = childList.get(0).get_id(); // Default chọn phần tử đầu tiên
        spinnerChild.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChildId = childList.get(position).get_id();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        edtDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                edtDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        edtTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (view, hour, minute) -> {
                edtTime.setText(String.format("%02d:%02d", hour, minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        btnCreate.setOnClickListener(v -> {
            String type = spinnerType.getSelectedItem().toString();
            String note = edtNote.getText().toString().trim();
            String customType = edtCustomType.getText().toString().trim();
            String date = edtDate.getText().toString().trim();
            String time = edtTime.getText().toString().trim();
            String repeatType = spinnerRepeatType.getSelectedItem().toString();
            boolean repeat = checkboxRepeat.isChecked();

            if (selectedChildId == null || date.isEmpty() || time.isEmpty() || (type.equals("other") && customType.isEmpty())) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("child_id", selectedChildId);
            data.put("type", type);
            if (type.equals("other")) data.put("custom_type", customType);
            data.put("note", note);
            data.put("reminder_date", date);
            data.put("reminder_time", time);
            data.put("repeat", repeat);
            data.put("repeat_type", repeatType);

            apiService.createReminder(data).enqueue(new Callback<SingleCareScheludeResponse>() {
                @Override
                public void onResponse(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Response<SingleCareScheludeResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(requireContext(), "Tạo nhắc thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadSchedules(); // Load lại danh sách sau khi tạo thành công
                    } else {
                        Toast.makeText(requireContext(), "Tạo nhắc thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
