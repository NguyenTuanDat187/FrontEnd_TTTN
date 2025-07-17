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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_Fragment extends Fragment {

    private static final String TAG = "Home_Fragment_DEBUG"; // Đổi tag để dễ lọc log

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
    // Mặc định là "community" để gửi lên admin duyệt
    private String selectedVisibility = "community"; // Giữ mặc định này, nhưng sẽ được ghi đè/xác nhận khi dialog mở

    private TextView textViewCreatePost;
    private ImageView imgAvatar;
    private RecyclerView recyclerCommunityPosts;
    private Post_ADAPTER postAdapter;
    private final List<Post> postList = new ArrayList<>();

    private static final String PREF_USER_SESSION = "user_session";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        Log.d(TAG, "onCreateView: Fragment view created.");

        // Load user data from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_USER_SESSION, Context.MODE_PRIVATE);
        userId = prefs.getString("_id", "");
        userName = prefs.getString("fullname", "");
        userAvatar = prefs.getString("image", ""); // Assume 'image' stores avatar URL
        Log.d(TAG, "onCreateView: User loaded - ID: " + userId + ", Name: " + userName + ", Avatar: " + userAvatar);

        // Initialize ApiService
        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        Log.d(TAG, "onCreateView: ApiService initialized.");

        // Initialize UI components
        textViewCreatePost = view.findViewById(R.id.textViewCreatePost);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        recyclerCommunityPosts = view.findViewById(R.id.recyclerCommunityPosts);

        // Set up RecyclerView for posts
        if (getContext() != null) {
            recyclerCommunityPosts.setLayoutManager(new LinearLayoutManager(getContext()));
            postAdapter = new Post_ADAPTER(getContext(), postList, apiService);
            recyclerCommunityPosts.setAdapter(postAdapter);
            Log.d(TAG, "onCreateView: RecyclerView and Post_ADAPTER initialized.");
        } else {
            Log.e(TAG, "onCreateView: Context is null, cannot set up RecyclerView.");
        }

        // Load user avatar
        if (userAvatar != null && !userAvatar.isEmpty() && !userAvatar.equals("null")) {
            Glide.with(this).load(userAvatar).placeholder(R.drawable.taikhoan).into(imgAvatar);
            Log.d(TAG, "onCreateView: Glide loaded user avatar: " + userAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.taikhoan);
            Log.d(TAG, "onCreateView: Using default avatar for user.");
        }

        // Set click listener for create post
        textViewCreatePost.setOnClickListener(v -> {
            showCreatePostDialog();
            Log.d(TAG, "onCreateView: 'Create Post' button clicked.");
        });

        // Load posts initially
        loadPosts();
        Log.d(TAG, "onCreateView: Calling loadPosts().");

        return view;
    }

    private void showCreatePostDialog() {
        Log.d(TAG, "showCreatePostDialog: Opening create post dialog.");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_post, null);
        builder.setView(dialogView);
        postDialog = builder.create();
        postDialog.show();

        // Reset state for new post
        selectedImageUris.clear();
        // Không đặt selectedVisibility = "community" ở đây nữa.
        // Nó sẽ được xác định bởi spinner và logic bên dưới.
        Log.d(TAG, "showCreatePostDialog: Image URI list cleared.");

        // Initialize dialog UI components
        TextView tvUserName = dialogView.findViewById(R.id.tvUserName);
        edtPostContent = dialogView.findViewById(R.id.edtPostContent);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        btnPostSubmit = dialogView.findViewById(R.id.btnPostSubmit);
        ImageView imgDialogAvatar = dialogView.findViewById(R.id.imgAvatar);
        spinnerVisibility = dialogView.findViewById(R.id.spinnerVisibility);
        progressBar = dialogView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        Log.d(TAG, "showCreatePostDialog: Dialog views initialized.");

        // Set up image preview RecyclerView
        rvSelectedImagesPreview = dialogView.findViewById(R.id.rvSelectedImagesPreview);
        if (getContext() != null) {
            rvSelectedImagesPreview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            selectedImageAdapter = new SelectedImageAdapter(requireContext(), selectedImageUris);
            rvSelectedImagesPreview.setAdapter(selectedImageAdapter);
            Log.d(TAG, "showCreatePostDialog: Image preview RecyclerView initialized.");
        } else {
            Log.e(TAG, "showCreatePostDialog: Context is null, cannot set up image preview RecyclerView.");
        }
        rvSelectedImagesPreview.setVisibility(View.GONE);


        // Set user info in dialog
        tvUserName.setText(userName);
        Glide.with(this).load(userAvatar).placeholder(R.drawable.taikhoan).into(imgDialogAvatar);
        Log.d(TAG, "showCreatePostDialog: User name and avatar set in dialog.");

        // Set up visibility spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.visibility_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVisibility.setAdapter(adapter);

        // Set default value for spinner to "Cộng đồng"
        int defaultPosition = adapter.getPosition("Cộng đồng");
        if (defaultPosition != -1) {
            spinnerVisibility.setText(adapter.getItem(defaultPosition), false);
            // >>> LOG MỚI: Đảm bảo giá trị default được ánh xạ và log ngay lập tức
            String defaultSelectionText = adapter.getItem(defaultPosition).toString();
            mapVisibilitySelection(defaultSelectionText); // Gọi hàm để map và log
            Log.d(TAG, "showCreatePostDialog: Spinner default position set to: " + defaultSelectionText + " -> Mapped selectedVisibility: " + selectedVisibility);
        } else {
            // >>> LOG MỚI: Nếu không tìm thấy "Cộng đồng" trong adapter
            Log.w(TAG, "showCreatePostDialog: 'Cộng đồng' not found in visibility_options array. Defaulting selectedVisibility to: " + selectedVisibility);
        }

        // Handle spinner item selection
        spinnerVisibility.setOnItemClickListener((parent, view, position, id) -> {
            String selection = parent.getItemAtPosition(position).toString();
            mapVisibilitySelection(selection); // Gọi hàm để map và log
            Log.d(TAG, "showCreatePostDialog: Visibility selected by user: " + selection + " -> Mapped to: " + selectedVisibility);
        });

        // Set click listeners
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
            Log.d(TAG, "showCreatePostDialog: 'Select Image' button clicked, starting image picker.");
        });

        btnPostSubmit.setOnClickListener(v -> {
            // >>> LOG MỚI: Kiểm tra giá trị cuối cùng của selectedVisibility trước khi submit
            Log.d(TAG, "submitPost: Final selectedVisibility before API call: " + selectedVisibility);
            submitPost();
            Log.d(TAG, "showCreatePostDialog: 'Submit Post' button clicked.");
        });
    }

    // Hàm mới để ánh xạ và log giá trị selectedVisibility
    private void mapVisibilitySelection(String selection) {
        if (selection.equals("Bạn bè")) {
            selectedVisibility = "friends";
        } else if (selection.equals("Riêng tư")) {
            selectedVisibility = "private";
        } else if (selection.equals("Cộng đồng")) {
            selectedVisibility = "community";
        } else {
            // Trường hợp fallback nếu có lựa chọn khác không được handle (ví dụ: "Công khai")
            selectedVisibility = "public"; // Đảm bảo có giá trị mặc định hợp lệ
        }
        // Log được đưa ra ngoài hàm này hoặc trong hàm gọi nó để kiểm soát tốt hơn
    }


    private void submitPost() {
        String content = edtPostContent.getText().toString().trim();
        Log.d(TAG, "submitPost: Post content: '" + content + "'. Image count: " + selectedImageUris.size());
        Log.d(TAG, "submitPost: Current selectedVisibility for submission: " + selectedVisibility); // Thêm log ở đây

        if (content.isEmpty() && selectedImageUris.isEmpty()) {
            showToastSafe("Vui lòng nhập nội dung hoặc chọn ảnh để đăng bài.");
            Log.w(TAG, "submitPost: Content and images are empty. Aborting post.");
            return;
        }

        btnPostSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_USER_SESSION, Context.MODE_PRIVATE);
        String currentToken = prefs.getString("token", "NO_TOKEN_FOUND");
        Log.d(TAG, "submitPost: Token before API call: " + (currentToken.equals("NO_TOKEN_FOUND") ? "Not Found" : currentToken.substring(0, Math.min(currentToken.length(), 10)) + "..."));


        if (!selectedImageUris.isEmpty()) {
            Log.d(TAG, "submitPost: Images selected, calling uploadImagesToServer.");
            uploadImagesToServer(content);
        } else {
            Log.d(TAG, "submitPost: No images selected, calling createPostWithMediaUrls directly.");
            createPostWithMediaUrls(content, new ArrayList<>());
        }
    }

    private void uploadImagesToServer(String postContent) {
        Log.d(TAG, "uploadImagesToServer: Starting image upload for " + selectedImageUris.size() + " images.");
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    throw new IOException("Unable to open input stream for URI: " + uri);
                }
                byte[] fileBytes = getBytesFromInputStream(inputStream);
                inputStream.close();
                String fileName = getFileName(uri);
                String mimeType = requireContext().getContentResolver().getType(uri);
                if (mimeType == null || mimeType.isEmpty()) mimeType = "image/jpeg";
                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), fileBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("images", fileName, requestFile);
                imageParts.add(body);
                Log.d(TAG, "uploadImagesToServer: Added image part - FileName: " + fileName + ", MimeType: " + mimeType);
            } catch (IOException e) {
                showToastSafe("Lỗi xử lý ảnh: " + e.getMessage());
                btnPostSubmit.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "uploadImagesToServer: Error processing image URI: " + uri.toString(), e);
                return;
            }
        }

        apiService.uploadMultipleImages(imageParts).enqueue(new Callback<MultiImageUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<MultiImageUploadResponse> call, @NonNull Response<MultiImageUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "uploadImagesToServer -> onResponse: Images uploaded successfully. Number of URLs: " + response.body().getImageUrls().size());
                    createPostWithMediaUrls(postContent, response.body().getImageUrls());
                } else {
                    String errorMsg = "Lỗi upload ảnh: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += " - " + errorBody;
                            Log.e(TAG, "uploadImagesToServer -> onResponse: API Error Body: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "uploadImagesToServer -> onResponse: Error reading errorBody", e);
                    }
                    showToastSafe(errorMsg);
                    btnPostSubmit.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "uploadImagesToServer -> onResponse: Image upload failed: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MultiImageUploadResponse> call, @NonNull Throwable t) {
                showToastSafe("Lỗi mạng khi upload ảnh: " + t.getMessage());
                btnPostSubmit.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "uploadImagesToServer -> onFailure: Network error during image upload: " + t.getMessage(), t);
            }
        });
    }

    private void createPostWithMediaUrls(String content, List<String> mediaUrls) {
        PostRequest request = new PostRequest(userId, content, selectedVisibility, mediaUrls);
        // >>> LOG QUAN TRỌNG NHẤT: Kiểm tra Request object trước khi gửi
        Log.d(TAG, "createPostWithMediaUrls: Sending PostRequest: " +
                "userId=" + userId +
                ", content='" + content + "'" +
                ", selectedVisibility='" + selectedVisibility + "'" + // Đây là giá trị sẽ được gửi đi
                ", mediaUrlsCount=" + mediaUrls.size());

        apiService.createPost(request).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(@NonNull Call<PostResponse> call, @NonNull Response<PostResponse> response) {
                btnPostSubmit.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Post createdPost = response.body().getPost();
                    String statusMessage;
                    if (createdPost != null && "pending".equals(createdPost.getStatus())) {
                        statusMessage = "Bài viết đã được gửi để xem xét.";
                        Log.d(TAG, "createPostWithMediaUrls -> onResponse: Post created with status: pending. Post ID: " + createdPost.get_id());
                    } else {
                        statusMessage = "Đăng bài thành công.";
                        Log.d(TAG, "createPostWithMediaUrls -> onResponse: Post created successfully. Post ID: " + (createdPost != null ? createdPost.get_id() : "N/A"));
                    }
                    showToastSafe(statusMessage);

                    if (postDialog != null && postDialog.isShowing()) {
                        postDialog.dismiss();
                        Log.d(TAG, "createPostWithMediaUrls -> onResponse: Post dialog dismissed.");
                    }
                    loadPosts(); // Reload posts after successful creation
                    Log.d(TAG, "createPostWithMediaUrls -> onResponse: Calling loadPosts() after successful post creation.");
                } else {
                    String errorMessage = "Lỗi đăng bài: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            errorMessage += " - " + errorBody;
                            Log.e(TAG, "createPostWithMediaUrls -> onResponse: API Error Body: " + errorBody);
                        } catch (IOException e) {
                            Log.e(TAG, "createPostWithMediaUrls -> onResponse: Error reading errorBody", e);
                        }
                    }
                    showToastSafe(errorMessage);
                    Log.e(TAG, "createPostWithMediaUrls -> onResponse: Post creation failed: " + errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostResponse> call, @NonNull Throwable t) {
                showToastSafe("Lỗi kết nối hoặc xử lý dữ liệu: " + t.getMessage());
                btnPostSubmit.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "createPostWithMediaUrls -> onFailure: Error during post creation: " + t.getMessage(), t);
            }
        });
    }

    private void loadPosts() {
        Log.d(TAG, "loadPosts: Fetching all posts from API.");
        apiService.getAllPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "loadPosts -> onResponse: Successfully fetched " + response.body().size() + " posts.");
                    postList.clear();
                    List<Post> fetchedPosts = response.body();

                    List<Post> filteredPosts = fetchedPosts.stream()
                            .filter(post -> {
                                String postCreatorId = (post.getUser() != null) ? post.getUser().getId() : "N/A_User_Object_Null";
                                String currentPostId = post.get_id() != null ? post.get_id() : "N/A_Post_ID";
                                String postStatus = post.getStatus() != null ? post.getStatus() : "N/A_Status";
                                String postVisibility = post.getVisibility() != null ? post.getVisibility() : "N/A_Visibility";

                                boolean isOwnPost = userId != null && userId.equals(postCreatorId);
                                boolean shouldDisplay = false;

                                // Display logic:
                                // 1. Posts that are ACTIVE and have 'public' OR 'community' visibility (after being approved)
                                // 2. Any posts created by the current user (including pending/rejected)
                                // 3. Posts that are ACTIVE and have 'friends' visibility AND are by the current user (this is needed for consistency if 'friends' is not filtered by backend)
                                //    Or, if you want to show "friends" posts to all friends, you'd need a list of friend IDs from the current user.
                                // 4. Posts that are ACTIVE and have 'private' visibility AND are by the current user (already covered by isOwnPost)

                                if ("active".equals(postStatus)) {
                                    if ("public".equals(postVisibility) || "community".equals(postVisibility)) {
                                        shouldDisplay = true; // Show all active public/community posts
                                    } else if ("friends".equals(postVisibility) || "private".equals(postVisibility)) {
                                        if (isOwnPost) {
                                            shouldDisplay = true; // Show own active private/friends posts
                                        }
                                        // TODO: If you have a friends list, check if postCreatorId is in current user's friends list for 'friends' visibility
                                    }
                                }

                                // Always display own posts, regardless of status or visibility (for editing/tracking)
                                if (isOwnPost) {
                                    shouldDisplay = true;
                                }


                                if (shouldDisplay) {
                                    Log.d(TAG, "loadPosts -> filter: Post ID " + currentPostId + " displayed. Status: " + postStatus + ", Visibility: " + postVisibility + ", Is Own Post: " + isOwnPost);
                                } else {
                                    Log.d(TAG, "loadPosts -> filter: Post ID " + currentPostId + " filtered out. Status: " + postStatus + ", Visibility: " + postVisibility + ", Is Own Post: " + isOwnPost);
                                }
                                return shouldDisplay;
                            })
                            .collect(Collectors.toList());

                    // Sort posts by creation date in reverse order (newest first)
                    filteredPosts.sort(Comparator.comparing(Post::getCreated_at).reversed());
                    Log.d(TAG, "loadPosts -> onResponse: Filtered and sorted " + filteredPosts.size() + " posts.");

                    postList.addAll(filteredPosts);
                    if (postAdapter != null) {
                        postAdapter.notifyDataSetChanged();
                        Log.d(TAG, "loadPosts -> onResponse: Adapter notified of data change.");
                    } else {
                        Log.e(TAG, "loadPosts -> onResponse: postAdapter is NULL. Cannot notifyDataSetChanged().");
                    }

                } else {
                    String errorBodyString = "N/A";
                    try {
                        if (response.errorBody() != null) {
                            errorBodyString = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "loadPosts -> onResponse: Error reading errorBody", e);
                    }
                    showToastSafe("Không thể tải bài viết: " + response.code());
                    Log.e(TAG, "loadPosts -> onResponse: Failed to load posts. Response code: " + response.code() + ", Error body: " + errorBodyString);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                showToastSafe("Lỗi mạng khi tải bài viết: " + t.getMessage());
                Log.e(TAG, "loadPosts -> onFailure: Network error during post fetching: " + t.getMessage(), t);
            }
        });
    }

    private void showToastSafe(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            Log.d(TAG, "showToastSafe: Displaying toast: " + message);
        } else {
            Log.w(TAG, "showToastSafe: Cannot display toast, fragment not added or context is null. Message: " + message);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: RequestCode: " + requestCode + ", ResultCode: " + resultCode + ", Data: " + (data != null ? "present" : "null"));
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUris.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                }
                Log.d(TAG, "onActivityResult: Selected " + count + " images from clip data.");
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
                Log.d(TAG, "onActivityResult: Selected single image from data.");
            }

            if (!selectedImageUris.isEmpty() && selectedImageAdapter != null) {
                rvSelectedImagesPreview.setVisibility(View.VISIBLE);
                selectedImageAdapter.notifyDataSetChanged();
                Log.d(TAG, "onActivityResult: Image preview visible, adapter notified. Total selected: " + selectedImageUris.size());
            } else {
                rvSelectedImagesPreview.setVisibility(View.GONE);
                Log.d(TAG, "onActivityResult: No images selected or adapter is null. Image preview hidden.");
            }
        } else {
            Log.d(TAG, "onActivityResult: Image selection cancelled or failed.");
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
        if (uri == null) {
            Log.e(TAG, "getFileName: URI is null.");
            return "image_" + System.currentTimeMillis() + ".jpg";
        }
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) result = cursor.getString(nameIndex);
                }
            } catch (Exception e) {
                Log.e(TAG, "getFileName: Error getting file name for URI: " + uri.toString(), e);
            }
        }
        if (result == null || result.isEmpty()) {
            result = "image_" + System.currentTimeMillis() + ".jpg";
            Log.w(TAG, "getFileName: Could not get display name for URI, using generated name: " + result);
        }
        return result;
    }

    // Lifecycle methods for logging (unchanged)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: Fragment attached.");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Fragment created.");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Fragment started.");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Fragment resumed.");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Fragment paused.");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Fragment stopped.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Fragment view destroyed.");
        rvSelectedImagesPreview = null;
        selectedImageAdapter = null;
        edtPostContent = null;
        spinnerVisibility = null;
        postDialog = null;
        progressBar = null;
        btnPostSubmit = null;
        textViewCreatePost = null;
        imgAvatar = null;
        recyclerCommunityPosts = null;
        postAdapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Fragment destroyed.");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: Fragment detached.");
    }
}