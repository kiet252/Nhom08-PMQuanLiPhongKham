package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhom08_quanlyphongkham.R;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordClinicalWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TabClinicalFragment extends Fragment {

    private static final String SECTION_BLOOD = "blood";
    private static final String SECTION_IMAGING = "imaging";
    private static final String SECTION_ULTRASOUND = "ultrasound";
    private static final String SECTION_ENDOSCOPY = "endoscopy";
    private static final String SECTION_AUDIOLOGY = "audiology";

    private final Map<String, LinearLayout> optionContainers = new LinkedHashMap<>();
    private final Map<String, View> arrows = new LinkedHashMap<>();
    private final Map<String, Boolean> expandedStates = new LinkedHashMap<>();
    private final Map<String, Integer> sectionColors = new LinkedHashMap<>();
    private final Map<String, Integer> selectedCounts = new LinkedHashMap<>();
    private final Map<String, TextView> badges = new LinkedHashMap<>();

    private ClinicalRepository clinicalRepository;
    private List<ClinicalItem> availableClinicalItems;
    private DoctorExDetailViewModel doctorExDetailViewModel;

    public TabClinicalFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_clinical, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clinicalRepository = new ClinicalRepository(requireContext());
        doctorExDetailViewModel =
                new ViewModelProvider(requireActivity()).get(DoctorExDetailViewModel.class);

        bindSectionViews(view);
        bindSectionColors();
        initializeSectionCounts();
        attachSectionBadges();
        setupSectionClicks(view);
        setupPreviewButton(view);
        observeMedicalRecord();
        loadClinicalItems();
    }

    private void observeMedicalRecord() {
        doctorExDetailViewModel.getMedicalRecord().observe(getViewLifecycleOwner(), record -> {
            Set<Integer> preselectedClinicalIds = new LinkedHashSet<>();

            if (record != null && record.getClinicalData() != null) {
                for (MedicalRecordClinicalWrapper wrapper : record.getClinicalData()) {
                    if (wrapper == null || wrapper.getClinical() == null) {
                        continue;
                    }

                    int clinicalId = wrapper.getClinical().getId();
                    if (clinicalId > 0) {
                        preselectedClinicalIds.add(clinicalId);
                    }
                }
            }

            doctorExDetailViewModel.initializeSelectedClinicalIds(preselectedClinicalIds);
            renderClinicalItemsIfReady();
        });
    }

    private void bindSectionViews(View view) {
        optionContainers.put(SECTION_BLOOD, view.findViewById(R.id.containerBloodOptions));
        optionContainers.put(SECTION_IMAGING, view.findViewById(R.id.containerImagingOptions));
        optionContainers.put(SECTION_ULTRASOUND, view.findViewById(R.id.containerUltrasoundOptions));
        optionContainers.put(SECTION_ENDOSCOPY, view.findViewById(R.id.containerEndoscopyOptions));
        optionContainers.put(SECTION_AUDIOLOGY, view.findViewById(R.id.containerAudiologyOptions));

        arrows.put(SECTION_BLOOD, view.findViewById(R.id.arrowBlood));
        arrows.put(SECTION_IMAGING, view.findViewById(R.id.arrowImaging));
        arrows.put(SECTION_ULTRASOUND, view.findViewById(R.id.arrowUltrasound));
        arrows.put(SECTION_ENDOSCOPY, view.findViewById(R.id.arrowEndoscopy));
        arrows.put(SECTION_AUDIOLOGY, view.findViewById(R.id.arrowAudiology));

        expandedStates.put(SECTION_BLOOD, false);
        expandedStates.put(SECTION_IMAGING, false);
        expandedStates.put(SECTION_ULTRASOUND, false);
        expandedStates.put(SECTION_ENDOSCOPY, false);
        expandedStates.put(SECTION_AUDIOLOGY, false);
    }

    private void bindSectionColors() {
        sectionColors.put(SECTION_BLOOD, 0xFFE74C78);
        sectionColors.put(SECTION_IMAGING, 0xFF3D7BE0);
        sectionColors.put(SECTION_ULTRASOUND, 0xFF0F9FAF);
        sectionColors.put(SECTION_ENDOSCOPY, 0xFF9B59B6);
        sectionColors.put(SECTION_AUDIOLOGY, 0xFFF39C12);
    }

    private void initializeSectionCounts() {
        for (String key : optionContainers.keySet()) {
            selectedCounts.put(key, 0);
        }
    }

    private void attachSectionBadges() {
        attachBadgeToHeader(R.id.headerBlood, R.id.arrowBlood, SECTION_BLOOD, 0xFFE74C78);
        attachBadgeToHeader(R.id.headerImaging, R.id.arrowImaging, SECTION_IMAGING, 0xFF3D7BE0);
        attachBadgeToHeader(R.id.headerUltrasound, R.id.arrowUltrasound, SECTION_ULTRASOUND, 0xFF0F9FAF);
        attachBadgeToHeader(R.id.headerEndoscopy, R.id.arrowEndoscopy, SECTION_ENDOSCOPY, 0xFF9B59B6);
        attachBadgeToHeader(R.id.headerAudiology, R.id.arrowAudiology, SECTION_AUDIOLOGY, 0xFFF39C12);
    }

    private void attachBadgeToHeader(int headerResId, int arrowResId, String sectionKey, int accentColor) {
        View root = getView();
        if (root == null) {
            return;
        }

        LinearLayout headerLayout = root.findViewById(headerResId);
        ImageView arrowView = root.findViewById(arrowResId);
        if (headerLayout == null || arrowView == null) {
            return;
        }

        ViewGroup row = (ViewGroup) arrowView.getParent();
        if (row == null) {
            return;
        }

        TextView badge = new TextView(requireContext());
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        badgeParams.rightMargin = dp(10);
        badge.setLayoutParams(badgeParams);
        badge.setGravity(Gravity.CENTER);
        badge.setText("0");
        badge.setTextColor(0xFFFFFFFF);
        badge.setTextSize(12);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setBackground(createCountBadgeBackground(accentColor));
        badge.setVisibility(View.GONE);

        int arrowIndex = row.indexOfChild(arrowView);
        row.addView(badge, arrowIndex);
        badges.put(sectionKey, badge);
    }

    private void setupSectionClicks(View view) {
        view.findViewById(R.id.headerBlood).setOnClickListener(v -> toggleSection(SECTION_BLOOD));
        view.findViewById(R.id.headerImaging).setOnClickListener(v -> toggleSection(SECTION_IMAGING));
        view.findViewById(R.id.headerUltrasound).setOnClickListener(v -> toggleSection(SECTION_ULTRASOUND));
        view.findViewById(R.id.headerEndoscopy).setOnClickListener(v -> toggleSection(SECTION_ENDOSCOPY));
        view.findViewById(R.id.headerAudiology).setOnClickListener(v -> toggleSection(SECTION_AUDIOLOGY));
    }

    private void setupPreviewButton(View view) {
        View btnContinue = view.findViewById(R.id.btnContinueClinical);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v ->
                    ((ExaminationFormDetail_doctor) requireActivity()).navigateToDiagnosisTab()
            );
        }
    }

    private void toggleSection(String sectionKey) {
        LinearLayout container = optionContainers.get(sectionKey);
        View arrow = arrows.get(sectionKey);
        boolean isExpanded = Boolean.TRUE.equals(expandedStates.get(sectionKey));

        if (container == null || arrow == null) {
            return;
        }

        if (isExpanded) {
            container.setVisibility(View.GONE);
            arrow.animate().rotation(0f).setDuration(180).start();
        } else {
            container.setVisibility(View.VISIBLE);
            arrow.animate().rotation(180f).setDuration(180).start();
        }

        expandedStates.put(sectionKey, !isExpanded);
    }

    private void loadClinicalItems() {
        clinicalRepository.getAllClinicalItems().enqueue(new Callback<List<ClinicalItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<ClinicalItem>> call,
                                   @NonNull Response<List<ClinicalItem>> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    availableClinicalItems = response.body();
                    renderClinicalItemsIfReady();
                } else {
                    Toast.makeText(requireContext(), "Không tải được dữ liê cận lâm sàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ClinicalItem>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }

                Toast.makeText(requireContext(), "Loi ket noi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderClinicalItemsIfReady() {
        if (availableClinicalItems == null || !isAdded()) {
            return;
        }
        bindClinicalItems(availableClinicalItems);
    }

    private void bindClinicalItems(List<ClinicalItem> items) {
        for (LinearLayout container : optionContainers.values()) {
            container.removeAllViews();
        }

        for (String key : selectedCounts.keySet()) {
            selectedCounts.put(key, 0);
            updateBadge(key, 0);
        }

        for (ClinicalItem item : items) {
            String sectionKey = resolveSectionKey(item.getLoai_dich_vu());
            if (sectionKey == null) {
                continue;
            }

            LinearLayout container = optionContainers.get(sectionKey);
            Integer accentColor = sectionColors.get(sectionKey);
            if (container == null) {
                continue;
            }

            boolean isPreselected = doctorExDetailViewModel.isClinicalSelected(item.getId());
            container.addView(createClinicalOption(
                    sectionKey,
                    item,
                    accentColor != null ? accentColor : 0xFF64748B,
                    isPreselected
            ));
            if (isPreselected) {
                increaseSectionCount(sectionKey);
            }
        }
    }

    private String resolveSectionKey(String rawType) {
        String normalizedType = normalizeLoose(rawType);

        if (normalizedType.contains("xet nghiem mau")) {
            return SECTION_BLOOD;
        }
        if (normalizedType.contains("chan doan hinh anh")) {
            return SECTION_IMAGING;
        }
        if (normalizedType.contains("sieu am")) {
            return SECTION_ULTRASOUND;
        }
        if (normalizedType.contains("noi soi")) {
            return SECTION_ENDOSCOPY;
        }
        if (normalizedType.contains("thinh hoc")) {
            return SECTION_AUDIOLOGY;
        }
        return null;
    }

    private View createClinicalOption(String sectionKey, ClinicalItem item, int accentColor, boolean isInitiallyChecked) {
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

        ImageView tickView = new ImageView(requireContext());
        LinearLayout.LayoutParams tickParams = new LinearLayout.LayoutParams(dp(22), dp(22));
        tickParams.rightMargin = dp(10);
        tickView.setLayoutParams(tickParams);
        tickView.setImageResource(R.drawable.bg_clinical_circle_unchecked);
        tickView.setTag(false);

        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        textView.setText(item.getTen_dich_vu());
        textView.setTextColor(0xFF334155);
        textView.setTextSize(14);

        if (isInitiallyChecked) {
            tickView.setImageDrawable(createCheckedCircleDrawable(accentColor));
            tickView.setTag(true);
            textView.setTextColor(accentColor);
        }

        row.addView(tickView);
        row.addView(textView);

        row.setOnClickListener(v -> {
            boolean isChecked = Boolean.TRUE.equals(tickView.getTag());

            if (isChecked) {
                tickView.setImageResource(R.drawable.bg_clinical_circle_unchecked);
                tickView.setTag(false);
                textView.setTextColor(0xFF334155);
                doctorExDetailViewModel.setClinicalSelected(item.getId(), false);
                decreaseSectionCount(sectionKey);
            } else {
                tickView.setImageDrawable(createCheckedCircleDrawable(accentColor));
                tickView.setTag(true);
                textView.setTextColor(accentColor);
                doctorExDetailViewModel.setClinicalSelected(item.getId(), true);
                increaseSectionCount(sectionKey);
            }
        });

        return row;
    }

    private void increaseSectionCount(String sectionKey) {
        int current = selectedCounts.containsKey(sectionKey) ? selectedCounts.get(sectionKey) : 0;
        int newCount = current + 1;
        selectedCounts.put(sectionKey, newCount);
        updateBadge(sectionKey, newCount);
    }

    private void decreaseSectionCount(String sectionKey) {
        int current = selectedCounts.containsKey(sectionKey) ? selectedCounts.get(sectionKey) : 0;
        int newCount = Math.max(0, current - 1);
        selectedCounts.put(sectionKey, newCount);
        updateBadge(sectionKey, newCount);
    }

    private void updateBadge(String sectionKey, int count) {
        TextView badge = badges.get(sectionKey);
        if (badge == null) {
            return;
        }

        if (count <= 0) {
            badge.setVisibility(View.GONE);
        } else {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(count));
        }
    }

    private GradientDrawable createOptionBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(0xFFF8FAFC);
        drawable.setCornerRadius(dp(12));
        drawable.setStroke(dp(1), 0xFFD8DEE9);
        return drawable;
    }

    private GradientDrawable createCountBadgeBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        return drawable;
    }

    private android.graphics.drawable.Drawable createCheckedCircleDrawable(int color) {
        android.graphics.drawable.LayerDrawable layerDrawable =
                (android.graphics.drawable.LayerDrawable)
                        androidx.core.content.ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.bg_clinical_circle_checked
                        ).mutate();

        android.graphics.drawable.GradientDrawable circle =
                (android.graphics.drawable.GradientDrawable) layerDrawable.getDrawable(0);
        circle.setColor(color);

        return layerDrawable;
    }

    private String normalizeLoose(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
        return normalized.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
