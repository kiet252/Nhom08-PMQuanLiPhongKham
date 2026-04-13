package dashboard_fragment.add_update_patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;

public class CreatePatientFragment extends Fragment {
    private String currentToken;

    public CreatePatientFragment(String token) {
        currentToken = token;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_patient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void saveData() {
        Toast.makeText(getContext(), "Create Patient Logic here!", Toast.LENGTH_SHORT).show();
    }
}