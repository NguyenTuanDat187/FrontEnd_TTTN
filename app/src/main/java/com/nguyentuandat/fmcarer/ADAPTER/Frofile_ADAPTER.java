package com.nguyentuandat.fmcarer.ADAPTER;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nguyentuandat.fmcarer.MODEL.User;
import com.nguyentuandat.fmcarer.R;

import java.util.ArrayList;
import java.util.List;

public class Frofile_ADAPTER extends RecyclerView.Adapter<Frofile_ADAPTER.SubUserViewHolder> {

    private static final String TAG = "Frofile_ADAPTER";
    private final List<User> subuserList;
    private final Context context;
    private final OnSubUserActionListener listener;

    public interface OnSubUserActionListener {
        void onActionWithPasswordConfirmation(User subuser, String actionType, String password, AlertDialog passwordDialog);
    }

    public Frofile_ADAPTER(Context context, List<User> list, OnSubUserActionListener listener) {
        this.context = context;
        this.subuserList = list != null ? list : new ArrayList<>();
        this.listener = listener;
        Log.d(TAG, "Frofile_ADAPTER initialized with " + this.subuserList.size() + " items.");
    }

    @NonNull
    @Override
    public SubUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sub_account, parent, false);
        Log.d(TAG, "onCreateViewHolder: New SubUserViewHolder created.");
        return new SubUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubUserViewHolder holder, int position) {
        if (subuserList == null || position >= subuserList.size()) {
            Log.e(TAG, "onBindViewHolder: subuserList is null or position is out of bounds. Position: " + position + ", List size: " + (subuserList != null ? subuserList.size() : "null"));
            return;
        }

        User sub = subuserList.get(position);

        if (sub == null) {
            Log.w(TAG, "onBindViewHolder: User object at position " + position + " is null. Skipping bind.");
            return;
        }

        Log.d(TAG, "onBindViewHolder: Binding user: " + sub.getFullname() + " at position " + position);

        if (holder.textName != null) {
            holder.textName.setText(sub.getFullname() != null ? sub.getFullname() : "Tên không rõ");
        } else {
            Log.e(TAG, "onBindViewHolder: textName is NULL for item at position " + position);
        }

        if (holder.textPhone != null) {
            holder.textPhone.setText("SĐT: " + (sub.getNumberphone() != null ? sub.getNumberphone() : "Chưa có"));
        } else {
            Log.e(TAG, "onBindViewHolder: textPhone is NULL for item at position " + position);
        }

        // Đảm bảo ánh xạ đúng textRelationship
        if (holder.textRelationship != null) { // Thêm kiểm tra null cho textRelationship
            holder.textRelationship.setText("Quan hệ: " + (sub.getRelationship() != null ? sub.getRelationship() : "Không rõ"));
        } else {
            Log.e(TAG, "onBindViewHolder: textRelationship is NULL for item at position " + position);
        }


        if (holder.imageAvatar != null) {
            if (sub.getImage() != null && !sub.getImage().isEmpty()) {
                Glide.with(context)
                        .load(sub.getImage())
                        .placeholder(R.drawable.taikhoan)
                        .error(R.drawable.taikhoan)
                        .into(holder.imageAvatar);
                Log.d(TAG, "Loaded image for " + sub.getFullname() + ": " + sub.getImage());
            } else {
                holder.imageAvatar.setImageResource(R.drawable.taikhoan);
                Log.w(TAG, "No image URL for " + sub.getFullname() + ". Using default placeholder.");
            }
        } else {
            Log.e(TAG, "onBindViewHolder: imageAvatar is NULL for item at position " + position + ". Cannot load image.");
        }

        // Hiện tại chỉ log sự kiện long click, chưa thực hiện hành động gì phức tạp
        holder.itemView.setOnLongClickListener(v -> {
            // Thay vì gọi showOptionDialog ngay, bạn có thể gọi listener
            // để Fragment xử lý (nếu bạn muốn Fragment kiểm soát hoàn toàn)
            // hoặc giữ nguyên showOptionDialog như hiện tại.
            // Để đơn giản hóa việc hiển thị danh sách, tôi sẽ giữ nguyên cách gọi hiện tại.
            showOptionDialog(sub);
            Log.d(TAG, "Long click on item: " + sub.getFullname());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return subuserList.size();
    }

    public void updateData(List<User> newSubusers) {
        this.subuserList.clear();
        if (newSubusers != null) {
            this.subuserList.addAll(newSubusers);
            Log.d(TAG, "updateData: Added " + newSubusers.size() + " new items.");
        } else {
            Log.d(TAG, "updateData: newSubusers list is null. No new items added.");
        }
        notifyDataSetChanged();
        Log.d(TAG, "updateData: Adapter data updated. Current size: " + subuserList.size());
    }

    public static class SubUserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar;
        TextView textName, textPhone, textRelationship; // Đảm bảo textRelationship được khai báo và ánh xạ

        public SubUserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.imageSubAvatar);
            textName = itemView.findViewById(R.id.textSubName);
            textPhone = itemView.findViewById(R.id.textSubPhone);

            if (imageAvatar == null) Log.e(TAG, "SubUserViewHolder: imageSubAvatar (R.id.imageSubAvatar) is NULL!");
            if (textName == null) Log.e(TAG, "SubUserViewHolder: textName (R.id.textSubName) is NULL!");
            if (textPhone == null) Log.e(TAG, "SubUserViewHolder: textPhone (R.id.textSubPhone) is NULL!");
            if (textRelationship == null) Log.e(TAG, "SubUserViewHolder: textRelationship (R.id.textSubRelationship) is NULL!");
            Log.d(TAG, "SubUserViewHolder initialized. All views checked for null.");
        }
    }

    // Giữ nguyên các phương thức này để hiển thị dialog option,
    // nhưng việc xử lý chính (edit/delete) sẽ được chuyển lên Fragment thông qua listener
    private void showOptionDialog(User subuser) {
        new AlertDialog.Builder(context)
                .setTitle("Tùy chọn")
                .setItems(new String[]{"✏️ Chỉnh sửa", "🗑️ Xóa"}, (dialog, which) -> {
                    if (which == 0) { // Người dùng chọn "Chỉnh sửa"
                        showPasswordConfirmationDialog(subuser, "edit");
                    } else if (which == 1) { // Người dùng chọn "Xóa"
                        showConfirmDelete(subuser);
                    }
                }).show();
    }

    private void showConfirmDelete(User subuser) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản phụ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    showPasswordConfirmationDialog(subuser, "delete");
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showPasswordConfirmationDialog(User subuser, String actionType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.diglog_confirmpassword, null);
        builder.setView(dialogView);

        final AlertDialog passwordDialog = builder.create();

        if (passwordDialog.getWindow() != null) {
            passwordDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
            passwordDialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        TextInputEditText edtConfirmPassword = dialogView.findViewById(R.id.edtConfirmPassword);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        if ("edit".equals(actionType)) {
            tvDialogTitle.setText("Xác nhận để chỉnh sửa");
            tvDialogMessage.setText("Vui lòng nhập mật khẩu của bạn để xác nhận chỉnh sửa tài khoản phụ.");
        } else if ("delete".equals(actionType)) {
            tvDialogTitle.setText("Xác nhận để xóa");
            tvDialogMessage.setText("Vui lòng nhập mật khẩu của bạn để xác nhận xóa tài khoản phụ.");
        }

        btnCancel.setOnClickListener(v -> {
            passwordDialog.dismiss();
            Log.d(TAG, "Password confirmation dialog cancelled for action: " + actionType);
        });

        btnConfirm.setOnClickListener(v -> {
            String enteredPassword = edtConfirmPassword.getText().toString().trim();
            if (enteredPassword.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập mật khẩu.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Password confirmation: Empty password entered for action: " + actionType);
            } else {
                if (listener != null) {
                    // Truyền passwordDialog về Fragment để nó quản lý việc đóng dialog
                    listener.onActionWithPasswordConfirmation(subuser, actionType, enteredPassword, passwordDialog);
                    Log.d(TAG, "Password submitted for confirmation and action: " + actionType + " for subuser: " + subuser.getFullname());
                } else {
                    Log.e(TAG, "showPasswordConfirmationDialog: Listener is null. Cannot call onActionWithPasswordConfirmation.");
                    Toast.makeText(context, "Lỗi nội bộ: Không thể xác nhận mật khẩu.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        passwordDialog.show();
    }
}