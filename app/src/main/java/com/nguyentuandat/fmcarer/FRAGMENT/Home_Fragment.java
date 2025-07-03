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
import com.nguyentuandat.fmcarer.MODEL_CALL_API.MultiImageUploadResponse;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.PostRequest;
import com.nguyentuandat.fmcarer.MODEL_CALL_API.PostResponse;
import com.nguyentuandat.fmcarer.NETWORK.ApiService;
import com.nguyentuandat.fmcarer.NETWORK.RetrofitClient;
import com.nguyentuandat.fmcarer.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private Spinner spinnerVisibility;
    private AlertDialog postDialog;
    private ApiService apiService;
    private ProgressBar progressBar;
    private Button btnPostSubmit;

    private String userId, userName, userAvatar; // Giữ lại để hiển thị UI nhưng không truyền lên API tạo bài viết
    private String selectedVisibility = "public";

    private TextView textViewCreatePost;
    private ImageView imgAvatar;
    private RecyclerView recyclerCommunityPosts;
    private Post_ADAPTER postAdapter;
    private final List<Post> postList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        SharedPreferences prefs = requireActivity().getSharedPreferences("USER", Context.MODE_PRIVATE);
        userId = prefs.getString("_id", "");
        userName = prefs.getString("fullname", "");
        userAvatar = prefs.getString("image", "");

        // ✅ THÊM LOG ĐỂ XÁC NHẬN DỮ LIỆU ĐANG ĐƯỢC LẤY RA TỪ SHARED PREFERENCES
        Log.d("Home_Fragment_User", "UserId: " + userId);
        Log.d("Home_Fragment_User", "UserName (from SharedPreferences): " + userName);
        Log.d("Home_Fragment_User", "UserAvatar (from SharedPreferences): " + userAvatar);


        textViewCreatePost = view.findViewById(R.id.textViewCreatePost);
        imgAvatar = view.findViewById(R.id.imgAvatar);

        recyclerCommunityPosts = view.findViewById(R.id.recyclerCommunityPosts);
        recyclerCommunityPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new Post_ADAPTER(getContext(), postList, apiService);
        recyclerCommunityPosts.setAdapter(postAdapter);

        if (userAvatar != null && !userAvatar.isEmpty() && !userAvatar.equals("null")) {
            Glide.with(this)
                    .load(userAvatar)
                    .placeholder(R.drawable.taikhoan)
                    .error(R.drawable.taikhoan)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.taikhoan);
        }
        // Các dòng Log.d này đã được thêm ở trên, không cần lặp lại
        // userId = prefs.getString("_id", "");
        // userName = prefs.getString("fullname", "");
        // userAvatar = prefs.getString("image", "");

        // Log.d("UserDebug", "UserId: " + userId);
        // Log.d("UserDebug", "UserName: " + userName);
        // Log.d("UserDebug", "UserAvatar: " + userAvatar);

        textViewCreatePost.setOnClickListener(v -> showCreatePostDialog());
        loadPosts();
        return view;
    }

    private void showCreatePostDialog() {
        if (!isAdded() || getContext() == null) return;

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

        tvUserName.setText(userName);
        if (userAvatar != null && !userAvatar.isEmpty() && !userAvatar.equals("null")) {
            Glide.with(this)
                    .load(userAvatar)
                    .placeholder(R.drawable.taikhoan)
                    .error(R.drawable.taikhoan)
                    .into(imgDialogAvatar);
        } else {
            imgDialogAvatar.setImageResource(R.drawable.taikhoan);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.visibility_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVisibility.setAdapter(adapter);
        spinnerVisibility.setSelection(adapter.getPosition("Cộng đồng"));

        spinnerVisibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choice = parent.getItemAtPosition(position).toString();
                switch (choice) {
                    case "Cộng đồng":
                        selectedVisibility = "public";
                        break;
                    case "Gia đình":
                        selectedVisibility = "family";
                        break;
                    case "Riêng tư":
                        selectedVisibility = "private";
                        break;
                    default:
                        selectedVisibility = "public";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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

        if (!selectedImageUris.isEmpty()) {
            uploadImagesToServer(content);
        } else {
            createPostWithMediaUrls(content, new ArrayList<>());
        }
    }

    private void uploadImagesToServer(String postContent) {
        if (!isAdded() || getContext() == null) {
            showToastSafe("Fragment không còn hoạt động.");
            if (btnPostSubmit != null) btnPostSubmit.setEnabled(true);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    Log.e("Upload", "Could not open input stream for Uri: " + uri.toString());
                    continue;
                }

                byte[] fileBytes = getBytesFromInputStream(inputStream);
                if (fileBytes == null || fileBytes.length == 0) {
                    Log.e("Upload", "Could not read bytes or file is empty for Uri: " + uri.toString());
                    continue;
                }

                String fileName = getFileName(uri);
                if (fileName == null || fileName.isEmpty()) {
                    fileName = "image_" + System.currentTimeMillis() + ".jpg";
                }

                String mimeType = requireContext().getContentResolver().getType(uri);
                if (mimeType == null || !mimeType.startsWith("image/")) {
                    mimeType = "image/jpeg";
                }

                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), fileBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("images", fileName, requestFile);
                imageParts.add(body);

            } catch (IOException e) {
                Log.e("Upload", "Error preparing file for upload: " + e.getMessage(), e);
                showToastSafe("Lỗi xử lý ảnh: " + e.getMessage());
                if (btnPostSubmit != null) btnPostSubmit.setEnabled(true);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                return;
            }
        }

        if (imageParts.isEmpty()) {
            showToastSafe("Không có ảnh hợp lệ để tải lên. Tạo bài đăng không kèm ảnh.");
            createPostWithMediaUrls(postContent, new ArrayList<>());
            return;
        }

        apiService.uploadMultipleImages(imageParts).enqueue(new Callback<MultiImageUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<MultiImageUploadResponse> call, @NonNull Response<MultiImageUploadResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<String> uploadedUrls = response.body().getImageUrls();
                    Log.d("UploadSuccess", "Uploaded URLs: " + uploadedUrls);
                    createPostWithMediaUrls(postContent, uploadedUrls);
                } else {
                    String errorMsg = "Lỗi upload ảnh: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + Objects.requireNonNull(response.errorBody()).string();
                        }
                    } catch (IOException e) {
                        Log.e("UploadError", "Error reading error body: " + e.getMessage());
                    }
                    Log.e("UploadError", errorMsg);
                    showToastSafe(errorMsg);
                    if (btnPostSubmit != null) btnPostSubmit.setEnabled(true);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MultiImageUploadResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("UploadFailure", "Network error during image upload: " + t.getMessage(), t);
                showToastSafe("Lỗi mạng khi tải ảnh lên: " + t.getMessage());
                if (btnPostSubmit != null) btnPostSubmit.setEnabled(true);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void createPostWithMediaUrls(String content, List<String> mediaUrls) {
        // ✅ CHỈNH SỬA DÒNG NÀY: Không truyền userName và userAvatar nữa
        PostRequest request = new PostRequest(
                userId,
                content,
                selectedVisibility,
                mediaUrls
        );

        apiService.createPost(request).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(@NonNull Call<PostResponse> call, @NonNull Response<PostResponse> response) {
                if (!isAdded()) return;
                if (btnPostSubmit != null) btnPostSubmit.setEnabled(true);
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showToastSafe("Đăng bài thành công");
                    if (postDialog != null && postDialog.isShowing()) {
                        postDialog.dismiss();
                    }
                    loadPosts();
                } else {
                    String errorMsg = "Lỗi đăng bài: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + Objects.requireNonNull(response.errorBody()).string();
                        }
                    } catch (IOException e) {
                        Log.e("CreatePostError", "Error reading error body: " + e.getMessage());
                    }
                    Log.e("CreatePostError", errorMsg);
                    showToastSafe(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (btnPostSubmit != null) btnPostSubmit.setEnabled(true);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e("CreatePostFailure", "Network error during post creation: " + t.getMessage(), t);
                showToastSafe("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void loadPosts() {
        apiService.getAllPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("LoadPosts", "API call successful. Received " + response.body().size() + " posts from backend.");
                    postList.clear();
                    postList.addAll(response.body());
                    Log.d("LoadPosts", "postList now has " + postList.size() + " items.");

                    if (!postList.isEmpty()) {
                        Log.d("LoadPosts", "First post content: " + postList.get(0).getContent());
                        Log.d("LoadPosts", "First post mediaUrls: " + postList.get(0).getMediaUrls());
                    }

                    postAdapter.notifyDataSetChanged();
                    Log.d("LoadPosts", "notifyDataSetChanged called.");

                } else {
                    showToastSafe("Không thể tải bài viết: " + response.code());
                    Log.e("LoadPosts", "Failed to load posts: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showToastSafe("Lỗi mạng khi tải bài viết: " + t.getMessage());
                Log.e("LoadPosts", "Network error loading posts: " + t.getMessage(), t);
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
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }

            if (rvSelectedImagesPreview != null) {
                if (!selectedImageUris.isEmpty()) {
                    rvSelectedImagesPreview.setVisibility(View.VISIBLE);
                    if (selectedImageAdapter != null) {
                        selectedImageAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Home_Fragment", "selectedImageAdapter is null unexpectedly in onActivityResult! Re-initializing.");
                        Context context = getContext();
                        if (context != null) {
                            selectedImageAdapter = new SelectedImageAdapter(context, selectedImageUris);
                            rvSelectedImagesPreview.setAdapter(selectedImageAdapter);
                            selectedImageAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("Home_Fragment", "Context is null, cannot re-initialize adapter.");
                        }
                    }
                } else {
                    rvSelectedImagesPreview.setVisibility(View.GONE);
                }
            } else {
                Log.e("Home_Fragment", "rvSelectedImagesPreview is null in onActivityResult. Dialog might be dismissed.");
            }
        }
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

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
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e("GetFileName", "Error getting file name from content URI: " + e.getMessage());
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
            if (result == null || result.isEmpty()) {
                result = "image_" + System.currentTimeMillis() + ".jpg";
            } else if (!result.contains(".")) {
                result += ".jpg";
            }
        }
        return result;
    }
}