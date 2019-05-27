package org.inventivetalent.trashapp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

// https://stackoverflow.com/a/28504193/6257838
public class CustomViewPager extends ViewPager {

	private boolean swipeable;

	public CustomViewPager(@NonNull Context context) {
		super(context);
		swipeable = true;
	}

	public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		swipeable = true;
	}

	@Override
	public void setCurrentItem(int item) {
		super.setCurrentItem(item);
		Log.i("CustomViewPager", "setCurrentItem(" + item + ")");

		if (item == 0) {
			this.swipeable = true;
		} else {
			this.swipeable = false;
		}
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return swipeable && super.onTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return swipeable && super.onInterceptTouchEvent(ev);
	}

	public boolean isSwipeable() {
		return swipeable;
	}

	public void setSwipeable(boolean swipeable) {
		this.swipeable = swipeable;
	}
}
