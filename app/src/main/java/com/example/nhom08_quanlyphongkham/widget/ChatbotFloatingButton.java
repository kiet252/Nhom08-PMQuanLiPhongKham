package com.example.nhom08_quanlyphongkham.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nhom08_quanlyphongkham.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ChatbotFloatingButton extends FrameLayout {

    private final View ring1;
    private final View ring2;
    private final View ring3;

    private final List<ObjectAnimator> runningAnimators = new ArrayList<>();

    private float downRawX;
    private float downRawY;
    private float startX;
    private float startY;
    private boolean dragging = false;

    private final int touchSlop;

    public ChatbotFloatingButton(@NonNull Context context) {
        this(context, null);
    }

    public ChatbotFloatingButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatbotFloatingButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        setClickable(true);
        setFocusable(true);
        setClipChildren(false);
        setClipToPadding(false);

        ring1 = buildRing(context);
        ring2 = buildRing(context);
        ring3 = buildRing(context);

        addView(ring1, ringLayout(dp(84)));
        addView(ring2, ringLayout(dp(84)));
        addView(ring3, ringLayout(dp(84)));

        MaterialCardView bubble = new MaterialCardView(context);
        bubble.setCardBackgroundColor(Color.parseColor("#0D5FA8"));
        bubble.setRadius(dp(28));
        bubble.setCardElevation(dp(8));
        bubble.setUseCompatPadding(false);

        LayoutParams bubbleLp = new LayoutParams(dp(56), dp(56), Gravity.CENTER);
        addView(bubble, bubbleLp);

        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.ic_chatbot_wrapper);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        bubble.addView(icon, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startPulseAnimations();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopPulseAnimations();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downRawX = event.getRawX();
                downRawY = event.getRawY();
                startX = getX();
                startY = getY();
                dragging = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - downRawX;
                float dy = event.getRawY() - downRawY;

                if (!dragging && (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop)) {
                    dragging = true;
                }

                if (dragging) {
                    moveWithinParent(startX + dx, startY + dy);
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (!dragging) {
                    performClick();
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void moveWithinParent(float targetX, float targetY) {
        View parent = (View) getParent();
        if (parent == null) {
            setX(targetX);
            setY(targetY);
            return;
        }

        float maxX = parent.getWidth() - getWidth();
        float maxY = parent.getHeight() - getHeight();

        float clampedX = Math.max(0f, Math.min(targetX, maxX));
        float clampedY = Math.max(0f, Math.min(targetY, maxY));

        setX(clampedX);
        setY(clampedY);
    }

    private void startPulseAnimations() {
        if (!runningAnimators.isEmpty()) return;

        animateRing(ring1, 0L);
        animateRing(ring2, 450L);
        animateRing(ring3, 900L);
    }

    private void stopPulseAnimations() {
        for (ObjectAnimator animator : runningAnimators) {
            animator.cancel();
        }
        runningAnimators.clear();
    }

    private void animateRing(View ring, long delay) {
        ring.setAlpha(0f);
        ring.setScaleX(1f);
        ring.setScaleY(1f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ring, View.SCALE_X, 1f, 1.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ring, View.SCALE_Y, 1f, 1.8f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(ring, View.ALPHA, 0.45f, 0f);

        scaleX.setDuration(1800);
        scaleY.setDuration(1800);
        alpha.setDuration(1800);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);

        scaleX.setRepeatMode(ValueAnimator.RESTART);
        scaleY.setRepeatMode(ValueAnimator.RESTART);
        alpha.setRepeatMode(ValueAnimator.RESTART);

        scaleX.setStartDelay(delay);
        scaleY.setStartDelay(delay);
        alpha.setStartDelay(delay);

        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        alpha.setInterpolator(new DecelerateInterpolator());

        runningAnimators.add(scaleX);
        runningAnimators.add(scaleY);
        runningAnimators.add(alpha);

        scaleX.start();
        scaleY.start();
        alpha.start();
    }

    private View buildRing(Context context) {
        View ring = new View(context);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(dp(2), Color.parseColor("#66B7E8"));

        ring.setBackground(drawable);
        ring.setAlpha(0f);
        return ring;
    }

    private LayoutParams ringLayout(int sizePx) {
        return new LayoutParams(sizePx, sizePx, Gravity.CENTER);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}