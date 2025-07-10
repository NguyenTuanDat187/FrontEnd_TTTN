package com.nguyentuandat.fmcarer.FRAGMENT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyentuandat.fmcarer.ADAPTER.Post_ADAPTER;
import com.nguyentuandat.fmcarer.ADAPTER.SelectedImageAdapter;
import com.nguyentuandat.fmcarer.MODEL.Post;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.PostRequest;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;
import com.nguyentuandat.fmcarer.RESPONSE.MultiImageUploadResponse;
import com.nguyentuandat.fmcarer.RESPONSE.PostResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_Fragment extends Fragment {

    private static final int REQUEST_CODE_SELECT_IMAGE = 101;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private RecyclerView rvSelectedImagesPreview;
    private SelectedImageAdapter selectedImageAdapter;
    private EditText edtPostContent;
    private AutoCompleteTextView spinnerVisibility;
    private AlertDialog postDialog;
    private ApiService apiService;
    private ProgressBar progressBar;
    private Button btnPostSubmit;

    private String userId, userName, userAvatar;
    private String selectedVisibility = "public";

    private TextView textViewCreatePost;
    private ImageView imgAvatar;
    private RecyclerView recyclerCommunityPosts;
    private Post_ADAPTER postAdapter;
    private final List<Post> postList = new ArrayList<>();

    // ✅ Hằng số cho tên SharedPreferences, khớp với nơi Login_Activity lưu token và thông tin người dùng
    private static final String PREF_USER_SESSION = "user_session";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        // ✅ Lấy thông tin người dùng từ SharedPreferences "user_session" để nhất quán
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_USER_SESSION, Context.MODE_PRIVATE);
        userId = prefs.getString("_id", "");
        userName = prefs.getString("fullname", "");
        userAvatar = prefs.getString("image", "");

        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        textViewCreatePost = view.findViewById(R.id.textViewCreatePost);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        recyclerCommunityPosts = view.findViewById(R.id.recyclerCommunityPosts);
        recyclerCommunityPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new Post_ADAPTER(getContext(), postList, apiService);
        recyclerCommunityPosts.setAdapter(postAdapter);

        if (userAvatar != null && !userAvatar.isEmpty() && !userAvatar.equals("null")) {
            Glide.with(this).load(userAvatar).placeholder(R.drawable.taikhoan).into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.taikhoan);
        }

        textViewCreatePost.setOnClickListener(v -> showCreatePostDialog());
        loadPosts();

        return view;
    }

    private void showCreatePostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_post, null);
        builder.setView(dialogView);
        postDialog = builder.create();
        postDialog.show();

        selectedImageUris.clear();
        selectedVisibility = "public";

        TextView tvUserName = dialogView.findViewById(R.id.tvUserName);
        edtPostContent = dialogView.findViewById(R.id.edtPostContent);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        btnPostSubmit = dialogView.findViewById(R.id.btnPostSubmit);
        ImageView imgDialogAvatar = dialogView.findViewById(R.id.imgAvatar);
        spinnerVisibility = dialogView.findViewById(R.id.spinnerVisibility);
        progressBar = dialogView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        rvSelectedImagesPreview = dialogView.findViewById(R.id.rvSelectedImagesPreview);
        rvSelectedImagesPreview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        selectedImageAdapter = new SelectedImageAdapter(requireContext(), selectedImageUris);
        rvSelectedImagesPreview.setAdapter(selectedImageAdapter);
        rvSelectedImagesPreview.setVisibility(View.GONE);

        // Hiển thị tên và ảnh đại diện của người dùng hiện tại trên dialog
        tvUserName.setText(userName);
        Glide.with(this).load(userAvatar).placeholder(R.drawable.taikhoan).into(imgDialogAvatar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.visibility_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVisibility.setAdapter(adapter);
        int defaultPosition = adapter.getPosition("Cộng đồng");
        if (defaultPosition != -1) {
            spinnerVisibility.setText(adapter.getItem(defaultPosition), false);
            selectedVisibility = "public";
        }


        spinnerVisibility.setOnItemClickListener((parent, view, position, id) -> {
            String selection = parent.getItemAtPosition(position).toString();
            if (selection.equals("Bạn bè")) {
                selectedVisibility = "friends";
            } else if (selection.equals("Riêng tư")) {
                selectedVisibility = "private";
            } else { // "Cộng đồng"
                selectedVisibility = "public";
            }
        });


        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        });

        btnPostSubmit.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String content = edtPostContent.getText().toString().trim();
        if (content.isEmpty() && selectedImageUris.isEmpty()) {
            showToastSafe("Vui lòng nhập nội dung hoặc chọn ảnh để đăng bài.");
            return;
        }
        btnPostSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // ✅ Thêm log để kiểm tra token trước khi gửi bài
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_USER_SESSION, Context.MODE_PRIVATE);
        String currentToken = prefs.getString("token", "NO_TOKEN_FOUND");
        Log.d("Home_Fragment", "Token before submitPost: " + (currentToken.equals("NO_TOKEN_FOUND") ? "Not Found" : currentToken.substring(0, Math.min(currentToken.length(), 10)) + "..."));


        if (!selectedImageUris.isEmpty()) {
            uploadImagesToServer(content);
        } else {
            createPostWithMediaUrls(content, new ArrayList<>());
        }
    }

    private void uploadImagesToServer(String postContent) {
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                byte[] fileBytes = getBytesFromInputStream(inputStream);
                String fileName = getFileName(uri);
                String mimeType = requireContext().getContentResolver().getType(uri);
                if (mimeType == null) mimeType = "image/jpeg";
                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), fileBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("images", fileName, requestFile);
                imageParts.add(body);
            } catch (IOException e) {
                showToastSafe("Lỗi xử lý ảnh: " + e.getMessage());
                btnPostSubmit.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                return;
            }
        }

        apiService.uploadMultipleImages(imageParts).enqueue(new Callback<MultiImageUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<MultiImageUploadResponse> call, @NonNull Response<MultiImageUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    createPostWithMediaUrls(postContent, response.body().getImageUrls());
                } else {
                    showToastSafe("Lỗi upload ảnh: " + response.code());
                    btnPostSubmit.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MultiImageUploadResponse> call, @NonNull Throwable t) {
                showToastSafe("Lỗi mạng khi upload ảnh: " + t.getMessage());
                btnPostSubmit.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void createPostWithMediaUrls(String content, List<String> mediaUrls) {
        PostRequest request = new PostRequest(userId, content, selectedVisibility, mediaUrls);

        apiService.createPost(request).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(@NonNull Call<PostResponse> call, @NonNull Response<PostResponse> response) {
                btnPostSubmit.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showToastSafe("Đăng bài thành công");
                    if (postDialog != null && postDialog.isShowing()) postDialog.dismiss();
                    loadPosts();
                } else {
                    String errorMessage = "Lỗi đăng bài: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e("Home_Fragment", "Error reading errorBody", e);
                        }
                    }
                    showToastSafe(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostResponse> call, @NonNull Throwable t) {
                showToastSafe("Lỗi kết nối: " + t.getMessage());
                btnPostSubmit.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadPosts() {
        apiService.getAllPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postList.clear();
                    postList.addAll(response.body());
                    postAdapter.notifyDataSetChanged();
                } else {
                    showToastSafe("Không thể tải bài viết: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                showToastSafe("Lỗi mạng khi tải bài viết: " + t.getMessage());
            }
        });
    }

    private void showToastSafe(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUris.clear();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }
            if (!selectedImageUris.isEmpty() && selectedImageAdapter != null) {
                rvSelectedImagesPreview.setVisibility(View.VISIBLE);
                selectedImageAdapter.notifyDataSetChanged();
            } else {
                rvSelectedImagesPreview.setVisibility(View.GONE);
            }
        }
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) result = cursor.getString(nameIndex);
                }
            } catch (Exception e) {
                Log.e("GetFileName", "Lỗi lấy tên file: " + e.getMessage());
            }
        }
        if (result == null) {
            result = "image_" + System.currentTimeMillis() + ".jpg";
        }
        return result;
    }
}