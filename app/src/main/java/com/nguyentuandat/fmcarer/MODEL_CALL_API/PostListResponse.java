package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.google.gson.annotations.SerializedName;
import com.nguyentuandat.fmcarer.MODEL.Post;

import java.util.List;

public class PostListResponse {
    private boolean success;
    private String message;

    @SerializedName("posts") // ⚠️ Đảm bảo backend trả về key này
    private List<Post> posts;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Post> getPosts() {
        return posts;
    }
}
