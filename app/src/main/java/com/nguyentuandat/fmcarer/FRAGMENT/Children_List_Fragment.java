package com.nguyentuandat.fmcarer.FRAGMENT;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.nguyentuandat.fmcarer.RESPONSE.ChildrenResponse;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Children_List_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private Children_ADAPTER adapter;
    private FloatingActionButton btnAddChild;
    private List<Children> childrenList = new ArrayList<>();

    private ApiService apiService;
    private String bearerToken;  // token chuẩn dạng "Bearer <token>"

    private static final String TAG = "ChildrenListFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.children_list_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerChildren);
        btnAddChild = view.findViewById(R.id.btnAddChild);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new Children_ADAPTER(getContext());
        recyclerView.setAdapter(adapter);

        // Lấy token một cách chuẩn xác từ đúng SharedPreferences
        bearerToken = getBearerToken();
        Log.d(TAG, "Lấy token: " + bearerToken);

        if (bearerToken == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập hoặc token không hợp lệ", Toast.LENGTH_SHORT).show();
            // Bạn có thể xử lý chuyển màn hình đăng nhập hoặc disable UI ở đây nếu muốn
        }

        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        adapter.setOnChildActionListener(new Children_ADAPTER.OnChildActionListener() {
            @Override
            public void onEditChild(Children child) {
                showAddOrUpdateDialog(child);
            }

            @Override
            public void onDeleteChild(Children child) {
                showDeleteConfirmation(child);
            }
        });

        loadChildrenList();

        btnAddChild.setOnClickListener(v -> showAddOrUpdateDialog(null));

        return view;
    }

    // Hàm lấy token dạng "Bearer <token>" từ SharedPreferences đúng file
    private String getBearerToken() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");
        Log.d(TAG, "Token lấy từ SharedPreferences: " + token);
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        return null; // trả về null nếu không có token hợp lệ
    }

    private void loadChildrenList() {
        if (bearerToken == null) {
            Toast.makeText(getContext(), "Token không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.getChildrenByUser(bearerToken).enqueue(new Callback<ChildrenResponse>() {
            @Override
            public void onResponse(Call<ChildrenResponse> call, Response<ChildrenResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    childrenList = response.body().getData();
                    adapter.setData(childrenList);
                } else if (response.code() == 401) {
                    Toast.makeText(getContext(), "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                    // Xử lý logout hoặc chuyển sang màn hình login nếu cần
                } else {
                    Toast.makeText(getContext(), "Không có dữ liệu trẻ em", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Response không thành công, code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChildrenResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi kết nối loadChildrenList", t);
            }
        });
    }

    // Các hàm thêm/sửa/xóa và dialog giữ nguyên như bạn đã có, chỉ cần đảm bảo gọi api với bearerToken đúng

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

            if (bearerToken == null) {
                Toast.makeText(requireContext(), "Token không hợp lệ, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
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
                    String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    edtDob.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void addChildToServer(String name, String dob, String gender) {
        if (bearerToken == null) {
            Toast.makeText(getContext(), "Token không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Children child = new Children();
        child.setName(name);
        child.setDob(dob);
        child.setGender(gender);
        child.setAvatar_url(null); // vẫn có thể set nếu bạn dùng ảnh


        apiService.addChild(bearerToken, child).enqueue(new Callback<Children>() {
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
        if (bearerToken == null) {
            Toast.makeText(getContext(), "Token không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Children updatedChild = new Children(name, dob, gender);
        apiService.updateChild(bearerToken, childId, updatedChild).enqueue(new Callback<Children>() {
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

    private void showDeleteConfirmation(Children child) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa trẻ này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteChildFromServer(child.get_id()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteChildFromServer(String childId) {
        if (bearerToken == null) {
            Toast.makeText(getContext(), "Token không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteChild(bearerToken, childId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                    loadChildrenList();
                } else {
                    Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        loadChildrenList();
    }

}
