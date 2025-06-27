    package com.nguyentuandat.fmcarer.ADAPTER;

    import android.annotation.SuppressLint;
    import android.app.AlertDialog;
    import android.app.DatePickerDialog;
    import android.app.TimePickerDialog;
    import android.content.Context;
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
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.google.android.material.checkbox.MaterialCheckBox;
    import com.google.android.material.textfield.TextInputEditText;
    import com.google.android.material.textfield.TextInputLayout;
    import com.nguyentuandat.fmcarer.MODEL.Care_Schelude;
    import com.nguyentuandat.fmcarer.MODEL.Children;
    import com.nguyentuandat.fmcarer.MODEL_CALL_API.ApiResponse;
    import com.nguyentuandat.fmcarer.MODEL_CALL_API.SingleCareScheludeResponse;
    import com.nguyentuandat.fmcarer.NETWORK.ApiService;
    import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
    import com.nguyentuandat.fmcarer.R;

    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    public class Care_schedule_ADAPTER extends RecyclerView.Adapter<Care_schedule_ADAPTER.ViewHolder> {

        private final Context context;
        private List<Care_Schelude> displayList;
        private final List<Children> childList;
        private final ApiService apiService;

        public Care_schedule_ADAPTER(Context context, List<Care_Schelude> scheduleList, List<Children> childList) {
            this.context = context;
            this.displayList = new ArrayList<>(scheduleList);
            this.childList = childList;
            this.apiService = RetrofitClient.getInstance().create(ApiService.class);
        }

        public void setData(List<Care_Schelude> newData) {
            this.displayList = new ArrayList<>(newData);
            notifyDataSetChanged();
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

            holder.tvChildName.setText(schedule.getChild().getName());

            String type = schedule.getType().equals("other") && schedule.getCustomType() != null
                    ? schedule.getCustomType()
                    : getTypeLabel(schedule.getType());
            holder.tvType.setText("Loại nhắc: " + type);

            String cleanDate = schedule.getReminderDate();
            if (cleanDate != null && cleanDate.contains("T")) {
                cleanDate = cleanDate.substring(0, cleanDate.indexOf("T"));
            }
            holder.tvDateTime.setText(cleanDate + " - " + schedule.getReminderTime());

            String repeatText = schedule.isRepeat()
                    ? "Lặp lại: " + getRepeatTypeLabel(schedule.getRepeatType())
                    : "Không lặp lại";
            holder.tvRepeat.setText(repeatText);

            Glide.with(context)
                    .load(schedule.getChild().getAvatar_url())
                    .placeholder(R.drawable.taikhoan)
                    .circleCrop()
                    .into(holder.imgChild);

            holder.btnDelete.setOnClickListener(v -> confirmDelete(schedule.getId(), position));
            holder.btnEdit.setOnClickListener(v -> openEditDialog(schedule, position));
        }


        private void confirmDelete(String scheduleId, int position) {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa nhắc nhở")
                    .setMessage("Bạn có chắc chắn muốn xóa?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        apiService.deleteReminder(scheduleId).enqueue(new Callback<ApiResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                                    // Tìm vị trí phần tử theo ID an toàn
                                    int indexToRemove = -1;
                                    for (int i = 0; i < displayList.size(); i++) {
                                        if (displayList.get(i).getId().equals(scheduleId)) {
                                            indexToRemove = i;
                                            break;
                                        }
                                    }

                                    if (indexToRemove != -1) {
                                        displayList.remove(indexToRemove);
                                        notifyItemRemoved(indexToRemove);
                                        Toast.makeText(context, "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Trường hợp không tìm thấy phần tử cần xóa
                                        Toast.makeText(context, "Không tìm thấy phần tử để xóa", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                                Toast.makeText(context, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }


        private void openEditDialog(Care_Schelude schedule, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.diglog_create_care_schelude, null);
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
            TextView btnUpdate = dialogView.findViewById(R.id.btn_create_reminder);

            // Set dữ liệu cũ
            edtNote.setText(schedule.getNote());
            edtCustomType.setText(schedule.getCustomType() != null ? schedule.getCustomType() : "");
            edtDate.setText(schedule.getReminderDate());
            edtTime.setText(schedule.getReminderTime());
            checkboxRepeat.setChecked(schedule.isRepeat());

            String[] types = {"eat", "sleep", "bathe", "vaccine", "other"};
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, types);
            spinnerType.setAdapter(typeAdapter);
            int typePos = schedule.getType().equals("other") ? 4 : java.util.Arrays.asList(types).indexOf(schedule.getType());
            spinnerType.setSelection(typePos);
            layoutCustomType.setVisibility(schedule.getType().equals("other") ? View.VISIBLE : View.GONE);

            spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    layoutCustomType.setVisibility(types[pos].equals("other") ? View.VISIBLE : View.GONE);
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });

            String[] repeats = {"none", "daily", "weekly", "monthly"};
            ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, repeats);
            spinnerRepeatType.setAdapter(repeatAdapter);
            spinnerRepeatType.setSelection(java.util.Arrays.asList(repeats).indexOf(schedule.getRepeatType()));

            List<String> childNames = new ArrayList<>();
            for (Children child : childList) {
                childNames.add(child.getName());
            }
            ArrayAdapter<String> childAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, childNames);
            spinnerChild.setAdapter(childAdapter);

            int childPos = 0;
            for (int i = 0; i < childList.size(); i++) {
                if (childList.get(i).get_id().equals(schedule.getChild().get_id())) {
                    childPos = i;
                    break;
                }
            }
            spinnerChild.setSelection(childPos);

            edtDate.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                    edtDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            });

            edtTime.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(context, (view, hour, minute) -> {
                    edtTime.setText(String.format("%02d:%02d", hour, minute));
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            });

            btnUpdate.setText("Cập nhật");
            btnUpdate.setOnClickListener(v -> {
                String type = spinnerType.getSelectedItem().toString();
                String note = edtNote.getText().toString().trim();
                String customType = edtCustomType.getText().toString().trim();
                String date = edtDate.getText().toString().trim();
                String time = edtTime.getText().toString().trim();
                String repeatType = spinnerRepeatType.getSelectedItem().toString();
                boolean repeat = checkboxRepeat.isChecked();
                String childId = childList.get(spinnerChild.getSelectedItemPosition()).get_id();

                if (type.equals("other") && customType.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập loại nhắc khác", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("child_id", childId);
                data.put("type", type);
                if (type.equals("other")) data.put("custom_type", customType);
                data.put("note", note);
                data.put("reminder_date", date);
                data.put("reminder_time", time);
                data.put("repeat", repeat);
                data.put("repeat_type", repeatType);

                apiService.updateReminder(schedule.getId(), data).enqueue(new Callback<SingleCareScheludeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Response<SingleCareScheludeResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Care_Schelude updatedItem = response.body().getData();

                            // Cập nhật theo ID an toàn, tránh lỗi vị trí
                            for (int i = 0; i < displayList.size(); i++) {
                                if (displayList.get(i).getId().equals(updatedItem.getId())) {
                                    displayList.set(i, updatedItem);
                                    notifyItemChanged(i);
                                    break;
                                }
                            }
                            Toast.makeText(context, "Đã cập nhật thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SingleCareScheludeResponse> call, @NonNull Throwable t) {
                        Toast.makeText(context, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }


        @Override
        public int getItemCount() {
            return displayList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgChild, btnEdit, btnDelete;
            TextView tvChildName, tvType, tvDateTime, tvRepeat;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imgChild = itemView.findViewById(R.id.imgChild);
                tvChildName = itemView.findViewById(R.id.tvChildName);
                tvType = itemView.findViewById(R.id.tvType);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                tvRepeat = itemView.findViewById(R.id.tvRepeat);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
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
