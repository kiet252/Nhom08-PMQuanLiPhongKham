package dashboard_fragment.account_chatbot;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.example.nhom08_quanlyphongkham.BuildConfig;
import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.genai.Client;
import com.google.gson.JsonObject;

import dashboard_fragment.UserRole;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ChatbotBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_ROLE = "arg_role";

    public static ChatbotBottomSheetFragment newInstance(String roleName) {
        ChatbotBottomSheetFragment fragment = new ChatbotBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, roleName);
        fragment.setArguments(args);
        return fragment;
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String roleName = UserRole.BAC_SI.name();

    private NestedScrollView scrollMessages;
    private LinearLayout chatMessageContainer;
    private EditText edtChatInput;

    private String welcomeMessage = "";
    private Client geminiClient;
    private ChatbotDatabaseAssistant databaseAssistant;
    private final StringBuilder conversationHistory = new StringBuilder();
    private View loadingView;
    private TextView loadingTextView;

    private final Handler loadingHandler = new Handler(Looper.getMainLooper());

    private final Runnable loadingRunnable = new Runnable() {

        int dotCount = 0;

        @Override
        public void run() {

            if (loadingTextView == null) return;

            dotCount = (dotCount + 1) % 4;

            String dots = "";
            for (int i = 0; i < dotCount; i++) {
                dots += ".";
            }

            loadingTextView.setText(
                    "🔍 Đang tra cứu thông tin" + dots
            );

            loadingHandler.postDelayed(this, 500);
        }
    };

    private final String[] suggestionTexts = new String[5];

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String value = getArguments().getString(ARG_ROLE);
            if (value != null && !value.trim().isEmpty()) {
                roleName = value.trim();
            }
        }
    }

    @Override
    public @NonNull Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                int twoThirdScreenHeight = (int) (requireContext().getResources().getDisplayMetrics().heightPixels * (2.0f / 3.0f));
                ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
                params.height = twoThirdScreenHeight;
                bottomSheet.setLayoutParams(params);

                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

                behavior.setFitToContents(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(true);
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setDimAmount(0.7f);
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            );
        }

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvBotRoleHint = view.findViewById(R.id.tvBotRoleHint);
        MaterialButton btnSuggestion1 = view.findViewById(R.id.btnSuggestion1);
        MaterialButton btnSuggestion2 = view.findViewById(R.id.btnSuggestion2);
        MaterialButton btnSuggestion3 = view.findViewById(R.id.btnSuggestion3);
        MaterialButton btnSuggestion4 = view.findViewById(R.id.btnSuggestion4);
        MaterialButton btnSuggestion5 = view.findViewById(R.id.btnSuggestion5);
        scrollMessages = view.findViewById(R.id.scrollChatMessages);
        chatMessageContainer = view.findViewById(R.id.chatMessageContainer);
        edtChatInput = view.findViewById(R.id.edtChatInput);
        ImageButton btnCloseChatbot = view.findViewById(R.id.btnCloseChatbot);
        ImageButton btnSendChat = view.findViewById(R.id.btnSendChat);
        ImageButton btnVoiceInput = view.findViewById(R.id.btnVoiceInput);

        applyRoleContent(
                tvBotRoleHint,
                btnSuggestion1,
                btnSuggestion2,
                btnSuggestion3,
                btnSuggestion4,
                btnSuggestion5
        );

        renderInitialGreeting();

        btnCloseChatbot.setOnClickListener(v ->{
            conversationHistory.setLength(0);

            dismiss();
        });

        btnSuggestion1.setOnClickListener(v -> sendMessage(btnSuggestion1.getText().toString()));
        btnSuggestion2.setOnClickListener(v -> sendMessage(btnSuggestion2.getText().toString()));
        btnSuggestion3.setOnClickListener(v -> sendMessage(btnSuggestion3.getText().toString()));
        btnSuggestion4.setOnClickListener(v -> sendMessage(btnSuggestion4.getText().toString()));
        btnSuggestion5.setOnClickListener(v -> sendMessage(btnSuggestion5.getText().toString()));

        btnSendChat.setOnClickListener(v -> sendMessage(edtChatInput.getText().toString()));

        btnVoiceInput.setOnClickListener(v -> startVoiceInput());

        edtChatInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage(edtChatInput.getText().toString());
            return true;
        });
        geminiClient = new Client.Builder()
                .apiKey(BuildConfig.GEMINI_API_KEY)
                .build();
        databaseAssistant = new ChatbotDatabaseAssistant(requireContext(), roleName);
    }

    private void applyRoleContent(
            TextView tvBotRoleHint,
            MaterialButton btnSuggestion1,
            MaterialButton btnSuggestion2,
            MaterialButton btnSuggestion3,
            MaterialButton btnSuggestion4,
            MaterialButton btnSuggestion5
    ) {
        if (UserRole.BAC_SI.name().equals(roleName)) {
            tvBotRoleHint.setText("Hỗ trợ tác vụ dành cho Bác sĩ");
            welcomeMessage =
                    "👋 Xin chào Bác sĩ! Tôi là MedBot - trợ lý AI của phòng khám TMH.\n" +
                            "Tôi có thể giúp bạn hướng dẫn sử dụng hệ thống, trả lời thắc mắc về các tính năng.\n" +
                            "Hãy hỏi tôi bất cứ điều gì!";

            setSuggestionTexts(
                    btnSuggestion1, btnSuggestion2, btnSuggestion3, btnSuggestion4, btnSuggestion5,
                    "Cách đổi trạng thái phiếu khám",
                    "Cách chọn dịch vụ cận lâm sàng",
                    "Cách chọn/nhập chẩn đoán",
                    "Cách kê đơn thuốc",
                    "Cách xem bệnh án"
            );
            return;
        }

        if (UserRole.ADMIN.name().equals(roleName)) {
            tvBotRoleHint.setText("Hỗ trợ tác vụ dành cho Quản trị viên");
            welcomeMessage =
                    "👋 Xin chào Quản trị viên! Tôi là MedBot - trợ lý AI của phòng khám TMH.\n" +
                            "Tôi có thể giúp bạn hướng dẫn sử dụng hệ thống, trả lời thắc mắc về các tính năng.\n" +
                            "Hãy hỏi tôi bất cứ điều gì!";

            setSuggestionTexts(
                    btnSuggestion1, btnSuggestion2, btnSuggestion3, btnSuggestion4, btnSuggestion5,
                    "Làm sao chỉnh sửa thông tin nhân viên",
                    "Làm sao để thêm thuốc và dịch vụ CLS",
                    "Làm sao để phân ca cho nhân viên",
                    "Làm sao để tạo thông báo chung",
                    "Làm sao để xóa thông báo chung"
            );
            return;
        }

        tvBotRoleHint.setText("Hỗ trợ tác vụ dành cho Nhân viên");
        welcomeMessage =
                "👋 Xin chào Nhân viên! Tôi là MedBot - trợ lý AI của phòng khám TMH.\n" +
                        "Tôi có thể giúp bạn hướng dẫn sử dụng hệ thống, trả lời thắc mắc về các tính năng.\n" +
                        "Hãy hỏi tôi bất cứ điều gì!";

        setSuggestionTexts(
                btnSuggestion1, btnSuggestion2, btnSuggestion3, btnSuggestion4, btnSuggestion5,
                "Cách tạo hồ sơ cho bệnh nhân mới",
                "Cách cập nhật thông tin cho bệnh nhân",
                "Cách tạo phiếu khám",
                "Cách xem chi tiết phiếu khám",
                "Cách hủy phiếu khám"
        );
    }

    private void renderInitialGreeting() {
        chatMessageContainer.removeAllViews();
        appendBotMessage(welcomeMessage, nowTime());
        scrollToBottom();
    }

    private void sendMessage(String rawMessage) {

        String message = rawMessage == null ? "" : rawMessage.trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(requireContext(),
                    "Nhập câu hỏi trước khi gửi",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        appendUserMessage(message, nowTime());
        edtChatInput.setText("");

        mainHandler.postDelayed(() -> {

            if (!isAdded()) return;

            String localAnswer = answerQueryLocal(message);

            if (localAnswer != null) {
                appendBotMessage(localAnswer, nowTime());
                return;
            }
            if (isDatabaseQuery(message)) {

                if (!canAccessDatabaseIntent(message)) {

                    appendBotMessage(
                            "❌ Bạn không có quyền truy cập thông tin này.",
                            nowTime()
                    );
                    return;
                }

                showLoadingMessage();

                databaseAssistant.processNaturalLanguage(
                        message,
                        json -> mainHandler.post(() -> {

                            if (!isAdded()) return;

                            hideLoadingMessage();

                            String display =
                                    formatDatabaseResponse(json);

                            appendBotMessage(
                                    display,
                                    nowTime()
                            );
                        })
                );

                return;
            }

            showLoadingMessage();
            askGemini(message);

        }, 300);
    }

    private boolean canAccessDatabaseIntent(String message) {

        String q = normalize(message);

        if (UserRole.ADMIN.name().equals(roleName)) {
            return true;
        }

        if (UserRole.BAC_SI.name().equals(roleName)) {

            if (containsAny(q,
                    "hoa don",
                    "doanh thu",
                    "tong tien")) {
                return false;
            }

            return true;
        }

        if (UserRole.NHAN_VIEN.name().equals(roleName)) {

            if (containsAny(q,
                    "benh an",
                    "lich su benh an",
                    "chan doan")) {
                return false;
            }

            if (containsAny(q,
                    "kho thuoc",
                    "ton kho",
                    "thuoc sap het")) {
                return false;
            }

            return true;
        }

        return false;
    }

    private boolean isDatabaseQuery(String message) {
        String q = normalize(message);

        if (containsAny(q,
                "thong tin benh nhan",
                "ho so benh nhan",
                "tra cuu benh nhan",
                "tim benh nhan",
                "benh nhan ten",
                "cccd",
                "can cuoc cong dan",
                "can cuoc",
                "ma benh nhan",
                "id benh nhan")) return true;

        if (containsAny(q,
                "lich hen cua benh nhan",
                "lich hen benh nhan",
                "lich hen cua",
                "dat lich hen",
                "xem lich hen")) return true;

        if (containsAny(q,
                "benh an cua benh nhan",
                "benh an cua",
                "ho so benh an",
                "lich su kham",
                "lich su benh an")) return true;

        if (containsAny(q,
                "lich kham hom nay",
                "lich kham ngay",
                "lich kham cua bac si",
                "lich kham",
                "phieu kham hom nay",
                "phieu kham ngay")) return true;

        if (containsAny(q,
                "hoa don cua benh nhan",
                "hoa don benh nhan",
                "tra cuu hoa don benh nhan")) return true;

        if (containsAny(q,
                "hoa don hom nay",
                "hoa don ngay",
                "hoa don thang")) return true;

        if (containsAny(q,
                "tra cuu thuoc",
                "tim thuoc",
                "thong tin thuoc",
                "thuoc ten",
                "hoat chat")) return true;

        if (containsAny(q,
                "tra cuu dich vu",
                "tim dich vu",
                "dich vu lam sang",
                "can lam sang ten",
                "xet nghiem ten")) return true;

        if (containsAny(q,
                "bao nhieu benh nhan",
                "so luong benh nhan",
                "dem benh nhan",
                "tong so benh nhan")) return true;

        if (containsAny(q,
                "bao nhieu lich hen",
                "so luong lich hen",
                "dem lich hen",
                "tong so lich hen")) return true;

        if (containsAny(q,
                "bao nhieu phieu kham",
                "so luong phieu kham",
                "dem phieu kham",
                "tong so phieu kham")) return true;

        if (containsAny(q,
                "bao nhieu hoa don",
                "so luong hoa don",
                "dem hoa don",
                "tong so hoa don")) return true;

        if (containsAny(q,
                "danh sach benh nhan",
                "liet ke benh nhan",
                "tat ca benh nhan")) return true;

        if (containsAny(q,
                "danh sach lich hen",
                "liet ke lich hen",
                "tat ca lich hen")) return true;

        if (containsAny(q,
                "danh sach phieu kham",
                "liet ke phieu kham",
                "tat ca phieu kham")) return true;

        if (containsAny(q,
                "danh sach hoa don",
                "liet ke hoa don",
                "tat ca hoa don",
                "xem hoa don")) return true;

        if (containsAny(q,
                "kho thuoc",
                "ton kho",
                "thong ke kho",
                "thuoc con lai",
                "thuoc sap het",
                "thuoc het hang",
                "so luong thuoc")) return true;

        return false;
    }

    private String formatDatabaseResponse(String json) {
        if (json == null) return "⚠️ Không nhận được phản hồi từ hệ thống.";

        try {
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            boolean success = obj.has("success") && obj.get("success").getAsBoolean();

            if (!success) {
                String msg = obj.has("message") ? obj.get("message").getAsString() : "Đã có lỗi xảy ra.";
                return "⚠️ " + msg;
            }

            String intent = obj.has("intent") ? obj.get("intent").getAsString() : "";
            com.google.gson.JsonElement dataEl = obj.has("data") ? obj.get("data") : null;

            if (dataEl == null || dataEl.isJsonNull()) {
                return "ℹ️ Không có dữ liệu phù hợp.";
            }

            if (dataEl.isJsonObject()) {
                com.google.gson.JsonObject dataObj = dataEl.getAsJsonObject();
                if (dataObj.has("count")) {
                    int count = dataObj.get("count").getAsInt();
                    return buildCountMessage(intent, count);
                }
            }

            if ("INVENTORY_STATS".equals(intent) && dataEl.isJsonObject()) {
                com.google.gson.JsonObject stats = dataEl.getAsJsonObject();
                int total = stats.has("total_distinct_medicines") ? stats.get("total_distinct_medicines").getAsInt() : 0;
                long stock = stats.has("total_stock_count") ? stats.get("total_stock_count").getAsLong() : 0;
                int low = stats.has("low_stock_types_count") ? stats.get("low_stock_types_count").getAsInt() : 0;
                return "📦 Thống kê kho thuốc\n\n" +
                        "• Tổng số loại thuốc: " + total + "\n" +
                        "• Tổng tồn kho: " + stock + " đơn vị\n" +
                        "• Số loại sắp hết (< 10): " + low;
            }

            if (dataEl.isJsonArray()) {
                com.google.gson.JsonArray arr = dataEl.getAsJsonArray();
                if (arr.size() == 0) return "ℹ️ Không tìm thấy dữ liệu phù hợp.";
                return buildArraySummary(intent, arr);
            }

            return "✅ Dữ liệu:\n" + json;

        } catch (Exception e) {
            return "⚠️ Lỗi xử lý dữ liệu: " + e.getMessage();
        }
    }

    private String buildCountMessage(String intent, int count) {
        switch (intent) {
            case "COUNT_PATIENTS":     return "👥 Tổng số bệnh nhân: **" + count + "** người.";
            case "COUNT_APPOINTMENTS": return "📅 Tổng số lịch hẹn: **" + count + "** lịch.";
            case "COUNT_EXAMINATIONS": return "📋 Tổng số phiếu khám: **" + count + "** phiếu.";
            case "COUNT_BILLS":        return "🧾 Tổng số hóa đơn: **" + count + "** hóa đơn.";
            default:                   return "📊 Kết quả: " + count;
        }
    }

    private String buildArraySummary(String intent, com.google.gson.JsonArray arr) {
        int size = arr.size();
        StringBuilder sb = new StringBuilder();

        switch (intent) {
            case "PATIENT_PROFILE":
            case "LIST_PATIENTS": {
                sb.append("👤 ").append(size == 1 ? "Thông tin bệnh nhân" : "Danh sách bệnh nhân (" + size + " người)").append("\n\n");
                int limit = Math.min(size, 10);
                for (int i = 0; i < limit; i++) {
                    com.google.gson.JsonObject p = arr.get(i).getAsJsonObject();
                    sb.append("• ").append(getString(p, "ho_ten", "(Chưa rõ tên)"));
                    String idBN = getString(p, "id", "");
                    if (!idBN.isEmpty()) sb.append(" | Mã bệnh nhân: ").append(idBN);
                    String phone = getString(p, "so_dien_thoai", "");
                    if (!phone.isEmpty()) sb.append(" | SĐT: ").append(phone);
                    sb.append("\n");
                }
                if (size > 10) sb.append("... và ").append(size - 10).append(" người khác.");
                break;
            }
            case "APPOINTMENT_BY_PATIENT":
            case "LIST_APPOINTMENTS": {
                sb.append("📅 ").append(size == 1 ? "Lịch hẹn" : "Danh sách lịch hẹn (" + size + " lịch)").append("\n\n");
                int limit = Math.min(size, 10);
                for (int i = 0; i < limit; i++) {
                    com.google.gson.JsonObject a = arr.get(i).getAsJsonObject();
                    sb.append("• Ngày: ").append(getString(a, "ngay_hen", "?"));

                    String note = getString(a, "ghi_chu", "");
                    if (!note.isEmpty()) {
                        sb.append(" | Ghi chú: ").append(note);
                    }

                    sb.append(" | Mã BN: ")
                            .append(getString(a, "patient_id", "?"));

                    sb.append("\n");
                }
                if (size > 10) sb.append("... và ").append(size - 10).append(" lịch khác.");
                break;
            }
            case "MEDICAL_RECORD_BY_PATIENT": {
                sb.append("🗂️ Bệnh án (").append(size).append(" lần khám)\n\n");
                int limit = Math.min(size, 5);
                for (int i = 0; i < limit; i++) {
                    com.google.gson.JsonObject r = arr.get(i).getAsJsonObject();
                    sb.append("• Ngày: ").append(getString(r, "ngay_kham", "?"));
                    String diag = getString(r, "chan_doan", "");
                    if (!diag.isEmpty()) sb.append(" | Chẩn đoán: ").append(diag);
                    sb.append("\n");
                }
                if (size > 5) sb.append("... và ").append(size - 5).append(" lần khám khác.");
                break;
            }
            case "DOCTOR_SCHEDULE":
            case "LIST_EXAMINATIONS": {
                sb.append("📋 ").append(size == 1 ? "Phiếu khám" : "Danh sách phiếu khám (" + size + " phiếu)").append("\n\n");
                int limit = Math.min(size, 10);
                for (int i = 0; i < limit; i++) {
                    com.google.gson.JsonObject e = arr.get(i).getAsJsonObject();
                    sb.append("• Ngày: ").append(getString(e, "ngay_kham", "?"));
                    String gio = getString(e, "gio_du_kien", "");
                    if (!gio.isEmpty()) sb.append(" ").append(gio);

                    if (e.has("patient") && !e.get("patient").isJsonNull()) {
                        try {
                            String ten = e.get("patient").getAsJsonObject().get("ho_ten").getAsString();
                            sb.append(" | BN: ").append(ten);
                        } catch (Exception ignored) {}
                    }
                    String status = getString(e, "trang_thai", "");
                    if (!status.isEmpty()) sb.append(" | TT: ").append(status);
                    sb.append("\n");
                }
                if (size > 10) sb.append("... và ").append(size - 10).append(" phiếu khác.");
                break;
            }
            case "BILL_BY_PATIENT":
            case "BILL_BY_DATE":
            case "LIST_BILLS": {

                sb.append("🧾 ")
                        .append(size == 1
                                ? "Hóa đơn"
                                : "Danh sách hóa đơn (" + size + ")")
                        .append("\n\n");

                int limit = Math.min(size, 10);

                for (int i = 0; i < limit; i++) {

                    JsonObject b = arr.get(i).getAsJsonObject();

                    sb.append("• BN: ")
                            .append(getString(b, "ten_benh_nhan", "Không rõ"));

                    sb.append("\n  Ngày khám: ")
                            .append(getString(b, "ngay_kham", "?"));

                    sb.append("\n  Tổng tiền: ")
                            .append(getString(b, "tong_thanh_toan", "0"))
                            .append("đ");

                    String status =
                            getString(b, "trang_thai_thanh_toan", "");

                    if (!status.isEmpty()) {
                        sb.append("\n  Trạng thái: ")
                                .append(status);
                    }

                    sb.append("\n\n");
                }

                if (size > 10) {
                    sb.append("... và ")
                            .append(size - 10)
                            .append(" hóa đơn khác.");
                }

                break;
            }
            case "MEDICINE_LOOKUP": {
                sb.append("💊 Thuốc tìm được (").append(size).append(")\n\n");
                int limit = Math.min(size, 10);
                for (int i = 0; i < limit; i++) {
                    com.google.gson.JsonObject m = arr.get(i).getAsJsonObject();
                    sb.append("• ").append(getString(m, "ten_thuoc", "?"));
                    String hc = getString(m, "hoat_chat", "");
                    if (!hc.isEmpty()) sb.append(" (").append(hc).append(")");
                    String dvt = getString(m, "don_vi_tinh", "");
                    if (!dvt.isEmpty()) sb.append(" | ĐVT: ").append(dvt);
                    sb.append(" | Tồn: ").append(getString(m, "ton_kho", "?"));
                    sb.append("\n");
                }
                if (size > 10) sb.append("... và ").append(size - 10).append(" loại khác.");
                break;
            }
            case "CLINICAL_LOOKUP": {
                sb.append("🔬 Dịch vụ lâm sàng (").append(size).append(")\n\n");
                int limit = Math.min(size, 10);
                for (int i = 0; i < limit; i++) {
                    com.google.gson.JsonObject c = arr.get(i).getAsJsonObject();
                    sb.append("• ").append(getString(c, "ten_dich_vu", "?"));
                    String price = getString(c, "don_gia", "");
                    if (!price.isEmpty()) sb.append(" | Giá: ").append(price).append("đ");
                    sb.append("\n");
                }
                if (size > 10) sb.append("... và ").append(size - 10).append(" dịch vụ khác.");
                break;
            }
            default:
                sb.append("✅ Tìm được ").append(size).append(" kết quả.");
        }
        return sb.toString().trim();
    }

    private String getString(com.google.gson.JsonObject obj, String key, String defaultVal) {
        try {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsString();
            }
        } catch (Exception ignored) {}
        return defaultVal;
    }

    private String answerQueryLocal(String rawMessage) {

        String query = normalize(rawMessage);

        if (UserRole.BAC_SI.name().equals(roleName)) {
            return answerDoctor(query);
        }

        if (UserRole.NHAN_VIEN.name().equals(roleName)) {
            return answerStaff(query);
        }

        if (UserRole.ADMIN.name().equals(roleName)) {
            return answerAdmin(query);
        }

        return null;

    }
    private String answerDoctor(String rawMessage) {
        String query = normalize(rawMessage);

        if (containsAny(query,
                "doi trang thai phieu kham",
                "trang thai phieu kham",
                "doi trang thai",
                "chuyen trang thai")) {
            return "📘 Cách đổi trạng thái phiếu khám\n\n" +
                    "Nhấn vào \"Danh sách phiếu khám\" → Mở phiếu cần xử lý → Đổi trạng thái sang Chờ khám, Đang khám hoặc Đã khám → Bấm Lưu.";
        }

        if (containsAny(query,
                "chon dich vu can lam sang",
                "chon can lam sang",
                "xet nghiem can lam sang")) {
            return "📘 Cách chọn dịch vụ cận lâm sàng\n\n" +
                    "Nhấn vào \"Danh sách phiếu khám\" → Mở phiếu cần xử lý → Mở tab \"Cận lâm sàng\" → Chọn loại xét nghiệm/cận lâm sàng cần chỉ định → Chuyển sang tab \"Đơn thuốc\" → Nhấn \"Lưu bệnh án\" để xác nhận.";
        }

        if (containsAny(query,
                "cach chon/nhap chan doan",
                "cach chon nhap chan doan",
                "chon nhap chan doan",
                "chon/nhap chan doan",
                "chon chan doan",
                "nhap chan doan")) {
            return "📘 Cách chọn/nhập chẩn đoán\n\n" +
                    "Nhấn vào \"Danh sách phiếu khám\" → Mở phiếu cần xử lý → Mở tab \"Chẩn đoán\" → Chọn chẩn đoán có sẵn hoặc nhập nội dung mới → Kiểm tra lại thông tin → Chuyển sang tab \"Đơn thuốc\" → Nhấn \"Lưu bệnh án\" để xác nhận.";
        }

        if (containsAny(query,
                "ke don thuoc",
                "ke don")) {
            return "📘 Kê đơn thuốc\n\n" +
                    "Nhấn vào \"Danh sách phiếu khám\" → Mở phiếu cần xử lý → Mở tab \"Đơn thuốc\" → bấm \"Thêm thuốc\" → Tìm thuốc → Nhập liều lượng, số ngày uống và hướng dẫn dùng → Nhấn \"Lưu bệnh án\" để xác nhận.";
        }

        if (containsAny(query,
                "xem benh an",
                "tra cuu benh an",
                "benh an")) {
            return "📘 Tra cứu bệnh án\n\n" +
                    "Nhấn vào \"Xem bệnh án\" → Nhập mã BN hoặc CCCD để tìm. Hệ thống sẽ hiển thị toàn bộ lịch sử khám, dịch vụ cân lâm sàng đã làm, kết quả chẩn đoán và đơn thuốc.";
        }

        if (containsAny(query,
                "tao lich hen",
                "hen kham")) {
            return "📘 Cách tạo lịch hẹn\n\n" +
                    "Vào màn hình \"Tạo lịch hẹn\" → Nhập mã BN hoặc CCCD → Chọn ngày tái khám → Ghi chú nếu cần → Bấm \"Xác nhận đặt lịch hẹn\" để hẹn tái khám.";
        }

        if (containsAny(query,
                "thay doi thong tin ca nhan",
                "cap nhat thong tin ca nhan",
                "thong tin ca nhan",
                "doi thong tin ca nhan")) {
            return "📘 Thay đổi thông tin cá nhân\n\n" +
                    "Vào mục Tài khoản → Chọn \"Chỉnh sửa hồ sơ\" → Cập nhật dữ liệu cần thiết → Bấm \"Lưu thay đổi\".";
        }

        if (containsAny(query,
                "doi mat khau",
                "doi password",
                "mat khau")) {
            return "📘 Đổi mật khẩu\n\n" +
                    "Vào mục Tài khoản → Chọn \"Đổi mật khẩu\" → Nhập mật khẩu cũ, mật khẩu mới và xác nhận → Bấm \"Cập nhật\".";
        }

        return null;
    }

    private String answerStaff(String rawMessage) {
        String query = normalize(rawMessage);

        if (containsAny(query,
                "cach tao ho so cho benh nhan moi",
                "tao ho so benh nhan moi",
                "tao ho so benh nhan",
                "them benh nhan",
                "tao benh nhan")) {
            return "📘 Cách tạo hồ sơ cho bệnh nhân mới\n\n" +
                    "Vào mục \"Quản lý thông tin bệnh nhân\" → Nhấn nút \"Tạo mới\" → Nhập đầy đủ Họ tên, Địa chỉ, Số điện thoại, Giới tính, Ngày sinh, CCCD → Nhấn \"Lưu\" để tạo mới.";
        }

        if (containsAny(query,
                "cach cap nhat thong tin cho benh nhan",
                "cap nhat thong tin cho benh nhan",
                "sua thong tin benh nhan",
                "cap nhat thong tin benh nhan",
                "sua benh nhan")) {
            return "📘 Cách cập nhật thông tin cho bệnh nhân\n\n" +
                    "Vào mục \"Quản lý thông tin bệnh nhân\" → Nhấn nút \"Cập nhật\" → Nhập đầy đủ Họ tên, Địa chỉ, Số điện thoại, Giới tính, Ngày sinh, CCCD → Nhấn \"Lưu\" để cập nhật.";
        }

        if (containsAny(query,
                "cach tao phieu kham",
                "tao phieu kham",
                "them phieu kham",
                "tao phieu")) {
            return "📘 Cách tạo phiếu khám\n\n" +
                    "Nhập CCCD hoặc mã bênh nhân → Nhấn \"Tìm\" → Chọn lịch hẹn gần nhất (Nếu có), chọn ngày khám, giờ khám dự kiến, bác sĩ khám → Nhập trệu chứng ban đầu → Chọn hình thức thanh toán (Nếu chọn chuyển khoản thì sẽ xuất hiện QR thanh toán) → Nhấn \"Tạo phiếu\".";
        }

        if (containsAny(query,
                "cach xem chi tiet phieu kham",
                "xem chi tiet phieu kham",
                "chi tiet phieu kham",
                "xem phieu kham")) {
            return "📘 Cách xem chi tiết phiếu khám\n\n" +
                    "Vào mục \"Quản lý phiếu khám\" → Nhập CCCD hoặc mã bệnh nhân → Sử dụng bộ lọc và sắp xếp → Nhấn giữ vào hàng phiếu khám cần xem → Chọn Chi tiết.";
        }

        if (containsAny(query,
                "cach huy phieu kham",
                "huy phieu kham",
                "huy phieu") && !query.contains("chuyen")) {
            return "📘 Cách hủy phiếu khám\n\n" +
                    "Vào mục \"Quản lý phiếu khám\" → Nhập CCCD hoặc mã bệnh nhân → Sử dụng bộ lọc và sắp xếp → Nhấn giữ vào hàng phiếu khám cần xem → Chọn Hủy phiếu.";
        }

        if (containsAny(query,
                "thay doi thong tin ca nhan",
                "cap nhat thong tin ca nhan",
                "thong tin ca nhan",
                "doi thong tin ca nhan")) {
            return "📘 Thay đổi thông tin cá nhân\n\n" +
                    "Vào mục Tài khoản → Chọn \"Chỉnh sửa hồ sơ\" → Cập nhật dữ liệu cần thiết → Bấm \"Lưu thay đổi\".";
        }

        if (containsAny(query,
                "doi mat khau",
                "doi password",
                "mat khau")) {
            return "📘 Đổi mật khẩu\n\n" +
                    "Vào mục Tài khoản → Chọn \"Đổi mật khẩu\" → Nhập mật khẩu cũ, mật khẩu mới và xác nhận → Bấm \"Cập nhật\".";
        }

        return null;
    }

    private String answerAdmin(String rawMessage) {
        String query = normalize(rawMessage);

        if (containsAny(query,
                "lam sao chinh sua thong tin nhan vien",
                "chinh sua thong tin nhan vien",
                "sua thong tin nhan vien",
                "sua nhan vien",
                "cap nhat nhan vien")) {
            return "📘 Cách chỉnh sửa thông tin nhân viên\n\n" +
                    "Vào mục \"Quản lý thông tin nhân viên\" → Tìm và chọn nhân viên muốn chỉnh sửa → Nhấn biểu tượng \"Sửa\" (Cây bút) → Thay đổi các thông tin cần thiết → Nhấn \"Lưu\" hoặc \"Cập nhật\".";
        }

        if (containsAny(query,
                "lam sao de them thuoc va dich vu cls",
                "them thuoc va dich vu cls",
                "them thuoc",
                "them dich vu cls",
                "them danh muc")) {
            return "📘 Cách thêm thuốc và dịch vụ CLS\n\n" +
                    "Vào mục \"Quản lý thuốc và dịch vụ CLS\" → Chọn tab \"Danh sách thuốc\" hoặc \"Dịch vụ CLS\" tương ứng → Nhấn nút \"Thêm mới\" (+) → Nhập các thông tin bắt buộc (Tên, đơn giá, đơn vị tính,...) → Nhấn \"Lưu thông tin\" để áp dụng toàn hệ thống.";
        }

        if (containsAny(query,
                "lam sao de phan ca cho nhan vien",
                "phan ca cho nhan vien",
                "phan ca",
                "xep lich lam viec",
                "xep ca")) {
            return "📘 Cách phân ca cho nhân viên\n\n" +
                    "Vào mục \"Phân ca chấm công\" → Chọn ngày cần xếp lịch → Chọn ca làm việc tương ứng (Sáng/Chiều/Tối) → Chọn thêm nhân viên muốn phân ca → Nhân viên sẽ được thêm vào ca tương ứng.";
        }

        if (containsAny(query,
                "lam sao de tao thong bao chung",
                "tao thong bao chung",
                "them thong bao chung",
                "dang thong bao",
                "tao thong bao")) {
            return "📘 Cách tạo thông báo chung\n\n" +
                    "Vào mục \"Quản lí thông báo\" → Chọn \"Tạo thông báo mới\" (Dấu + góc trên bên phải) → Nhập tiêu đề và nội dung thông báo cho phòng khám → chọn chức vụ có thể xem được thông báo → Nhấn \"Gửi thông báo\" để hiển thị tới toàn bộ hệ thống.";
        }

        if (containsAny(query,
                "lam sao de xoa thong bao chung",
                "xoa thong bao chung",
                "xoa thong bao")) {
            return "📘 Cách xóa thông báo chung\n\n" +
                    "Vào mục \"Quản lý thông báo\" → Nhấn vào nút \"Chọn\" → Chọn các thông báo muốn gỡ bõ → Chọn nút \"Xóa\" để gỡ thông báo khỏi hệ thống.";
        }

        if (containsAny(query,
                "thay doi thong tin ca nhan",
                "cap nhat thong tin ca nhan",
                "thong tin ca nhan",
                "doi thong tin ca nhan")) {
            return "📘 Thay đổi thông tin cá nhân\n\n" +
                    "Vào mục Tài khoản → Chọn \"Chỉnh sửa hồ sơ\" → Cập nhật dữ liệu cần thiết → Bấm \"Lưu thay đổi\".";
        }

        if (containsAny(query,
                "doi mat khau",
                "doi password",
                "mat khau")) {
            return "📘 Đổi mật khẩu\n\n" +
                    "Vào mục Tài khoản → Chọn \"Đổi mật khẩu\" → Nhập mật khẩu cũ, mật khẩu mới và xác nhận → Bấm \"Cập nhật\".";
        }

        return null;
    }

    private void askGemini(String userMessage) {

        conversationHistory
                .append("Người dùng: ")
                .append(userMessage)
                .append("\n");

        Executors.newSingleThreadExecutor().execute(() -> {

            try {

                String prompt =
                        "Bạn là MedBot của phòng khám TMH.\n\n" +

                                "NHIỆM VỤ:\n" +
                                "- Chỉ trả lời các câu hỏi liên quan đến y tế.\n" +
                                "- Bao gồm bệnh lý, triệu chứng, thuốc, xét nghiệm, dinh dưỡng, sức khỏe, bệnh viện, bác sĩ, điều trị.\n" +
                                "- Nếu câu hỏi không thuộc lĩnh vực y tế thì KHÔNG được trả lời.\n" +

                                "BẮT BUỘC:\n" +
                                "Nếu câu hỏi không liên quan y tế, chỉ trả lời đúng duy nhất:\n" +
                                "\"Xin lỗi, tôi chỉ hỗ trợ các câu hỏi liên quan đến y tế và sức khỏe.\"\n\n" +

                                "LỊCH SỬ HỘI THOẠI:\n" +
                                conversationHistory.toString() + "\n" +

                                "Hãy trả lời tin nhắn cuối cùng của người dùng.";

                var result = geminiClient.models.generateContent(
                        "gemini-3-flash-preview",
                        prompt,
                        null
                );
                String response = result != null ? result.text() : null;

                if (response == null || response.trim().isEmpty()) {
                    response = "Xin lỗi, hiện tại tôi chưa thể trả lời câu hỏi này.";
                }

                conversationHistory
                        .append("MedBot: ")
                        .append(response)
                        .append("\n");

                if (conversationHistory.length() > 8000) {
                    conversationHistory.delete(
                            0,
                            conversationHistory.length() - 5000
                    );
                }

                String finalResponse = response;

                mainHandler.post(() -> {
                    if (isAdded()) {

                        hideLoadingMessage();

                        appendBotMessage(
                                finalResponse,
                                nowTime()
                        );
                    }
                });

            } catch (Exception e) {

                mainHandler.post(() -> {
                    if (isAdded()) {

                        hideLoadingMessage();

                        appendBotMessage(
                                "Không thể kết nối Gemini. Vui lòng thử lại sau.",
                                nowTime()
                        );
                    }
                });
            }
        });
    }

    private void showLoadingMessage() {

        loadingView = createBotBubble(
                "🔍 Đang tra cứu thông tin",
                nowTime()
        );

        chatMessageContainer.addView(loadingView);

        loadingTextView = loadingView.findViewWithTag("loading_text");

        loadingHandler.post(loadingRunnable);

        scrollToBottom();
    }
    private void hideLoadingMessage() {

        loadingHandler.removeCallbacks(loadingRunnable);

        if (loadingView != null) {
            chatMessageContainer.removeView(loadingView);
            loadingView = null;
            loadingTextView = null;
        }
    }
    private void appendUserMessage(String message, String time) {
        chatMessageContainer.addView(createUserBubble(message, time));
        scrollToBottom();
    }

    private void appendBotMessage(String message, String time) {
        chatMessageContainer.addView(createBotBubble(message, time));
        scrollToBottom();
    }

    private View createUserBubble(String message, String time) {
        LinearLayout wrapper = new LinearLayout(requireContext());
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setGravity(Gravity.END);
        wrapper.setPadding(0, dp(6), 0, dp(6));

        MaterialCardView card = new MaterialCardView(requireContext());
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#1D74E7"),
                        Color.parseColor("#4EA5FF")
                }
        );

        gradient.setCornerRadius(dp(18));

        card.setBackground(gradient);

        card.setRadius(dp(18));
        card.setCardElevation(dp(2));
        card.setUseCompatPadding(false);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dp(72), 0, 0, 0);
        card.setLayoutParams(cardParams);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(12), dp(14), dp(10));

        TextView tvMessage = new TextView(requireContext());
        tvMessage.setText(message);
        tvMessage.setTextColor(Color.WHITE);
        tvMessage.setTextSize(15);
        tvMessage.setLineSpacing(dp(4), 1f);
        tvMessage.setMaxWidth(messageMaxWidth());

        TextView tvTime = new TextView(requireContext());
        tvTime.setText(time);
        tvTime.setTextColor(Color.parseColor("#DDEBFF"));
        tvTime.setTextSize(10);
        tvTime.setTypeface(Typeface.DEFAULT);
        tvTime.setGravity(Gravity.END);
        tvTime.setPadding(0, dp(6), 0, 0);

        content.addView(tvMessage);
        content.addView(tvTime);
        card.addView(content);
        wrapper.addView(card);

        return wrapper;
    }

    private View createBotBubble(String message, String time) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        row.setPadding(0, dp(6), 0, dp(6));

        MaterialCardView avatar = new MaterialCardView(requireContext());
        avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
        avatar.setCardBackgroundColor(Color.parseColor("#1D9BF0"));
        avatar.setRadius(dp(18));
        avatar.setCardElevation(0f);
        avatar.setUseCompatPadding(false);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.ic_chatbot_wrapper);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        avatar.addView(icon, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(dp(16));
        card.setCardElevation(dp(2));
        card.setUseCompatPadding(false);

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        Color.parseColor("#FAFAFA"),
                        Color.parseColor("#ECEFF3")
                }
        );

        gradient.setCornerRadius(dp(16));

        card.setBackground(gradient);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dp(8), 0, dp(40), 0);
        card.setLayoutParams(cardParams);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(12), dp(14), dp(10));

        String[] parts = splitTitleAndBody(message);

        if (parts[0] != null) {
            TextView tvTitle = new TextView(requireContext());
            tvTitle.setText(parts[0]);
            tvTitle.setTextColor(Color.parseColor("#0F172A"));
            tvTitle.setTextSize(14);
            tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
            tvTitle.setMaxWidth(messageMaxWidth());

            content.addView(tvTitle);
        }

        TextView tvMessage = new TextView(requireContext());
        tvMessage.setText(parts[1]);

        if (message.contains("Đang tra cứu thông tin")) {
            tvMessage.setTag("loading_text");
        }

        tvMessage.setTextColor(Color.parseColor("#334155"));
        tvMessage.setTextSize(14);
        tvMessage.setLineSpacing(dp(4), 1f);
        tvMessage.setMaxWidth(messageMaxWidth());

        if (parts[0] != null) {
            LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            bodyParams.topMargin = dp(6);
            tvMessage.setLayoutParams(bodyParams);
        }

        TextView tvTime = new TextView(requireContext());
        tvTime.setText(time);
        tvTime.setTextColor(Color.parseColor("#94A3B8"));
        tvTime.setTextSize(10);
        tvTime.setGravity(Gravity.END);
        tvTime.setPadding(0, dp(6), 0, 0);

        content.addView(tvMessage);
        content.addView(tvTime);

        row.addView(avatar);
        row.addView(card);
        card.addView(content);

        return row;
    }

    private String[] splitTitleAndBody(String message) {
        if (message == null) {
            return new String[]{null, ""};
        }

        String trimmed = message.trim();
        int separatorIndex = trimmed.indexOf("\n\n");
        if (separatorIndex <= 0) {
            return new String[]{null, trimmed};
        }

        String title = trimmed.substring(0, separatorIndex).trim();
        String body = trimmed.substring(separatorIndex + 2).trim();
        return new String[]{title, body};
    }

    private String normalize(String input) {
        String value = input == null ? "" : input.toLowerCase(Locale.getDefault()).trim();

        value = value.replace("đ", "d").replace("Đ", "D");

        String noAccent = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return noAccent.replaceAll("[^a-z0-9\\s/]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String nowTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private int messageMaxWidth() {
        return (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.68f);
    }

    private void scrollToBottom() {
        if (scrollMessages == null) return;
        scrollMessages.post(() -> scrollMessages.fullScroll(View.FOCUS_DOWN));
    }

    private void setSuggestionTexts(
            MaterialButton btn1,
            MaterialButton btn2,
            MaterialButton btn3,
            MaterialButton btn4,
            MaterialButton btn5,
            String s1,
            String s2,
            String s3,
            String s4,
            String s5
    ) {
        btn1.setText(s1);
        btn2.setText(s2);
        btn3.setText(s3);
        btn4.setText(s4);
        btn5.setText(s5);
    }
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == android.app.Activity.RESULT_OK
                                && result.getData() != null) {

                            ArrayList<String> results =
                                    result.getData().getStringArrayListExtra(
                                            RecognizerIntent.EXTRA_RESULTS);

                            if (results != null && !results.isEmpty()) {
                                edtChatInput.setText(results.get(0));
                                edtChatInput.setSelection(
                                        edtChatInput.getText().length()
                                );
                            }
                        }
                    }
            );
    private final ActivityResultLauncher<String> requestAudioPermission =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startVoiceInput();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Cần quyền micro để sử dụng tính năng này",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
    private void startVoiceInput() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            requestAudioPermission.launch(
                    Manifest.permission.RECORD_AUDIO
            );
            return;
        }

        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "vi-VN"
        );
        speechLauncher.launch(intent);
    }

}