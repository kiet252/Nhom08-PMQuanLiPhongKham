package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.diagnosis_logic.DiagnosisOption;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.diagnosis_logic.DiagnosisSeedData;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordDiagnosisWrapper;

public class TabDiagnosisFragment extends Fragment {

    private final Map<String, Boolean> expandedStates = new LinkedHashMap<>();
    private final Map<String, Integer> selectedCounts = new LinkedHashMap<>();
    private final Map<String, LinearLayout> optionContainers = new LinkedHashMap<>();
    private final Map<String, View> arrows = new LinkedHashMap<>();
    private final Map<String, TextView> badges = new LinkedHashMap<>();
    private final LinkedHashSet<String> selectedDiagnoses = new LinkedHashSet<>();

    private Map<String, List<DiagnosisOption>> diagnosisGroups;
    private LinearLayout containerDiagnosisGroups;
    private TextView tvPrimaryDiagnosis;
    private MedicalRecordDiagnosisWrapper DiagnosisData;

    public TabDiagnosisFragment(MedicalRecordDiagnosisWrapper DiagnosisData) {
        this.DiagnosisData = DiagnosisData;
    }

    public TabDiagnosisFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_diagnosis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        diagnosisGroups = DiagnosisSeedData.getDiagnosisGroups();
        containerDiagnosisGroups = view.findViewById(R.id.containerDiagnosisGroups);
        tvPrimaryDiagnosis = view.findViewById(R.id.tvPrimaryDiagnosis);

