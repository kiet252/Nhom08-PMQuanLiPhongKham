package dashboard_fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nhom08_quanlyphongkham.R;
import com.example.nhom08_quanlyphongkham.uilogin.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    private static final String ARG_PROFILE = "arg_profile";
    private UserProfile userprofile;
    TextView textviewProfile, textviewEmail, textviewPhone, textviewBirthday, textviewGender, textviewAddress, textviewJobTitle;

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
            userprofile =(UserProfile) getArguments().getSerializable(ARG_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        initializeViews(view);
        setViewProfile(view);

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
    }

    private void setViewProfile(View view) {
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
}