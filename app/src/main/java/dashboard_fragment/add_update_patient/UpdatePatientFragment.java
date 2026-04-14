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

public class UpdatePatientFragment extends Fragment {

    public UpdatePatientFragment() { /* Required empty constructor */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_patient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void submitPatientData() {
        Toast.makeText(getContext(), "Update Patient Logic here!", Toast.LENGTH_SHORT).show();
    }
}