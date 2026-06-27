package dashboard_fragment.admin_timekeeping_fix_request;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Locale;

import coil.Coil;
import coil.request.ImageRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTimekeepingFixRequestActivity extends BaseActivity {
    private TimekeepingFixRequestRepository repository;
    private TimekeepingFixRequestAdapter adapter;
    private final List<TimekeepingFixRequestItem> allRequests = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tabAll;
    private TextView tabPending;
    private TextView tabApproved;
    private TextView tabRejected;
    private EditText edtSearch;
    private String selectedStatus = "Tất cả";
    private String searchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_timekeeping_fix_request);

        repository = new TimekeepingFixRequestRepository(this);
        bindViews();
        setupList();
        setupActions();
        loadRequests();
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.rvTimekeepingFixRequests);
        progressBar = findViewById(R.id.progressFixRequests);
        layoutEmpty = findViewById(R.id.layoutFixRequestEmpty);
        tabAll = findViewById(R.id.tabFixAll);
        tabPending = findViewById(R.id.tabFixPending);
        tabApproved = findViewById(R.id.tabFixApproved);
        tabRejected = findViewById(R.id.tabFixRejected);
        edtSearch = findViewById(R.id.edtFixRequestSearch);
    }

    private void setupList() {
        adapter = new TimekeepingFixRequestAdapter();
        adapter.setOnRequestClickListener(this::showDetailDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBackFixRequest).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefreshFixRequest).setOnClickListener(v -> loadRequests());
        tabAll.setOnClickListener(v -> selectStatus("Tất cả"));
        tabPending.setOnClickListener(v -> selectStatus("Chờ duyệt"));
        tabApproved.setOnClickListener(v -> selectStatus("Đã duyệt"));
        tabRejected.setOnClickListener(v -> selectStatus("Từ chối"));
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchText = s != null ? s.toString() : "";
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadRequests() {
        setLoading(true);
        repository.getAllRequests().enqueue(new Callback<List<TimekeepingFixRequestItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<TimekeepingFixRequestItem>> call,
                                   @NonNull Response<List<TimekeepingFixRequestItem>> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(AdminTimekeepingFixRequestActivity.this,
                            "Không thể tải yêu cầu sửa chấm công", Toast.LENGTH_SHORT).show();
                    updateData(new ArrayList<>());
                    return;
                }
                updateData(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<TimekeepingFixRequestItem>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AdminTimekeepingFixRequestActivity.this,
                        "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                updateData(new ArrayList<>());
            }
        });
    }

    private void updateData(List<TimekeepingFixRequestItem> requests) {
        allRequests.clear();
        allRequests.addAll(requests);
        updateCounters();
        applyFilters();
    }

    private void updateCounters() {
        int pending = countByStatus("Chờ duyệt");
        tabAll.setText("Tất cả  " + allRequests.size());
        tabPending.setText("Chờ duyệt  " + pending);
        tabApproved.setText("Đã duyệt  " + countByStatus("Đã duyệt"));
        tabRejected.setText("Từ chối  " + countByStatus("Từ chối"));
    }

    private int countByStatus(String status) {
        int count = 0;
        for (TimekeepingFixRequestItem item : allRequests) {
            if (status.equals(item.getStatus())) count++;
        }
        return count;
    }

    private void selectStatus(String status) {
        selectedStatus = status;
        updateTabs();
        applyFilters();
    }

    private void updateTabs() {
        setTabSelected(tabAll, "Tất cả".equals(selectedStatus));
        setTabSelected(tabPending, "Chờ duyệt".equals(selectedStatus));
        setTabSelected(tabApproved, "Đã duyệt".equals(selectedStatus));
        setTabSelected(tabRejected, "Từ chối".equals(selectedStatus));
    }

    private void setTabSelected(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.bg_filter_tab_selected : R.drawable.bg_filter_tab_unselected);
        tab.setTextColor(android.graphics.Color.parseColor(selected ? "#FFFFFF" : "#64748B"));
    }

    private void applyFilters() {
        String query = searchText == null ? "" : searchText.trim().toLowerCase(Locale.getDefault());
        List<TimekeepingFixRequestItem> filtered = new ArrayList<>();
        for (TimekeepingFixRequestItem item : allRequests) {
            boolean statusMatch = "Tất cả".equals(selectedStatus) || selectedStatus.equals(item.getStatus());
            boolean searchMatch = query.isEmpty()
                    || item.getStaffName().toLowerCase(Locale.getDefault()).contains(query)
                    || item.getStaffRole().toLowerCase(Locale.getDefault()).contains(query)
                    || item.getReason().toLowerCase(Locale.getDefault()).contains(query);
            if (statusMatch && searchMatch) filtered.add(item);
        }
        adapter.setData(filtered);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showDetailDialog(TimekeepingFixRequestItem item) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_timekeeping_fix_request_detail);

        TextView tvTitle = dialog.findViewById(R.id.tvFixDetailTitle);
        TextView tvStatus = dialog.findViewById(R.id.tvFixDetailStatus);
        ImageView ivAvatar = dialog.findViewById(R.id.ivFixDetailAvatar);
        TextView tvName = dialog.findViewById(R.id.tvFixDetailName);
        TextView tvRole = dialog.findViewById(R.id.tvFixDetailRole);
        TextView tvOriginalDate = dialog.findViewById(R.id.tvFixOriginalDate);
        TextView tvOriginalStart = dialog.findViewById(R.id.tvFixOriginalStart);
        TextView tvOriginalEnd = dialog.findViewById(R.id.tvFixOriginalEnd);
        TextView tvOriginalStatus = dialog.findViewById(R.id.tvFixOriginalStatus);
        TextView tvRequestedStart = dialog.findViewById(R.id.tvFixRequestedStart);
        TextView tvRequestedEnd = dialog.findViewById(R.id.tvFixRequestedEnd);
        LinearLayout layoutRequestedStart = dialog.findViewById(R.id.layoutFixRequestedStart);
        LinearLayout layoutRequestedEnd = dialog.findViewById(R.id.layoutFixRequestedEnd);
        TextView tvReason = dialog.findViewById(R.id.tvFixReason);
        TextView tvEvidenceTitle = dialog.findViewById(R.id.tvFixEvidenceTitle);
        LinearLayout layoutEvidence = dialog.findViewById(R.id.layoutFixEvidenceImages);
        MaterialButton btnApprove = dialog.findViewById(R.id.btnApproveFixRequest);
        MaterialButton btnReject = dialog.findViewById(R.id.btnRejectFixRequest);

        tvTitle.setText("Mã yêu cầu #" + item.getId());
        tvName.setText(item.getStaffName());
        tvRole.setText(item.getStaffRole());
        tvOriginalDate.setText(TimekeepingFixRequestAdapter.formatDate(item.getOriginalStartTime(), item.getRequestedCheckOut()));
        tvOriginalStart.setText(TimekeepingFixRequestAdapter.formatTime(item.getOriginalStartTime()));
        tvOriginalEnd.setText(TimekeepingFixRequestAdapter.formatTime(item.getOriginalEndTime()));
        tvOriginalStatus.setText(item.getOriginalStatus());
        String reqStart = item.getRequestedCheckIn();
        String reqEnd = item.getRequestedCheckOut();

        if (reqStart != null && !reqStart.trim().isEmpty()) {
            layoutRequestedStart.setVisibility(View.VISIBLE);
            tvRequestedStart.setText(TimekeepingFixRequestAdapter.formatTime(reqStart));
        } else {
            layoutRequestedStart.setVisibility(View.GONE);
        }

        if (reqEnd != null && !reqEnd.trim().isEmpty()) {
            layoutRequestedEnd.setVisibility(View.VISIBLE);
            tvRequestedEnd.setText(TimekeepingFixRequestAdapter.formatTime(reqEnd));
        } else {
            layoutRequestedEnd.setVisibility(View.GONE);
        }
        tvReason.setText(item.getReason());
        applyDialogStatus(tvStatus, item.getStatus(), btnApprove, btnReject);
        loadAvatar(ivAvatar, item.getStaffAvatar());
        renderEvidence(layoutEvidence, tvEvidenceTitle, item.getEvidenceUrls());

        dialog.findViewById(R.id.btnCloseFixDetail).setOnClickListener(v -> dialog.dismiss());
        btnApprove.setOnClickListener(v -> confirmApprove(item, dialog));
        btnReject.setOnClickListener(v -> confirmReject(item, dialog));

        Window window = dialog.getWindow();
        if (window != null) window.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        if (window != null) {
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void applyDialogStatus(TextView tvStatus, String status,
                                   MaterialButton btnApprove, MaterialButton btnReject) {
        boolean approved = "Đã duyệt".equalsIgnoreCase(status);
        boolean rejected = "Từ chối".equalsIgnoreCase(status);
        boolean pending = !approved && !rejected;

        if (approved) {
            tvStatus.setText("Đã duyệt");
            tvStatus.setTextColor(android.graphics.Color.parseColor("#16884A"));
            tvStatus.setBackgroundResource(R.drawable.bg_device_status_approved);
        } else if (rejected) {
            tvStatus.setText("Từ chối");
            tvStatus.setTextColor(android.graphics.Color.parseColor("#C62828"));
            tvStatus.setBackgroundResource(R.drawable.bg_device_status_rejected);
        } else {
            tvStatus.setText("Chờ duyệt");
            tvStatus.setTextColor(android.graphics.Color.parseColor("#B56B00"));
            tvStatus.setBackgroundResource(R.drawable.bg_device_status_pending);
        }

        btnApprove.setVisibility(pending ? View.VISIBLE : View.GONE);
        btnReject.setVisibility(pending ? View.VISIBLE : View.GONE);
    }

    private void renderEvidence(LinearLayout container, TextView title, List<String> urls) {
        container.removeAllViews();
        title.setText("MINH CHỨNG (" + urls.size() + " ẢNH)");

        if (urls.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Không có minh chứng");
            empty.setTextColor(android.graphics.Color.parseColor("#64748B"));
            empty.setTextSize(15);
            empty.setPadding(18, 18, 18, 18);
            container.addView(empty);
            return;
        }

        // Wrap sang HorizontalScrollView để chứa hàng ảnh
        android.widget.HorizontalScrollView hsv = new android.widget.HorizontalScrollView(this);
        hsv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        hsv.setHorizontalScrollBarEnabled(false);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        int size = dpToPx(80); // hình vuông 80dp
        int margin = dpToPx(4);

        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            int index = i;

            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, margin, margin, margin);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(R.drawable.bg_prescription_field);

            // Bo góc nếu muốn
            imageView.setClipToOutline(true);
            imageView.setOutlineProvider(android.view.ViewOutlineProvider.BACKGROUND);

            ImageRequest request = new ImageRequest.Builder(this)
                    .data(url)
                    .target(imageView)
                    .crossfade(true)
                    .placeholder(R.drawable.bg_prescription_field)
                    .error(R.drawable.bg_prescription_field)
                    .build();
            Coil.imageLoader(this).enqueue(request);

            imageView.setOnClickListener(v -> showFullImage(url, urls, index));
            row.addView(imageView);
        }

        hsv.addView(row);
        container.addView(hsv);
    }

    private void showFullImage(String currentUrl, List<String> urls, int startIndex) {
        Dialog fullDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fullDialog.setContentView(new android.widget.FrameLayout(this) {{
            setBackgroundColor(android.graphics.Color.BLACK);

            // ViewPager2 để swipe qua lại
            androidx.viewpager2.widget.ViewPager2 pager = new androidx.viewpager2.widget.ViewPager2(getContext());
            pager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            // Ngày giờ ở giữa trên cùng
            TextView tvDateTime = new TextView(getContext());
            tvDateTime.setText((startIndex + 1) + " / " + urls.size()); // tuỳ, hoặc để ngày giờ nếu có
            tvDateTime.setTextColor(android.graphics.Color.WHITE);
            tvDateTime.setTextSize(15);
            tvDateTime.setPadding(0, dpToPx(44), 0, 0);
            LayoutParams lpDate = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lpDate.gravity = android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL;
            tvDateTime.setGravity(android.view.Gravity.CENTER);
            tvDateTime.setLayoutParams(lpDate);
            addView(tvDateTime);

            pager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    tvDateTime.setText((position + 1) + " / " + urls.size());
                }
            });
            pager.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
                @NonNull @Override
                public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
                    ImageView iv = new ImageView(getContext());
                    iv.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT));
                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    return new androidx.recyclerview.widget.RecyclerView.ViewHolder(iv) {};
                }
                @Override
                public void onBindViewHolder(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int position) {
                    ImageView iv = (ImageView) holder.itemView;
                    ImageRequest req = new ImageRequest.Builder(getContext())
                            .data(urls.get(position))
                            .target(iv)
                            .crossfade(true)
                            .build();
                    Coil.imageLoader(getContext()).enqueue(req);
                }
                @Override public int getItemCount() { return urls.size(); }
            });
            pager.setCurrentItem(startIndex, false);
            addView(pager);

            // Nút đóng
            TextView btnClose = new TextView(getContext());
            btnClose.setText("✕");
            btnClose.setTextColor(android.graphics.Color.WHITE);
            btnClose.setTextSize(22);
            btnClose.setPadding(dpToPx(16), dpToPx(40), dpToPx(16), dpToPx(16));
            btnClose.setOnClickListener(v -> fullDialog.dismiss());
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
            btnClose.setLayoutParams(lp);
            addView(btnClose);
        }});
        fullDialog.show();
    }
    private void confirmApprove(TimekeepingFixRequestItem item, Dialog dialog) {
        new AlertDialog.Builder(this)
                .setTitle("Phê duyệt yêu cầu")
                .setMessage("Cập nhật giờ chấm công theo yêu cầu này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Phê duyệt", (d, which) -> approveRequest(item, dialog))
                .show();
    }

    private void confirmReject(TimekeepingFixRequestItem item, Dialog dialog) {
        new AlertDialog.Builder(this)
                .setTitle("Từ chối yêu cầu")
                .setMessage("Bạn có chắc muốn từ chối yêu cầu sửa chấm công này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Từ chối", (d, which) -> rejectRequest(item, dialog))
                .show();
    }

    private void approveRequest(TimekeepingFixRequestItem item, Dialog dialog) {
        setLoading(true);
        repository.updateShift(item).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    setLoading(false);
                    Toast.makeText(AdminTimekeepingFixRequestActivity.this,
                            "Không thể cập nhật ca làm", Toast.LENGTH_SHORT).show();
                    return;
                }
                repository.approveRequest(item).enqueue(new StatusCallback(dialog, "Đã phê duyệt yêu cầu"));
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AdminTimekeepingFixRequestActivity.this,
                        "Lỗi kết nối khi cập nhật ca", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectRequest(TimekeepingFixRequestItem item, Dialog dialog) {
        setLoading(true);
        repository.rejectRequest(item.getId()).enqueue(new StatusCallback(dialog, "Đã từ chối yêu cầu"));
    }

    private class StatusCallback implements Callback<ResponseBody> {
        private final Dialog dialog;
        private final String successMessage;

        StatusCallback(Dialog dialog, String successMessage) {
            this.dialog = dialog;
            this.successMessage = successMessage;
        }

        @Override
        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
            setLoading(false);
            if (!response.isSuccessful()) {
                Toast.makeText(AdminTimekeepingFixRequestActivity.this,
                        "Không thể cập nhật trạng thái yêu cầu", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(AdminTimekeepingFixRequestActivity.this, successMessage, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadRequests();
        }

        @Override
        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
            setLoading(false);
            Toast.makeText(AdminTimekeepingFixRequestActivity.this,
                    "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAvatar(ImageView imageView, String avatarPath) {
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.dashboard_icon_avatar);
            return;
        }
        String url = avatarPath.startsWith("http")
                ? avatarPath
                : "https://waiuciilyysobnvcwshd.supabase.co/storage/v1/object/public/avatars/" + avatarPath;
        ImageRequest request = new ImageRequest.Builder(this)
                .data(url)
                .target(imageView)
                .crossfade(true)
                .placeholder(R.drawable.dashboard_icon_avatar)
                .error(R.drawable.dashboard_icon_avatar)
                .build();
        Coil.imageLoader(this).enqueue(request);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
