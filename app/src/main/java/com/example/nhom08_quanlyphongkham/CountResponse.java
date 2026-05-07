package com.example.nhom08_quanlyphongkham;

import com.google.gson.annotations.SerializedName;

public class CountResponse {
    @SerializedName("count")
    private long count;

    public long getCount() { return count; }
}