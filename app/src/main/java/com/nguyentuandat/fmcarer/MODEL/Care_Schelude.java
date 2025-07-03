package com.nguyentuandat.fmcarer.MODEL;

import com.google.gson.annotations.SerializedName;

public class Care_Schelude {

    @SerializedName("_id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("child_id")
    private Children child;

    @SerializedName("type")
    private String type;

    @SerializedName("custom_type")
    private String customType;

    @SerializedName("note")
    private String note;

    @SerializedName("reminder_date")
    private String reminderDate;

    @SerializedName("reminder_time")
    private String reminderTime;

    @SerializedName("repeat")
    private boolean repeat;

    @SerializedName("repeat_type")
    private String repeatType;

    @SerializedName("is_completed")
    private boolean completed;

    // ✅ Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Children getChild() {
        return child;
    }

    public void setChild(Children child) {
        this.child = child;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCustomType() {
        return customType;
    }

    public void setCustomType(String customType) {
        this.customType = customType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
