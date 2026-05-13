package dashboard_fragment.doctor_examination_list.doctor_examination_form_detail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nhom08_quanlyphongkham.R;

import java.util.Locale;

final class DoctorExaminationStatusStyle {
    private DoctorExaminationStatusStyle() {
    }

    static void applyBadgeStyle(TextView badgeView, String rawStatus) {
        if (badgeView == null) {
            return;
        }

        StatusPalette palette = resolve(rawStatus);
        badgeView.setText(String.format(Locale.getDefault(), "• %s", palette.status.getDisplayName()));
        badgeView.setTextColor(palette.accentColor);
        badgeView.setBackground(createRoundedBackground(badgeView.getContext(), 18, palette.backgroundColor, 0));
    }

    static void applyStatusSectionStyle(View rootView, String rawStatus) {
        if (rootView == null) {
            return;
        }

        LinearLayout statusLayout = rootView.findViewById(R.id.layoutDoctorExFormPatientInfoStatus);
        if (statusLayout == null) {
            return;
        }

        StatusPalette selectedPalette = resolve(rawStatus);
        statusLayout.setBackgroundColor(Color.TRANSPARENT);

        styleStatusButton(statusLayout.findViewById(R.id.btnStatusWaiting),
                selectedPalette.status == DoctorExaminationStatus.WAITING,
                resolve(DoctorExaminationStatus.WAITING.getDisplayName()),
                resolve(DoctorExaminationStatus.WAITING.getDisplayName()).accentColor,
                R.drawable.ic_status_waiting_doctor_ex_form_list);
        styleStatusButton(statusLayout.findViewById(R.id.btnStatusInProgress),
                selectedPalette.status == DoctorExaminationStatus.IN_PROGRESS,
                resolve(DoctorExaminationStatus.IN_PROGRESS.getDisplayName()),
                resolve(DoctorExaminationStatus.IN_PROGRESS.getDisplayName()).accentColor,
                R.drawable.ic_status_examining_doctor_ex_form_list);
        styleStatusButton(statusLayout.findViewById(R.id.btnStatusDone),
                selectedPalette.status == DoctorExaminationStatus.DONE,
                resolve(DoctorExaminationStatus.DONE.getDisplayName()),
                resolve(DoctorExaminationStatus.DONE.getDisplayName()).accentColor,
                R.drawable.ic_status_examined_doctor_ex_form_list);
    }

    private static StatusPalette resolve(String rawStatus) {
        DoctorExaminationStatus status = DoctorExaminationStatus.fromValue(rawStatus);
        switch (status) {
            case DONE:
                return new StatusPalette(status, Color.parseColor("#2E9E5B"));
            case IN_PROGRESS:
                return new StatusPalette(status, Color.parseColor("#1E88E5"));
            case WAITING:
            default:
                return new StatusPalette(status, Color.parseColor("#E67E22"));
        }
    }

    private static void styleStatusButton(LinearLayout button, boolean isSelected, StatusPalette palette,
                                          int defaultAccentColor, int defaultIconResId) {
        if (button == null) {
            return;
        }

        button.setBackground(isSelected
                ? createRoundedBackground(button.getContext(), 14, palette.accentColor, palette.accentColor)
                : button.getContext().getDrawable(R.drawable.bg_doctor_detail_status_btn_default));

        for (int index = 0; index < button.getChildCount(); index++) {
            View child = button.getChildAt(index);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(isSelected ? Color.WHITE : defaultAccentColor);
            } else if (child instanceof ImageView) {
                ImageView imageView = (ImageView) child;
                imageView.clearColorFilter();
                imageView.setImageDrawable(isSelected
                        ? createSelectedStatusIcon(palette.accentColor)
                        : button.getContext().getDrawable(defaultIconResId));
            } else if (child instanceof ViewGroup) {
                styleNestedChildren((ViewGroup) child, isSelected, defaultAccentColor, defaultIconResId, palette.accentColor);
            }
        }
    }

    private static void styleNestedChildren(ViewGroup parent, boolean isSelected, int defaultAccentColor,
                                            int defaultIconResId, int selectedAccentColor) {
        for (int index = 0; index < parent.getChildCount(); index++) {
            View child = parent.getChildAt(index);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(isSelected ? Color.WHITE : defaultAccentColor);
            } else if (child instanceof ImageView) {
                ImageView imageView = (ImageView) child;
                imageView.clearColorFilter();
                imageView.setImageDrawable(isSelected
                        ? createSelectedStatusIcon(selectedAccentColor)
                        : parent.getContext().getDrawable(defaultIconResId));
            }
        }
    }

    private static Drawable createSelectedStatusIcon(int accentColor) {
        GradientDrawable outerCircle = new GradientDrawable();
        outerCircle.setShape(GradientDrawable.OVAL);
        outerCircle.setColor(Color.WHITE);

        GradientDrawable innerCircle = new GradientDrawable();
        innerCircle.setShape(GradientDrawable.OVAL);
        innerCircle.setColor(accentColor);
        innerCircle.setSize(12, 12);

        LayerDrawable drawable = new LayerDrawable(new Drawable[]{outerCircle, innerCircle});
        drawable.setLayerInset(1, 14, 14, 14, 14);
        return drawable;
    }

    private static GradientDrawable createRoundedBackground(Context context, int radiusDp, int fillColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(context, radiusDp));
        drawable.setColor(fillColor);
        if (strokeColor != 0) {
            drawable.setStroke(dp(context, 1), strokeColor);
        }
        return drawable;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static final class StatusPalette {
        final DoctorExaminationStatus status;
        final int accentColor;
        final int backgroundColor;

        StatusPalette(DoctorExaminationStatus status, int accentColor) {
            this.status = status;
            this.accentColor = accentColor;
            this.backgroundColor = blendWithWhite(accentColor, 0.86f);
        }

        private static int blendWithWhite(int color, float ratio) {
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);

            int mixedRed = Math.round(red + (255 - red) * ratio);
            int mixedGreen = Math.round(green + (255 - green) * ratio);
            int mixedBlue = Math.round(blue + (255 - blue) * ratio);
            return Color.rgb(mixedRed, mixedGreen, mixedBlue);
        }
    }
}
