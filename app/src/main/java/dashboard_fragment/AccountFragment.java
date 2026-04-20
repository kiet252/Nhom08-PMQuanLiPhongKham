package dashboard_fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.UserProfile;
import com.example.nhom08_quanlyphongkham.login;
import com.example.nhom08_quanlyphongkham.uilogin.AuthRepository;
import com.example.nhom08_quanlyphongkham.uilogin.LoginResponse;
import com.example.nhom08_quanlyphongkham.uilogin.SharedPrefManager;
import com.example.nhom08_quanlyphongkham.uilogin.SupabaseClientProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    private static final String ARG_PROFILE = "arg_profile";
    private UserProfile userprofile;

    TextView textviewProfile, textviewEmail, textviewPhone, textviewBirthday, textviewGender, textviewAddress, textviewJobTitle;
    Button btnEditPass;
    Button btnLogout;
    private AuthRepository authRepository;

    public AccountFragment() {
    }

    public static AccountFragment newInstance(UserProfile profile) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROFILE, profile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userprofile = (UserProfile) getArguments().getSerializable(ARG_PROFILE);
        }

        authRepository = new AuthRepository(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        initializeViews(view);
        setViewProfile();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        textviewProfile = view.findViewById(R.id.txtProfileName);
        textviewEmail = view.findViewById(R.id.txtEmail);
        textviewPhone = view.findViewById(R.id.txtPhone);
        textviewBirthday = view.findViewById(R.id.txtBirthday);
        textviewGender = view.findViewById(R.id.txtGender);
        textviewAddress = view.findViewById(R.id.txtAddress);
        textviewJobTitle = view.findViewById(R.id.txtJobTitle);

        btnEditPass = view.findViewById(R.id.btnEditPass);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setViewProfile() {
        String GreetingText = "Xin chào, " + userprofile.getHo_ten();

        textviewProfile.setText(GreetingText);

        textviewEmail.setText(userprofile.getEmail());
        textviewPhone.setText(userprofile.getSo_dien_thoai());

        Date NgaySinhUser = userprofile.getNgay_sinh();

        if (NgaySinhUser != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            textviewBirthday.setText(sdf.format(NgaySinhUser));
        }

        textviewGender.setText(userprofile.getGioitinh());
        textviewAddress.setText(userprofile.getDia_chi());
        textviewJobTitle.setText(userprofile.getChuc_vu());
    }

    private void setupListeners() {
        btnEditPass.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v->showLogoutDialog());
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.change_password_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setOnShowListener(d -> Change_Password_Dialog_Show_UI(dialog, dialogView));

        dialog.show();
    }

    private void Change_Password_Dialog_Show_UI(AlertDialog dialog, View dialogView) {
        Button btnConfirm = dialog.findViewById(R.id.btnUpdatePassword);
        Button btnCancel = dialog.findViewById(R.id.btnChangePassExit);

        TextInputEditText edtCurrentPassword = dialogView.findViewById(R.id.edtCurrentPassword);
        TextInputEditText edtNewPassword = dialogView.findViewById(R.id.edtNewPassword);
        TextInputEditText edtConfirmPassword = dialogView.findViewById(R.id.edtConfirmPassword);

        TextInputLayout layoutCurrentPassword = dialogView.findViewById(R.id.layoutCurrentPassword);
        TextInputLayout layoutNewPassword = dialogView.findViewById(R.id.layoutNewPassword);
        TextInputLayout layoutConfirmPassword = dialogView.findViewById(R.id.layoutConfirmPassword);

        btnConfirm.setOnClickListener(v -> {
            layoutCurrentPassword.setError(null);
            layoutNewPassword.setError(null);
            layoutConfirmPassword.setError(null);

            String currentPassword = edtCurrentPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (currentPassword.isEmpty()) {
                layoutCurrentPassword.setError("Xin hãy nhập mật khẩu hiện tại!");
                return;
            }

            if (newPassword.isEmpty()) {
                layoutNewPassword.setError("Xin hãy nhập mật khẩu mới!");
                return;
            }

            if (confirmPassword.isEmpty()) {
                layoutConfirmPassword.setError("Xin hãy xác nhận mật khẩu mới!");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                layoutConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
                return;
            }

            if (newPassword.length() < 6) {
                layoutNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự!");
                return;
            }

            verifyCurrentPassword(currentPassword, newPassword, layoutCurrentPassword, dialog);
        });

        btnCancel.setOnClickListener(v ->{
            dialog.dismiss();
        });
    }

    private void verifyCurrentPassword(String currentPassword, String newPassword, TextInputLayout layoutCurrentPassword, AlertDialog dialog) {
        authRepository.login(userprofile.getEmail(), currentPassword).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    layoutCurrentPassword.setError("Mật khẩu hiện tại sai! Xin hãy nhập lại");
                } else {
                    changePassword(newPassword, dialog);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changePassword(String newPassword, AlertDialog dialog) {
        authRepository.updatePassword(newPassword).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    try {
                        Toast.makeText(requireContext(), "Error" + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Unknown error" + e, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showLogoutDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.log_out_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setOnShowListener(d -> Logout_Dialog_Show_UI(dialog, dialogView));

        dialog.show();
    }
    private void Logout_Dialog_Show_UI(AlertDialog dialog, View dialogView) {
        Button btnLogoutConfirm = dialogView.findViewById(R.id.btnLogoutConfirm);
        Button btnLogoutCancel = dialogView.findViewById(R.id.btnLogoutCancel);

        btnLogoutConfirm.setOnClickListener(v -> {
            // Clean up using your manager
            SharedPrefManager.getInstance(requireContext()).clear();
            dialog.dismiss();

            Intent intent = new Intent(requireContext(), login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        btnLogoutCancel.setOnClickListener(v -> dialog.dismiss());
    }
}