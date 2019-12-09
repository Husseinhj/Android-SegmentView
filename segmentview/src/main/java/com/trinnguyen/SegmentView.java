package com.trinnguyen;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trinnguyen.segmentview.R;

import java.util.ArrayList;
import java.util.List;

public class SegmentView extends LinearLayout implements View.OnClickListener {

    private static final String LINE_TAG = "line";

    private int separateLineColor;
    private int unselectedTextColor;
    private int selectedTextColor;
    private int selectedBackgroundColor;
    private int numSegments;
    private int selectedIndex;
    private int textAppearanceId;

    private GradientDrawable backgroundDrawable;

    public SegmentView(Context context) {
        super(context);
        init(null, 0);
    }

    public SegmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SegmentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setOrientation(HORIZONTAL);

        // drawable
        backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setCornerRadius(dpToPx(8));
        setBackground(backgroundDrawable);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SegmentView, defStyle, 0);

        backgroundDrawable.setColor(a.getColor(R.styleable.SegmentView_unselectedBackgroundColor, Color.parseColor("#EDEDEE")));
        separateLineColor = a.getColor(R.styleable.SegmentView_separateLineColor, Color.parseColor("#D6D6D9"));
        unselectedTextColor = a.getColor(R.styleable.SegmentView_unselectedTextColor, Color.parseColor("#7F7F7F"));
        selectedTextColor = a.getColor(R.styleable.SegmentView_selectedTextColor, Color.WHITE);
        selectedBackgroundColor = a.getColor(R.styleable.SegmentView_selectedBackgroundColor, Color.parseColor("#005e80"));
        numSegments = a.getInteger(R.styleable.SegmentView_numSegments, 2);
        selectedIndex = a.getInteger(R.styleable.SegmentView_selectedIndex, 0);
        textAppearanceId = a.getResourceId(R.styleable.SegmentView_textAppearance, 0);

        a.recycle();

        // update view
        resetAllViews();
    }

    private void resetAllViews() {
        removeAllViews();
        addViews();
        updateTitles();
        updateSelectionState();
    }

    private void addViews() {
        for (int i = 0; i< numSegments; i++) {
            TextView textView = createTextView(i);
            textView.setOnClickListener(this);
            addView(textView);

            // add line
            if (i < numSegments - 1) {
                addView(createLine());
            }
        }
    }

    private TextView createTextView(int index) {
        TextView textView = new TextView(getContext());
        textView.setText("Item " + index);
        textView.setTextAppearance(getContext(), textAppearanceId);
        textView.setTag(index);
        textView.setGravity(Gravity.CENTER);

        // layout
        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.weight = 1;
        int margin = dpToPx(2);
        layoutParams.setMargins(margin, margin, margin, margin);
        textView.setLayoutParams(layoutParams);

        int padding = dpToPx(16);
        textView.setPadding(padding, 0, padding, 0);
        return textView;
    }

    private View createLine() {
        View line = new View(getContext());
        line.setBackgroundColor(separateLineColor);
        line.setTag(LINE_TAG);
        LinearLayout.LayoutParams lineLayout = new LayoutParams(dpToPx(1), ViewGroup.LayoutParams.MATCH_PARENT);
        int lineMargin = dpToPx(4);
        lineLayout.setMargins(0, lineMargin, 0, lineMargin);
        line.setLayoutParams(lineLayout);
        return line;
    }

    TransitionDrawable selectedDrawable = new TransitionDrawable(new Drawable[]{createUnselectedItemBackground(), createSelectedItemBackground()});

    TransitionDrawable unselectedDrawable = new TransitionDrawable(new Drawable[]{createSelectedItemBackground(), createUnselectedItemBackground()});

    private void updateSelectionState() {
        // color
        for (int i = 0; i< getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                // update background
                int index = (int)view.getTag();
                boolean isSelected = index == currentSelectedIndex();
                view.setBackground(isSelected ? createSelectedItemBackground() : createUnselectedItemBackground());

                view.setElevation(dpToPx(isSelected ? 2 : 0));
                ((TextView) view).setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
                ((TextView) view).setTypeface(((TextView) view).getTypeface(), isSelected ? Typeface.BOLD : Typeface.NORMAL);

                // lines
                if (i - 1 >= 0) {
                    View leftLine = getChildAt(i - 1);
                    leftLine.setVisibility(isSelected || (currentSelectedIndex() == index - 1) ? View.INVISIBLE : View.VISIBLE);
                }
                if (i + 1 < getChildCount()) {
                    View rightLine = getChildAt(i + 1);
                    rightLine.setVisibility(isSelected ? View.INVISIBLE : View.VISIBLE);
                }
            }
        }
    }

    private final int rippleColor = Color.parseColor("#1f000000");

    private Drawable createSelectedItemBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(selectedBackgroundColor);
        drawable.setCornerRadius(dpToPx(8));
        return drawable;
    }

    private Drawable createUnselectedItemBackground() {
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), null, null);
    }

    private void updateTitles() {
        for (int i = 0; i< getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                int index = (int)view.getTag();
                if (index < items.size()) {
                    ((TextView) view).setText(items.get(index));
                }
            }
        }
    }

    private List<String> items = new ArrayList<String>();

    public void setText(int index, String text) {
        if (index >= 0) {
            if (index < items.size()) {
                items.remove(index);
                items.add(index, text);
            } else {
                items.add(text);
            }

            // update
            updateTitles();
        }
    }

    public int getUnselectedTextColor() {
        return unselectedTextColor;
    }

    public void setUnselectedTextColor(int unselectedTextColor) {
        this.unselectedTextColor = unselectedTextColor;
        updateSelectionState();
    }

    public int getSelectedTextColor() {
        return selectedTextColor;
    }

    public void setSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
        updateSelectionState();
    }

    public void setUnselectedBackgroundColor(int unselectedBackgroundColor) {
        backgroundDrawable.setColor(unselectedBackgroundColor);
    }

    public int getSelectedBackgroundColor() {
        return selectedBackgroundColor;
    }

    public void setSelectedBackgroundColor(int selectedBackgroundColor) {
        this.selectedBackgroundColor = selectedBackgroundColor;
    }

    public int getNumSegments() {
        return numSegments;
    }

    public void setNumSegments(int numSegments) {
        if (this.numSegments != numSegments) {
            this.numSegments = numSegments;
            resetAllViews();
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        updateSelectionState();
    }

    public int getSeparateLineColor() {
        return separateLineColor;
    }

    public void setSeparateLineColor(int separateLineColor) {
        this.separateLineColor = separateLineColor;
        for (int i=0; i<getChildCount(); i++) {
            if (getChildAt(i).getTag() == LINE_TAG) {
                getChildAt(i).setBackgroundColor(separateLineColor);
            }
        }
    }

    private static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    private int currentSelectedIndex() {
        if (selectedIndex >= 0 && selectedIndex < numSegments) {
            return selectedIndex;
        }

        return 0;
    }

    @Override
    public void onClick(View v) {
        int index = (int)v.getTag();
        if (index != currentSelectedIndex()) {
            selectedIndex = index;
            updateSelectionState();
        }
    }
}