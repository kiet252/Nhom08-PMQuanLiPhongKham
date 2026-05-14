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

import com.example.nhom08_quanlyphongkham.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;
import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.medical_join_diagnosis_join_prescription.MedicalRecordClinicalWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TabClinicalFragment extends Fragment {

    private final Map<String, LinearLayout> optionContainers = new HashMap<>();
    private final Map<String, View> arrows = new HashMap<>();
    private final Map<String, Boolean> expandedStates = new HashMap<>();
    private final Map<String, Integer> sectionColors = new HashMap<>();
    private final Map<String, Integer> selectedCounts = new HashMap<>();
    private final Map<String, TextView> badges = new HashMap<>();

    private ClinicalRepository clinicalRepository;

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

        bindSectionViews(view);
        bindSectionColors();
        initializeSectionCounts();
        attachSectionBadges();
        setupSectionClicks(view);
        setupPreviewButton(view);

        loadClinicalItems();
    }

    private void bindSectionViews(View view) {
        optionContainers.put("Xét nghiệm máu", view.findViewById(R.id.containerBloodOptions));
        optionContainers.put("Chẩn đoán hình ảnh", view.findViewById(R.id.containerImagingOptions));
        optionContainers.put("Siêu âm", view.findViewById(R.id.containerUltrasoundOptions));
        optionContainers.put("Nội soi", view.findViewById(R.id.containerEndoscopyOptions));
        optionContainers.put("Thính học", view.findViewById(R.id.containerAudiologyOptions));

        arrows.put("Xét nghiệm máu", view.findViewById(R.id.arrowBlood));
        arrows.put("Chẩn đoán hình ảnh", view.findViewById(R.id.arrowImaging));
        arrows.put("Siêu âm", view.findViewById(R.id.arrowUltrasound));
        arrows.put("Nội soi", view.findViewById(R.id.arrowEndoscopy));
        arrows.put("Thính học", view.findViewById(R.id.arrowAudiology));

        expandedStates.put("Xét nghiệm máu", false);
        expandedStates.put("Chẩn đoán hình ảnh", false);
        expandedStates.put("Siêu âm", false);
        expandedStates.put("Nội soi", false);
        expandedStates.put("Thính học", false);
    }

    private void bindSectionColors() {
        sectionColors.put("Xét nghiệm máu", 0xFFE74C78);
        sectionColors.put("Chẩn đoán hình ảnh", 0xFF3D7BE0);
        sectionColors.put("Siêu âm", 0xFF0F9FAF);
        sectionColors.put("Nội soi", 0xFF9B59B6);
        sectionColors.put("Thính học", 0xFFF39C12);
    }

    private void initializeSectionCounts() {
        for (String key : optionContainers.keySet()) {
            selectedCounts.put(key, 0);
        }
    }

    private void attachSectionBadges() {
        attachBadgeToHeader(R.id.headerBlood, R.id.arrowBlood, "Xét nghiệm máu", 0xFFE74C78);
        attachBadgeToHeader(R.id.headerImaging, R.id.arrowImaging, "Chẩn đoán hình ảnh", 0xFF3D7BE0);
        attachBadgeToHeader(R.id.headerUltrasound, R.id.arrowUltrasound, "Siêu âm", 0xFF0F9FAF);
        attachBadgeToHeader(R.id.headerEndoscopy, R.id.arrowEndoscopy, "Nội soi", 0xFF9B59B6);
        attachBadgeToHeader(R.id.headerAudiology, R.id.arrowAudiology, "Thính học", 0xFFF39C12);
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
        view.findViewById(R.id.headerBlood).setOnClickListener(v -> toggleSection("Xét nghiệm máu"));
        view.findViewById(R.id.headerImaging).setOnClickListener(v -> toggleSection("Chẩn đoán hình ảnh"));
        view.findViewById(R.id.headerUltrasound).setOnClickListener(v -> toggleSection("Siêu âm"));
        view.findViewById(R.id.headerEndoscopy).setOnClickListener(v -> toggleSection("Nội soi"));
        view.findViewById(R.id.headerAudiology).setOnClickListener(v -> toggleSection("Thính học"));
    }

    private void setupPreviewButton(View view) {
        View btnContinue = view.findViewById(R.id.btnContinueClinical);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Đang preview tab cận lâm sàng", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void toggleSection(String loaiDichVu) {
        LinearLayout container = optionContainers.get(loaiDichVu);
        View arrow = arrows.get(loaiDichVu);
        boolean isExpanded = Boolean.TRUE.equals(expandedStates.get(loaiDichVu));

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

        expandedStates.put(loaiDichVu, !isExpanded);
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
                    bindClinicalItems(response.body());
                } else {
                    Toast.makeText(requireContext(), "Không tải được dữ liệu cận lâm sàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ClinicalItem>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }

                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
            String loai = normalize(item.getLoai_dich_vu());

            for (String key : optionContainers.keySet()) {
                if (normalize(key).equals(loai)) {
                    LinearLayout container = optionContainers.get(key);
                    Integer accentColor = sectionColors.get(key);

                    if (container != null) {
                        container.addView(createClinicalOption(
                                key,
                                item,
                                accentColor != null ? accentColor : 0xFF64748B
                        ));
                    }
                    break;
                }
            }
        }
    }

    private View createClinicalOption(String sectionKey, ClinicalItem item, int accentColor) {
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

        row.addView(tickView);
        row.addView(textView);

        row.setOnClickListener(v -> {
            boolean isChecked = Boolean.TRUE.equals(tickView.getTag());

            if (isChecked) {
                tickView.setImageResource(R.drawable.bg_clinical_circle_unchecked);
                tickView.setTag(false);
                textView.setTextColor(0xFF334155);
                decreaseSectionCount(sectionKey);
            } else {
                tickView.setImageDrawable(createCheckedCircleDrawable(accentColor));
                tickView.setTag(true);
                textView.setTextColor(accentColor);
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

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
