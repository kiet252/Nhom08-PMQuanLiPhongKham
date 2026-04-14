package com.example.nhom08_quanlyphongkham;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class admin_manage_staff extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.admin_manage_staff);
        RecyclerView recyclerView = findViewById(R.id.rvEmployee);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// Giả sử lấy dữ liệu từ database trả về 1 ArrayList<Staff>
        List<StaffItem> dataFromServer = new ArrayList<>();

        StaffItemAdapter adapter = new StaffItemAdapter(dataFromServer);
        recyclerView.setAdapter(adapter);
    }

}
