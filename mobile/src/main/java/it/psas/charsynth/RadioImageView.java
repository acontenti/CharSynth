package it.psas.charsynth;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.ImageButton;

/**
 * Created by Alessandro Contenti on 03/09/2015.
 * Copyright © 1996, 2015 Alessandro Contenti. All Rights Reserved.
 */

public class RadioImageView extends ImageButton {
	private static final long ANIMATION_DURATION = 250;
	private static final Interpolator ANIMATION_INTERPOLATOR = new AccelerateDecelerateInterpolator();
	private Drawable mSrcOnDrawable;
	private Drawable mSrcOffDrawable;
	private OnToggleListener onToggleListener;
	private boolean checked = false;
	private int mPrimaryColor = Color.parseColor("#009688");
	private int mBackgroundColor = Color.WHITE;
	Animation anim_out = new AlphaAnimation(1.0f, 0.0f);
	Animation anim_in  = new AlphaAnimation(0.0f, 1.0f);

	public RadioImageView(Context context) {
		super(context);
		init(null, 0);
	}

	public RadioImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public RadioImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RadioImageView, defStyle, 0);
		mPrimaryColor = a.getColor(R.styleable.RadioImageView_primaryColor, mPrimaryColor);
		mBackgroundColor = a.getColor(R.styleable.RadioImageView_backgroundColor, mBackgroundColor);
		if (a.hasValue(R.styleable.RadioImageView_srcOn) && a.hasValue(R.styleable.RadioImageView_srcOff)) {
			mSrcOnDrawable = a.getDrawable(R.styleable.RadioImageView_srcOn);
			mSrcOffDrawable = a.getDrawable(R.styleable.RadioImageView_srcOff);
		}
		else throw new RuntimeException("srcOn and srcOff attributes not specified!");
		a.recycle();
		anim_in.setDuration(ANIMATION_DURATION);
		anim_in.setInterpolator(ANIMATION_INTERPOLATOR);
		anim_out.setDuration(ANIMATION_DURATION);
		anim_out.setInterpolator(ANIMATION_INTERPOLATOR);
		super.setBackgroundColor(mBackgroundColor);
		setImageDrawable(mSrcOffDrawable);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		throw new RuntimeException();
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			checked = true;
			setBackgroundColor(mPrimaryColor);
			setImageDrawable(mSrcOnDrawable);
			if (onToggleListener != null) onToggleListener.onToggle(RadioImageView.this, checked);
		}
	};

	public void setOnToggleListener (OnToggleListener listener) {
		onToggleListener = listener;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		setBackgroundColor(checked ? mPrimaryColor : mBackgroundColor);
		setImageDrawable(checked ? mSrcOnDrawable : mSrcOffDrawable);
		if (onToggleListener != null) onToggleListener.onToggle(this, checked);
	}

	public boolean isChecked() {
		return checked;
	}

	@Override
	public void setBackgroundColor(int color) {
		ValueAnimator backgroundColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), ((ColorDrawable) getBackground()).getColor(), color);
		backgroundColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				RadioImageView.super.setBackgroundColor((Integer) animator.getAnimatedValue());
			}
		});
		backgroundColorAnimator.setDuration(ANIMATION_DURATION);
		backgroundColorAnimator.setInterpolator(ANIMATION_INTERPOLATOR);
		backgroundColorAnimator.start();
	}

	public interface OnToggleListener {
		void onToggle (RadioImageView view, boolean state);
	}

	@Override
	public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP) {
			if(onClickListener != null) onClickListener.onClick(this);
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_UP && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
			if(onClickListener != null) onClickListener.onClick(this);
		}
		return super.dispatchKeyEvent(event);
	}
}
