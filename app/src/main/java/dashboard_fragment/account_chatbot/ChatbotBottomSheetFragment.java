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

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import dashboard_fragment.UserRole;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

        btnCloseChatbot.setOnClickListener(v -> dismiss());

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
            Toast.makeText(requireContext(), "Nhập câu hỏi trước khi gửi", Toast.LENGTH_SHORT).show();
            return;
        }

        appendUserMessage(message, nowTime());
        edtChatInput.setText("");

        mainHandler.postDelayed(() -> {
            if (!isAdded()) return;
            appendBotMessage(answerQuery(message), nowTime());
        }, 300);
    }

    private String answerQuery(String rawMessage) {

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

        return "Xin lỗi, tôi không hiểu câu hỏi này."; // Chỗ này Kiệt Cao sửa lại thêm 2 role kia vào nhé (answerAdmin với answerStaff)

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
                "dich vu can lam sang",
                "can lam sang",
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
                "nhap chan doan",
                "chan doan")) {
            return "📘 Cách chọn/nhập chẩn đoán\n\n" +
                    "Nhấn vào \"Danh sách phiếu khám\" → Mở phiếu cần xử lý → Mở tab \"Chẩn đoán\" → Chọn chẩn đoán có sẵn hoặc nhập nội dung mới → Kiểm tra lại thông tin → Chuyển sang tab \"Đơn thuốc\" → Nhấn \"Lưu bệnh án\" để xác nhận.";
        }

        if (containsAny(query,
                "ke don thuoc",
                "ke don",
                "don thuoc",
                "thuoc")) {
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
                "lich hen",
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

        return "Mình chưa hiểu rõ câu hỏi này.\n\n" +
                "Mình chỉ hỗ trợ các chức năng dành cho Bác sĩ. Bạn có thể bấm một trong các câu gợi ý phía dưới hoặc nhập cụ thể chức năng bạn muốn xem.";
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
                "huy phieu") && !query.contains("chuyen")) { // Tránh nhầm với đổi trạng thái
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

        return "Mình chưa hiểu rõ câu hỏi này.\n\n" +
                "Mình chỉ hỗ trợ các chức năng dành cho Nhân viên. Bạn có thể bấm một trong các câu gợi ý phía dưới hoặc nhập cụ thể chức năng bạn muốn xem.";
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

        return "Mình chưa hiểu rõ câu hỏi này.\n\n" +
                "Mình chỉ hỗ trợ các chức năng dành cho Quản trị viên. Bạn có thể bấm một trong các câu gợi ý phía dưới hoặc nhập cụ thể chức năng bạn muốn xem.";
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