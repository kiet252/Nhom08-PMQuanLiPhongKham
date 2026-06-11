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
        tvPendingCount.setText(pendingRequests.size() + " yêu cầu chờ duyệt");
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

        TextView tvStaffName = dialog.findViewById(R.id.tvDialogDeviceStaffName);
        TextView tvStaffId = dialog.findViewById(R.id.tvDialogDeviceStaffId);
        TextView tvAndroidId = dialog.findViewById(R.id.tvDialogDeviceAndroidId);
        TextView tvCreatedAt = dialog.findViewById(R.id.tvDialogDeviceCreatedAt);
        MaterialButton btnApprove = dialog.findViewById(R.id.btnApproveDevice);
        MaterialButton btnReject = dialog.findViewById(R.id.btnRejectDevice);

        tvStaffName.setText(request.getStaffName());
        tvStaffId.setText(request.getStaffId());
        tvAndroidId.setText(request.getAndroidId());
        tvCreatedAt.setText(DeviceApprovalAdapter.formatDetailDate(request.getCreatedAt()));

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
        new AlertDialog.Builder(this)
                .setTitle("Duyệt thiết bị")
                .setMessage("Bạn có chắc muốn duyệt thiết bị này cho nhân viên?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Duyệt", (d, which) -> approveRequest(request, dialog))
                .show();
    }

    private void confirmReject(DeviceApprovalRequest request, Dialog dialog) {
        new AlertDialog.Builder(this)
                .setTitle("Từ chối yêu cầu")
                .setMessage("Bạn có chắc muốn từ chối yêu cầu duyệt thiết bị này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Từ chối", (d, which) -> rejectRequest(request, dialog))
                .show();
    }

    private void approveRequest(DeviceApprovalRequest request, Dialog dialog) {
        if (request == null) return;
        setLoading(true);
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

    private void rejectRequest(DeviceApprovalRequest request, Dialog dialog) {
        setLoading(true);
        updateRequestStatus(request, "Từ chối", "Đã từ chối yêu cầu", dialog);
    }

    private void updateRequestStatus(DeviceApprovalRequest request, String status, String successMessage, Dialog dialog) {
        repository.updateRequestStatus(request.getId(), status).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    Toast.makeText(AdminManageDeviceActivity.this, "Không thể cập nhật trạng thái yêu cầu", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                Toast.makeText(AdminManageDeviceActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                loadPendingRequests();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AdminManageDeviceActivity.this, "Lỗi kết nối khi cập nhật yêu cầu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
