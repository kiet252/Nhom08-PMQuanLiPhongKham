package dashboard_fragment.admin_manage_device;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom08_quanlyphongkham.BaseActivity;
import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManageDeviceActivity extends BaseActivity {
    private DeviceApprovalRepository repository;
    private DeviceApprovalAdapter adapter;
    private final List<DeviceApprovalRequest> pendingRequests = new ArrayList<>();
    private RecyclerView recyclerView;
    private View layoutEmpty;
    private ProgressBar progressBar;
    private TextView tvPendingCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_device);

        repository = new DeviceApprovalRepository(this);
        recyclerView = findViewById(R.id.rvDeviceApprovals);
        layoutEmpty = findViewById(R.id.layoutDeviceApprovalEmpty);
        progressBar = findViewById(R.id.progressDeviceApprovals);
        tvPendingCount = findViewById(R.id.tvDeviceApprovalPendingCount);

        adapter = new DeviceApprovalAdapter();
        adapter.setOnRequestClickListener(this::showDetailDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBackDeviceApproval).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefreshDeviceApproval).setOnClickListener(v -> loadPendingRequests());

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        setLoading(true);
        repository.getPendingDeviceRequests().enqueue(new Callback<List<DeviceApprovalRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<DeviceApprovalRequest>> call,
                                   @NonNull Response<List<DeviceApprovalRequest>> response) {
                setLoading(false);
                //
                if (!response.isSuccessful() && response.errorBody() != null) {
                    try {
                        android.util.Log.e("SUPABASE_ERROR", "Mã lỗi: " + response.code() + " - Chi tiết: " + response.errorBody().string());
                    } catch (Exception e) { e.printStackTrace(); }
                } else if (response.body() != null) {
                    android.util.Log.d("SUPABASE_SUCCESS", "Kết nối thành công! Số lượng dòng: " + response.body().size());
                }
                //
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(AdminManageDeviceActivity.this, "Không thể tải yêu cầu duyệt thiết bị", Toast.LENGTH_SHORT).show();
                    updateList(new ArrayList<>());
                    return;
                }
                updateList(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<DeviceApprovalRequest>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AdminManageDeviceActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                updateList(new ArrayList<>());
            }
        });
    }

    private void updateList(List<DeviceApprovalRequest> requests) {
        pendingRequests.clear();
        pendingRequests.addAll(requests);
        adapter.setData(pendingRequests);
        
        long pendingCount = 0;
        for (DeviceApprovalRequest req : requests) {
            if (req != null && (req.getStatus() == null || "Chưa duyệt".equals(req.getStatus()))) {
                pendingCount++;
            }
        }
        tvPendingCount.setText(pendingCount + " yêu cầu chờ duyệt");
        
        layoutEmpty.setVisibility(pendingRequests.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(pendingRequests.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showDetailDialog(DeviceApprovalRequest request) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_device_approval_detail);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogDeviceTitle);
        if (tvTitle != null && "Face".equals(request.getTypeOfRequest())) {
            tvTitle.setText("Chi tiết yêu cầu duyệt khuôn mặt");
        }

        TextView tvStaffName = dialog.findViewById(R.id.tvDialogDeviceStaffName);
        TextView tvStaffId = dialog.findViewById(R.id.tvDialogDeviceStaffId);
        TextView tvAndroidId = dialog.findViewById(R.id.tvDialogDeviceAndroidId);
        TextView tvCreatedAt = dialog.findViewById(R.id.tvDialogDeviceCreatedAt);
        TextView tvStatus = dialog.findViewById(R.id.tvDialogDeviceStatus);
        MaterialButton btnApprove = dialog.findViewById(R.id.btnApproveDevice);
        MaterialButton btnReject = dialog.findViewById(R.id.btnRejectDevice);

        tvStaffName.setText(request.getStaffName());
        tvStaffId.setText(request.getStaffId());
        tvAndroidId.setText(request.getAndroidId());
        tvCreatedAt.setText(DeviceApprovalAdapter.formatDetailDate(request.getCreatedAt()));

        View layoutAndroidId = dialog.findViewById(R.id.layoutDialogDeviceAndroidId);
        View layoutFaceImage = dialog.findViewById(R.id.layoutDialogDeviceFaceImage);
        android.widget.ImageView imgFace = dialog.findViewById(R.id.imgDialogDeviceFace);

        if ("Face".equals(request.getTypeOfRequest())) {
            if (layoutAndroidId != null) layoutAndroidId.setVisibility(View.GONE);
            if (layoutFaceImage != null) layoutFaceImage.setVisibility(View.VISIBLE);

            String base64Image = request.getFaceImage();
            if (base64Image != null && !base64Image.trim().isEmpty()) {
                try {
                    byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (imgFace != null && decodedByte != null) {
                        imgFace.setImageBitmap(decodedByte);
                    }
                } catch (Exception e) {
                    android.util.Log.e("DeviceApproval", "Lỗi hiển thị ảnh khuôn mặt", e);
                    if (imgFace != null) {
                        imgFace.setImageResource(R.drawable.userprofile_ic_name);
                    }
                }
            } else {
                if (imgFace != null) {
                    imgFace.setImageResource(R.drawable.userprofile_ic_name);
                }
            }
        } else {
            if (layoutAndroidId != null) layoutAndroidId.setVisibility(View.VISIBLE);
            if (layoutFaceImage != null) layoutFaceImage.setVisibility(View.GONE);
        }

        if (tvStatus != null) {
            String status = request.getStatus();
            if ("Đã duyệt".equals(status)) {
                tvStatus.setText("Đã duyệt");
                tvStatus.setBackgroundResource(R.drawable.bg_device_status_approved);
                tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
                btnApprove.setEnabled(false);
                btnReject.setEnabled(false);
            } else if ("Từ chối".equals(status)) {
                tvStatus.setText("Từ chối");
                tvStatus.setBackgroundResource(R.drawable.bg_device_status_rejected);
                tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                btnApprove.setEnabled(false);
                btnReject.setEnabled(false);
            } else {
                tvStatus.setText("Chờ duyệt");
                tvStatus.setBackgroundResource(R.drawable.bg_device_status_pending);
                tvStatus.setTextColor(android.graphics.Color.parseColor("#D98B00"));
                btnApprove.setEnabled(true);
                btnReject.setEnabled(true);
            }
        }

        dialog.findViewById(R.id.btnCloseDeviceDialog).setOnClickListener(v -> dialog.dismiss());
        btnApprove.setOnClickListener(v -> confirmApprove(request, dialog));
        btnReject.setOnClickListener(v -> confirmReject(request, dialog));

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        if (window != null) {
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void confirmApprove(DeviceApprovalRequest request, Dialog dialog) {
        String title = "Face".equals(request.getTypeOfRequest()) ? "Duyệt khuôn mặt" : "Duyệt thiết bị";
        String message = "Face".equals(request.getTypeOfRequest()) ? "Bạn có chắc muốn duyệt khuôn mặt này cho nhân viên?" : "Bạn có chắc muốn duyệt thiết bị này cho nhân viên?";
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Duyệt", (d, which) -> approveRequest(request, dialog))
                .show();
    }

    private void confirmReject(DeviceApprovalRequest request, Dialog dialog) {
        String title = "Face".equals(request.getTypeOfRequest()) ? "Từ chối duyệt khuôn mặt" : "Từ chối yêu cầu";
        String message = "Face".equals(request.getTypeOfRequest()) ? "Bạn có chắc muốn từ chối yêu cầu duyệt khuôn mặt này?" : "Bạn có chắc muốn từ chối yêu cầu duyệt thiết bị này?";
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Từ chối", (d, which) -> rejectRequest(request, dialog))
                .show();
    }

    private void approveRequest(DeviceApprovalRequest request, Dialog dialog) {
        if (request == null) return;
        setLoading(true);
        if ("Face".equals(request.getTypeOfRequest())) {
            Object faceObj = request.getFace();
            String faceStr = null;
            if (faceObj instanceof String) {
                faceStr = (String) faceObj;
            } else if (faceObj != null) {
                try {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    faceStr = gson.toJson(faceObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            repository.approveProfileFace(request.getStaffId(), faceStr).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        setLoading(false);
                        Toast.makeText(AdminManageDeviceActivity.this, "Không thể cập nhật khuôn mặt nhân viên", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateRequestStatus(request, "Đã duyệt", "Đã duyệt khuôn mặt", dialog);
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    setLoading(false);
                    Toast.makeText(AdminManageDeviceActivity.this, "Lỗi kết nối khi duyệt khuôn mặt", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            repository.approveProfileDevice(request.getStaffId(), request.getAndroidId()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        setLoading(false);
                        Toast.makeText(AdminManageDeviceActivity.this, "Không thể cập nhật thiết bị nhân viên", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateRequestStatus(request, "Đã duyệt", "Đã duyệt thiết bị", dialog);
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    setLoading(false);
                    Toast.makeText(AdminManageDeviceActivity.this, "Lỗi kết nối khi duyệt thiết bị", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void rejectRequest(DeviceApprovalRequest request, Dialog dialog) {
        setLoading(true);
        updateRequestStatus(request, "Từ chối", "Đã từ chối yêu cầu", dialog);
    }

    private void updateRequestStatus(DeviceApprovalRequest request, String status, String successMessage, Dialog dialog) {
        repository.updateRequestStatus(request.getId(), status).enqueue(new Callback<ResponseBody>() {
             @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                setLoading(false);

                String url = call.request().url().toString();
                int code = response.code();
                String responseBodyStr = "";
                try {
                    if (response.body() != null) {
                        responseBodyStr = response.body().string();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                android.util.Log.d("SUPABASE_UPDATE_DIAGNOSTIC", "URL: " + url + " | Code: " + code + " | Body: " + responseBodyStr);

                if (!response.isSuccessful()) {
                    Toast.makeText(AdminManageDeviceActivity.this, "Không thể cập nhật trạng thái yêu cầu. Mã HTTP: " + code, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (responseBodyStr.trim().equals("[]") || responseBodyStr.trim().isEmpty()) {
                    Toast.makeText(AdminManageDeviceActivity.this, "Cảnh báo: Không có dòng nào được cập nhật trên database! (Lỗi RLS hoặc ID)", Toast.LENGTH_LONG).show();
                    android.util.Log.w("SUPABASE_UPDATE_DIAGNOSTIC", "Warning: 0 rows updated! Check filters or RLS policies.");
                }

                // Cập nhật hiển thị trạng thái trực tiếp trên dialog sang màu xanh lá/đỏ
                TextView tvStatus = dialog.findViewById(R.id.tvDialogDeviceStatus);
                if (tvStatus != null) {
                    if ("Đã duyệt".equals(status)) {
                        tvStatus.setText("Đã duyệt");
                        tvStatus.setBackgroundResource(R.drawable.bg_device_status_approved);
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
                    } else if ("Từ chối".equals(status)) {
                        tvStatus.setText("Từ chối");
                        tvStatus.setBackgroundResource(R.drawable.bg_device_status_rejected);
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                    }
                }

                // Vô hiệu hoá các nút bấm để người dùng thấy thao tác đã thành công và tránh click đúp
                MaterialButton btnApprove = dialog.findViewById(R.id.btnApproveDevice);
                MaterialButton btnReject = dialog.findViewById(R.id.btnRejectDevice);
                if (btnApprove != null) btnApprove.setEnabled(false);
                if (btnReject != null) btnReject.setEnabled(false);

                // Cập nhật trạng thái của đối tượng request trong danh sách và làm mới adapter
                request.setStatus(status);
                adapter.notifyDataSetChanged();

                Toast.makeText(AdminManageDeviceActivity.this, successMessage, Toast.LENGTH_SHORT).show();

                // Trì hoãn 1.5 giây trước khi đóng dialog và load lại danh sách để admin kịp nhìn thấy màu xanh lá/đỏ
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    dialog.dismiss();
                    loadPendingRequests();
                }, 1500);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AdminManageDeviceActivity.this, "Lỗi kết nối khi cập nhật yêu cầu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
