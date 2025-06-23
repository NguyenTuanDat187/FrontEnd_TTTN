package com.nguyentuandat.fmcarer.FRAGMENT;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nguyentuandat.fmcarer.ADAPTER.Children_ADAPTER;
import com.nguyentuandat.fmcarer.MODEL.Children;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Children_List_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private Children_ADAPTER adapter;
    private FloatingActionButton btnAddChild;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.children_list_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerChildren);
        btnAddChild = view.findViewById(R.id.btnAddChild);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Children_ADAPTER(getContext());
        recyclerView.setAdapter(adapter);

        loadChildrenList();

        btnAddChild.setOnClickListener(v -> showAddOrUpdateDialog(null));

        return view;
    }

    private void loadChildrenList() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        String userId = prefs.getString("_id", "");

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getChildrenByUser(userId).enqueue(new Callback<List<Children>>() {
            @Override
            public void onResponse(Call<List<Children>> call, Response<List<Children>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body());
                } else {
                    Toast.makeText(getContext(), "Không có dữ liệu trẻ em", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Children>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddOrUpdateDialog(@Nullable Children childToEdit) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_or_update_child);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        }

        TextInputEditText edtName = dialog.findViewById(R.id.edtChildName);
        TextInputEditText edtDob = dialog.findViewById(R.id.edtChildDOB);
        RadioGroup genderGroup = dialog.findViewById(R.id.rgGender);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveChild);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);

        if (edtName == null || edtDob == null || genderGroup == null || btnSave == null || btnCancel == null) {
            Toast.makeText(getContext(), "Không thể hiển thị form, thiếu layout", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị DatePicker khi click vào edtDob
        edtDob.setOnClickListener(v -> showDatePickerDialog(edtDob));

        if (childToEdit != null) {
            edtName.setText(childToEdit.getName());
            edtDob.setText(childToEdit.getDob());

            switch (childToEdit.getGender()) {
                case "male":
                    genderGroup.check(R.id.rbMale);
                    break;
                case "female":
                    genderGroup.check(R.id.rbFemale);
                    break;
                default:
                    genderGroup.check(R.id.rbOther);
                    break;
            }
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String dob = edtDob.getText().toString().trim();
            String gender = "other";

            int checkedId = genderGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.rbMale) gender = "male";
            else if (checkedId == R.id.rbFemale) gender = "female";

            if (name.isEmpty() || dob.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (childToEdit == null) {
                addChildToServer(name, dob, gender);
            } else {
                updateChildToServer(childToEdit.get_id(), name, dob, gender);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePickerDialog(TextInputEditText edtDob) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                    edtDob.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void addChildToServer(String name, String dob, String gender) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        String userId = prefs.getString("_id", "");

        Children child = new Children(userId, name, dob, gender);
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);

        api.addChild(child).enqueue(new Callback<Children>() {
            @Override
            public void onResponse(Call<Children> call, Response<Children> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                    loadChildrenList();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi thêm dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Children> call, Throwable t) {
                Toast.makeText(getContext(), "Thêm thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChildToServer(String childId, String name, String dob, String gender) {
        Children updatedChild = new Children(name, dob, gender);
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);

        api.updateChild(childId, updatedChild).enqueue(new Callback<Children>() {
            @Override
            public void onResponse(Call<Children> call, Response<Children> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    loadChildrenList();
                } else {
                    Toast.makeText(getContext(), "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Children> call, Throwable t) {
                Toast.makeText(getContext(), "Cập nhật thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
