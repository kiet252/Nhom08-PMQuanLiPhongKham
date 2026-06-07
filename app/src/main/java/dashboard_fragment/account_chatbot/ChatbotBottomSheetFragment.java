package dashboard_fragment.account_chatbot;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import dashboard_fragment.UserRole;


public class ChatbotBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_ROLE = "arg_role";

    public static ChatbotBottomSheetFragment newInstance(String roleName) {
        ChatbotBottomSheetFragment fragment = new ChatbotBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, roleName);
        fragment.setArguments(args);
        return fragment;
    }

    private String roleName = UserRole.NHAN_VIEN.name();

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

        MaterialTextView tvBotRoleHint = view.findViewById(R.id.tvBotRoleHint);
        MaterialTextView tvWelcomeMessage = view.findViewById(R.id.tvWelcomeMessage);
        MaterialButton btnSuggestion1 = view.findViewById(R.id.btnSuggestion1);
        MaterialButton btnSuggestion2 = view.findViewById(R.id.btnSuggestion2);
        MaterialButton btnSuggestion3 = view.findViewById(R.id.btnSuggestion3);
        MaterialButton btnSuggestion4 = view.findViewById(R.id.btnSuggestion4);
        MaterialButton btnSuggestion5 = view.findViewById(R.id.btnSuggestion5);
        EditText edtChatInput = view.findViewById(R.id.edtChatInput);
        ImageButton btnCloseChatbot = view.findViewById(R.id.btnCloseChatbot);
        ImageButton btnSendChat = view.findViewById(R.id.btnSendChat);

        applyRoleContent(
                tvBotRoleHint,
                tvWelcomeMessage,
                btnSuggestion1,
                btnSuggestion2,
                btnSuggestion3,
                btnSuggestion4,
                btnSuggestion5
        );

        btnCloseChatbot.setOnClickListener(v -> dismiss());

        btnSuggestion1.setOnClickListener(v -> edtChatInput.setText(btnSuggestion1.getText().toString()));
        btnSuggestion2.setOnClickListener(v -> edtChatInput.setText(btnSuggestion2.getText().toString()));
        btnSuggestion3.setOnClickListener(v -> edtChatInput.setText(btnSuggestion3.getText().toString()));

        btnSendChat.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Phần trả lời chatbot", Toast.LENGTH_SHORT).show()
        );
    }

    private void applyRoleContent(
            MaterialTextView tvBotRoleHint,
            MaterialTextView tvWelcomeMessage,
            MaterialButton btnSuggestion1,
            MaterialButton btnSuggestion2,
            MaterialButton btnSuggestion3,
            MaterialButton btnSuggestion4,
            MaterialButton btnSuggestion5
    ) {
        if (UserRole.ADMIN.name().equals(roleName)) {
            tvBotRoleHint.setText("Hỗ trợ tác vụ dành cho Quản trị viên");
            tvWelcomeMessage.setText(
                    "Xin chào Quản trị viên! Tôi là MedBot - trợ lý AI của phòng khám TMH.\n" +
                            "Tôi có thể giúp bạn hướng dẫn sử dụng hệ thống, trả lời thắc mắc về các tính năng. " +
                            "Hãy hỏi tôi bất cứ điều gì!"
            );

            setSuggestionTexts(
                    btnSuggestion1, btnSuggestion2, btnSuggestion3, btnSuggestion4, btnSuggestion5,
                    "Làm sao để thêm nhân viên mới?",
                    "Làm sao để xóa nhân viên?",
                    "Làm sao xem thống kê báo cáo?",
                    "Làm sao để tạo thông báo chung?",
                    "Làm sao để xóa thông báo chung?"
            );

        } else if (UserRole.BAC_SI.name().equals(roleName)) {
            tvBotRoleHint.setText("Hỗ trợ tác vụ dành cho Bác sĩ");
            tvWelcomeMessage.setText(
                    "Xin chào Bác sĩ! Tôi là MedBot - trợ lý AI của phòng khám TMH.\n" +
                            "Tôi có thể giúp bạn hướng dẫn sử dụng hệ thống, trả lời thắc mắc về các tính năng. " +
                            "Hãy hỏi tôi bất cứ điều gì!"
            );

            setSuggestionTexts(
                    btnSuggestion1, btnSuggestion2, btnSuggestion3, btnSuggestion4, btnSuggestion5,
                    "Cách đổi trạng thái phiếu khám",
                    "Cách chọn dịch vụ cận lâm sàng",
                    "Cách chọn/nhập chẩn đoán",
                    "Cách kê đơn thuốc",
                    "Cách xem bệnh án"
            );

        } else {
            tvBotRoleHint.setText("Hỗ trợ tác vụ dành cho Nhân viên");
            tvWelcomeMessage.setText(
                    "Xin chào Nhân viên! Tôi là MedBot - trợ lý AI của phòng khám TMH.\n" +
                            "Tôi có thể giúp bạn hướng dẫn sử dụng hệ thống, trả lời thắc mắc về các tính năng. " +
                            "Hãy hỏi tôi bất cứ điều gì!"
            );

            setSuggestionTexts(
                    btnSuggestion1, btnSuggestion2, btnSuggestion3, btnSuggestion4, btnSuggestion5,
                    "Cách tạo hồ sơ cho bệnh nhân mới",
                    "Cách cập nhật thông tin cho bệnh nhân",
                    "Cách tạo phiếu khám",
                    "Cách xem chi tiết phiếu khám",
                    "Cách sửa thông tin cá nhân"
            );
        }
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
}
