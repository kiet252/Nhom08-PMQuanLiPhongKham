package com.example.nhom08_quanlyphongkham.admin_reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {

    private List<ReportsActivity_Admin.DonKham> danhSach;

    public ReportsAdapter(List<ReportsActivity_Admin.DonKham> danhSach) {
        this.danhSach = danhSach;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportsActivity_Admin.DonKham dk = danhSach.get(position);

        // Đã xoá dòng set text cho Mã ĐK
        holder.tvNgay.setText(dk.getNgayKham());
        holder.tvBenhNhan.setText(dk.getTenBN());
        holder.tvTrangThai.setText(dk.getTrangThai());

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvTongTien.setText(fmt.format(dk.getTongTien()) + " đ");
    }

    @Override
    public int getItemCount() {
        return danhSach == null ? 0 : danhSach.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNgay, tvBenhNhan, tvTrangThai, tvTongTien; // Đã xoá tvMaDK

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đã xoá ánh xạ item_ma_dk
            tvNgay = itemView.findViewById(R.id.item_ngay);
            tvBenhNhan = itemView.findViewById(R.id.item_benh_nhan);
            tvTrangThai = itemView.findViewById(R.id.item_trang_thai);
            tvTongTien = itemView.findViewById(R.id.item_tong_tien);
        }
    }
}