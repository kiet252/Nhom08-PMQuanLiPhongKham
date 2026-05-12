package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.nhom08_quanlyphongkham.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dashboard_fragment.doctor_examination_list.doctor_examination_form_detail.clinical_logic.ClinicalItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TabClinicalFragment extends Fragment {

    private final Map<String, LinearLayout> optionContainers = new HashMap<>();
    private final Map<String, View> arrows = new HashMap<>();
    private final Map<String, Boolean> expandedStates = new HashMap<>();
    private final Map<String, Integer> sectionColors = new HashMap<>();

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

        for (ClinicalItem item : items) {
            String loai = normalize(item.getLoai_dich_vu());

            for (String key : optionContainers.keySet()) {
                if (normalize(key).equals(loai)) {
                    LinearLayout container = optionContainers.get(key);
                    Integer accentColor = sectionColors.get(key);

                    if (container != null) {
                        container.addView(createClinicalOption(
                                item,
                                accentColor != null ? accentColor : 0xFF64748B
                        ));
                    }
                    break;
                }
            }
        }
    }

    private View createClinicalOption(ClinicalItem item, int accentColor) {
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

        android.widget.TextView textView = new android.widget.TextView(requireContext());
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
            } else {
                tickView.setImageDrawable(createCheckedCircleDrawable(accentColor));
                tickView.setTag(true);
                textView.setTextColor(accentColor);
            }
        });

        return row;
    }

    private GradientDrawable createOptionBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(0xFFF8FAFC);
        drawable.setCornerRadius(dp(12));
        drawable.setStroke(dp(1), 0xFFD8DEE9);
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