        renderDiagnosisGroups();
        setupContinueButton(view);
        updatePrimaryDiagnosisText();
    }

    private void renderDiagnosisGroups() {
        containerDiagnosisGroups.removeAllViews();

        for (Map.Entry<String, List<DiagnosisOption>> entry : diagnosisGroups.entrySet()) {
            String groupName = entry.getKey();
            List<DiagnosisOption> options = entry.getValue();

            expandedStates.put(groupName, false);
            selectedCounts.put(groupName, 0);

            containerDiagnosisGroups.addView(createDiagnosisGroupCard(groupName, options));
        }
    }

    private View createDiagnosisGroupCard(String groupName, List<DiagnosisOption> options) {
        com.google.android.material.card.MaterialCardView card =
                new com.google.android.material.card.MaterialCardView(requireContext());

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(18));
        card.setCardElevation(dp(2));
        card.setCardBackgroundColor(0xFFFFFFFF);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setClickable(true);
        header.setFocusable(true);

        TextView title = new TextView(requireContext());
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        title.setText(groupName);
        title.setTextColor(0xFF0D3F6E);
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView badge = new TextView(requireContext());
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        badgeParams.rightMargin = dp(10);
        badge.setLayoutParams(badgeParams);
        badge.setGravity(Gravity.CENTER);
        badge.setText("0");
        badge.setTextColor(0xFFFFFFFF);
        badge.setTextSize(12);
        badge.setTypeface(null, android.graphics.Typeface.BOLD);
        badge.setBackground(createCountBadgeBackground());
        badge.setVisibility(View.GONE);

        android.widget.ImageView arrow = new android.widget.ImageView(requireContext());
        arrow.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        arrow.setImageResource(R.drawable.ic_chevron_down);
        arrow.setColorFilter(0xFF0D3F6E);

        header.addView(title);
        header.addView(badge);
        header.addView(arrow);

        LinearLayout optionsContainer = new LinearLayout(requireContext());
        optionsContainer.setOrientation(LinearLayout.VERTICAL);
        optionsContainer.setVisibility(View.GONE);
        optionsContainer.setPadding(0, dp(14), 0, 0);

        for (DiagnosisOption option : options) {
            optionsContainer.addView(createDiagnosisOptionRow(groupName, option));
        }

        header.setOnClickListener(v -> toggleGroup(groupName));

        root.addView(header);
        root.addView(optionsContainer);
        card.addView(root);

        optionContainers.put(groupName, optionsContainer);
        arrows.put(groupName, arrow);
        badges.put(groupName, badge);

        return card;
    }

    private View createDiagnosisOptionRow(String groupName, DiagnosisOption option) {
        LinearLayout row = new LinearLayout(requireContext());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(8);
        row.setLayoutParams(rowParams);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(createOptionBackground());

        android.widget.ImageView tickView = new android.widget.ImageView(requireContext());
        LinearLayout.LayoutParams tickParams = new LinearLayout.LayoutParams(dp(22), dp(22));
        tickParams.rightMargin = dp(10);
        tickView.setLayoutParams(tickParams);
        tickView.setImageResource(R.drawable.bg_clinical_circle_unchecked);
        tickView.setTag(false);

        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        textView.setText(option.getDiagnosisName());
        textView.setTextColor(0xFF334155);
        textView.setTextSize(14);

        row.addView(tickView);
        row.addView(textView);

        row.setOnClickListener(v -> {
            boolean isChecked = Boolean.TRUE.equals(tickView.getTag());

            if (isChecked) {
                tickView.setImageResource(R.drawable.bg_clinical_circle_unchecked);
                tickView.setTag(false);
                textView.setTextColor(0xFF334155);

                selectedDiagnoses.remove(option.getDiagnosisName());
                decreaseGroupCount(groupName);
            } else {
                tickView.setImageDrawable(createCheckedCircleDrawable(0xFF0D3F6E));
                tickView.setTag(true);
                textView.setTextColor(0xFF0D3F6E);

                selectedDiagnoses.add(option.getDiagnosisName());
                increaseGroupCount(groupName);
            }

            updatePrimaryDiagnosisText();
        });

        return row;
    }

    private void toggleGroup(String groupName) {
        LinearLayout container = optionContainers.get(groupName);
        View arrow = arrows.get(groupName);
        boolean isExpanded = Boolean.TRUE.equals(expandedStates.get(groupName));

        if (container == null || arrow == null) return;

        if (isExpanded) {
            container.setVisibility(View.GONE);
            arrow.animate().rotation(0f).setDuration(180).start();
        } else {
            container.setVisibility(View.VISIBLE);
            arrow.animate().rotation(180f).setDuration(180).start();
        }

        expandedStates.put(groupName, !isExpanded);
    }

    private void increaseGroupCount(String groupName) {
        int newCount = selectedCounts.get(groupName) + 1;
        selectedCounts.put(groupName, newCount);
        updateBadge(groupName, newCount);
    }

    private void decreaseGroupCount(String groupName) {
        int current = selectedCounts.get(groupName);
        int newCount = Math.max(0, current - 1);
        selectedCounts.put(groupName, newCount);
        updateBadge(groupName, newCount);
    }

    private void updateBadge(String groupName, int count) {
        TextView badge = badges.get(groupName);
        if (badge == null) return;

        if (count <= 0) {
            badge.setVisibility(View.GONE);
        } else {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(count));
        }
    }

    private void updatePrimaryDiagnosisText() {
        if (selectedDiagnoses.isEmpty()) {
            tvPrimaryDiagnosis.setText("Chưa chọn chẩn đoán");
            tvPrimaryDiagnosis.setTextColor(0xFF64748B);
            return;
        }

        List<String> selectedList = new ArrayList<>(selectedDiagnoses);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < selectedList.size(); i++) {
            builder.append("• ").append(selectedList.get(i));
            if (i < selectedList.size() - 1) {
                builder.append("\n");
            }
        }

        tvPrimaryDiagnosis.setText(builder.toString());
        tvPrimaryDiagnosis.setTextColor(0xFF1E293B);
    }

    private void setupContinueButton(View view) {
        View btnContinue = view.findViewById(R.id.btnContinueDiagnosis);
        btnContinue.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tiếp tục — Kê đơn thuốc", Toast.LENGTH_SHORT).show()
        );
    }

    private GradientDrawable createOptionBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(0xFFF8FAFC);
        drawable.setCornerRadius(dp(12));
        drawable.setStroke(dp(1), 0xFFD8DEE9);
        return drawable;
    }

    private GradientDrawable createCountBadgeBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(0xFF0D3F6E);
        return drawable;
    }

    private android.graphics.drawable.Drawable createCheckedCircleDrawable(int color) {
        android.graphics.drawable.LayerDrawable layerDrawable =
                (android.graphics.drawable.LayerDrawable)
                        androidx.core.content.ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.bg_clinical_circle_checked
                        ).mutate();

        GradientDrawable circle = (GradientDrawable) layerDrawable.getDrawable(0);
        circle.setColor(color);

        return layerDrawable;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
