package com.gromaudio.simplifiedmediaplayer.ui.customElements;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.gromaudio.simplifiedmediaplayer.R;


public class CustomImageButton extends ImageButton implements View.OnClickListener {

	private int mIsChecked = 0;
	private OnClickListener mClickListener = null;

	private int mColorSelected = 0;
	private int mColorFilterChecked = 0;
	private int mColorFilterChecked2 = 0;

	private boolean mIsChangeColor = false;
	private boolean mIsOutRectTouch = false;


	private long mStartTime;
	private int mRepeatCount;
	private RepeatListener mRepeatListener;
	private long mInterval = 500;


	interface RepeatListener {
		/**
		 * This method will be called repeatedly at roughly the interval
		 * specified in setRepeatListener(), for as long as the button
		 * is pressed.
		 * @param v The button as a View.
		 * @param duration The number of milliseconds the button has been pressed so far.
		 * @param repeatcount The number of previous calls in this sequence.
		 * If this is going to be the last call in this sequence (i.e. the user
		 * just stopped pressing the button), the value will be -1.
		 */
		void onRepeat(View v, long duration, int repeatcount);
	}


	public CustomImageButton(Context context) {
		super(context);

		initView();
	}

	public CustomImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView();
	}

	public CustomImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		initView();
	}

	private void initView() {
		super.setOnClickListener(this);
		mColorSelected = getResources().getColor(R.color.button_pressed);
		mColorFilterChecked = getResources().getColor(R.color.button_pressed);
        mColorFilterChecked2 = getResources().getColor(R.color.button_pressed2);

		if (mIsChangeColor) {
			setColorStyle();
		}

		setFocusable(true);
		setLongClickable(false);
	}

	private void setColorStyle() {
		if (mIsChecked == 1) {
			getDrawable().setColorFilter(mColorFilterChecked, PorterDuff.Mode.MULTIPLY);
		} else if (mIsChecked == 2) {
            getDrawable().setColorFilter(mColorFilterChecked2, PorterDuff.Mode.MULTIPLY);
        }
        else {
			getDrawable().setColorFilter(null);
		}
	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {

		int maskedAction = event.getActionMasked();
		Rect outRect = new Rect();
		this.getGlobalVisibleRect(outRect);
		if (outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
			if (!isLongClickable()) {
				if (maskedAction == MotionEvent.ACTION_DOWN) {
					mIsOutRectTouch = false;
                    //mIsChecked = mIsChecked==1 ? 0 : 1;
					if (mIsChangeColor) {
						setColorStyle();
					} else {
						//getDrawable().setColorFilter(mColorSelected, PorterDuff.Mode.MULTIPLY);
					}
				} else if (maskedAction == MotionEvent.ACTION_UP && mIsChangeColor) {
					setColorStyle();
				}
			}

		} else {
			if (!mIsOutRectTouch) {
				//mIsChecked = mIsChecked==1 ? 0 : 1;
				if (mIsChangeColor) {
					setColorStyle();
				} else {
					//getDrawable().setColorFilter(null);
				}
				mIsOutRectTouch = true;
			}
		}

		if (!mIsChangeColor && maskedAction == MotionEvent.ACTION_UP) {
			//getDrawable().setColorFilter(null);
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			// remove the repeater, but call the hook one more time
			removeCallbacks(mRepeater);
			if (mStartTime != 0) {
				doRepeat(true);
				mStartTime = 0;
			}
		}

		return super.onTouchEvent(event);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		mClickListener = l;
	}

	@Override
	public void onClick(View v) {
		if (mClickListener != null) {
			mClickListener.onClick(v);
		}
	}

	public void setChangeColorFilter(boolean change) {
		mIsChangeColor = change;
	}

	public boolean isChecked() {
		return mIsChecked==1;
	}

	public void setChecked(int isChecked) {
		mIsChecked = isChecked;
		if (mIsChangeColor) {
			setColorStyle();
		}
	}

    @Override
    public void setSelected(boolean isSelected){
        setChecked(isSelected ? 1 : 0);
    }

	/**
	 * Sets the listener to be called while the button is pressed and
	 * the interval in milliseconds with which it will be called.
	 * @param l The listener that will be called
	 * @param interval The interval in milliseconds for calls
	 */
	public void setRepeatListener(RepeatListener l, long interval) {
		setLongClickable(true);
		mRepeatListener = l;
		mInterval = interval;
	}

	@Override
	public boolean performLongClick() {
		if (mRepeatListener != null) {
			mStartTime = SystemClock.elapsedRealtime();
			mRepeatCount = 0;
			post(mRepeater);
			return true;
		} else {
			return super.performLongClick();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				// need to call super to make long press work, but return
				// true so that the application doesn't get the down event.
				super.onKeyDown(keyCode, event);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				// remove the repeater, but call the hook one more time
				removeCallbacks(mRepeater);
				if (mStartTime != 0) {
					doRepeat(true);
					mStartTime = 0;
				}
		}
		return super.onKeyUp(keyCode, event);
	}

	private Runnable mRepeater = new Runnable() {
		public void run() {
			doRepeat(false);
			if (isPressed()) {
				postDelayed(this, mInterval);
			}
		}
	};

	private  void doRepeat(boolean last) {
		long now = SystemClock.elapsedRealtime();
		if (mRepeatListener != null) {
			mRepeatListener.onRepeat(this, now - mStartTime, last ? -1 : mRepeatCount++);
		}
	}

	public void setColorSelected(int colorSelected) {
		mColorSelected = colorSelected;
		mColorFilterChecked = colorSelected;
        mColorFilterChecked2 = colorSelected;
	}

	@Override
	public Drawable getDrawable() {
		final Drawable drawable = super.getDrawable();
		drawable.mutate();
		return drawable;
	}
}
