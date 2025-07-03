package com.nguyentuandat.fmcarer.MODEL_CALL_API;

import com.google.gson.annotations.SerializedName;
import com.nguyentuandat.fmcarer.MODEL.Post;

public class PostResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("post") // Khớp với key JSON server trả về: "post": {...}
    private Post post;

    // Getter cho success
    public boolean isSuccess() {
        return success;
    }

    // Getter cho message
    public String getMessage() {
        return message;
    }

    // Getter cho post
    public Post getPost() {
        return post;
    }
}
