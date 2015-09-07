package it.psas.charsynth;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

/**
 * Created by Alessandro Contenti on 03/09/2015.
 * Copyright © 1996, 2015 Alessandro Contenti. All Rights Reserved.
 */
public class RadioBar extends RadioGroup {
	
	// holds the checked id; the selection is empty by default
	private int mCheckedId = -1;
	// tracks children radio buttons checked state
	private RadioImageView.OnToggleListener mChildOnCheckedChangeListener;
	// when true, mOnCheckedChangeListener discards events
	private boolean mProtectFromCheckedChange = false;
	private OnSelectedItemChangeListener mOnCheckedChangeListener;
	private PassThroughHierarchyChangeListener mPassThroughListener;

	/**
	 * {@inheritDoc}
	 */
	public RadioBar(Context context) {
		super(context);
		setOrientation(VERTICAL);
		init(null, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public RadioBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	private void init(AttributeSet attrs, int defStyle) {
		// retrieve selected radio button as requested by the user in the
		// XML layout file
		TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.RadioBar, defStyle, 0);
		int value = attributes.getResourceId(R.styleable.RadioBar_selectedItem, View.NO_ID);
		if (value != View.NO_ID) {
			mCheckedId = value;
		}
		int index = attributes.getInt(R.styleable.RadioBar_orientation, VERTICAL);
		setOrientation(index == 0 ? HORIZONTAL : VERTICAL);
		attributes.recycle();

		mChildOnCheckedChangeListener = new CheckedStateTracker();
		mPassThroughListener = new PassThroughHierarchyChangeListener();
		super.setOnHierarchyChangeListener(mPassThroughListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
		// the user listener is delegated to our pass-through listener
		mPassThroughListener.mOnHierarchyChangeListener = listener;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// checks the appropriate radio button as requested in the XML file
		if (mCheckedId != -1) {
			mProtectFromCheckedChange = true;
			setCheckedStateForView(mCheckedId, true);
			mProtectFromCheckedChange = false;
			setCheckedId(mCheckedId);
		}
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (child instanceof RadioImageView) {
			final RadioImageView button = (RadioImageView) child;
			if (button.isChecked()) {
				mProtectFromCheckedChange = true;
				if (mCheckedId != -1) {
					setCheckedStateForView(mCheckedId, false);
				}
				mProtectFromCheckedChange = false;
				setCheckedId(button.getId());
			}
		}

		super.addView(child, index, params);
	}

	/**
	 * <p>Sets the selection to the radio button whose identifier is passed in
	 * parameter. Using -1 as the selection identifier clears the selection;
	 * such an operation is equivalent to invoking {@link #clearCheck()}.</p>
	 *
	 * @param id the unique id of the radio button to select in this group
	 *
	 * @see #getCheckedRadioImageViewId()
	 * @see #clearCheck()
	 */
	public void check(int id) {
		// don't even bother
		if (id != -1 && (id == mCheckedId)) {
			return;
		}

		if (mCheckedId != -1) {
			setCheckedStateForView(mCheckedId, false);
		}

		if (id != -1) {
			setCheckedStateForView(id, true);
		}

		setCheckedId(id);
	}


	public void checkAtPosition(int position) {
		check(getChildAt(position).getId());
	}

	private void setCheckedId(int id) {
		mCheckedId = id;
		if (mOnCheckedChangeListener != null) {
			mOnCheckedChangeListener.onSelectedItemChange(this, mCheckedId, indexOfChild(findViewById(id)));
		}
	}

	private void setCheckedStateForView(int viewId, boolean checked) {
		View checkedView = findViewById(viewId);
		if (checkedView != null && checkedView instanceof RadioImageView) {
			((RadioImageView) checkedView).setChecked(checked);
		}
	}

	/**
	 * <p>Returns the identifier of the selected radio button in this group.
	 * Upon empty selection, the returned value is -1.</p>
	 *
	 * @return the unique id of the selected radio button in this group
	 *
	 * @see #check(int)
	 * @see #clearCheck()
	 *
	 * @attr ref android.R.styleable#RadioGroup_checkedButton
	 */
	public int getCheckedRadioImageViewId() {
		return mCheckedId;
	}

	/**
	 * <p>Clears the selection. When the selection is cleared, no radio button
	 * in this group is selected and {@link #getCheckedRadioImageViewId()} returns
	 * null.</p>
	 *
	 * @see #check(int)
	 * @see #getCheckedRadioImageViewId()
	 */
	public void clearCheck() {
		check(-1);
	}

	/**
	 * <p>Register a callback to be invoked when the checked radio button
	 * changes in this group.</p>
	 *
	 * @param listener the callback to call on checked state change
	 */
	public void setOnSelectedItemChangeListener(OnSelectedItemChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}

	@Deprecated
	@Override
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) throws RuntimeException{
		throw new RuntimeException("Use setOnSelectedItemChangeListener instead!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new RadioGroup.LayoutParams(getContext(), attrs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof RadioGroup.LayoutParams;
	}

	@Override
	protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(RadioBar.class.getName());
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(RadioBar.class.getName());
	}

	/**
	 * <p>Interface definition for a callback to be invoked when the checked
	 * radio button changed in this group.</p>
	 */
	public interface OnSelectedItemChangeListener {
		/**
		 * <p>Called when the checked radio button has changed. When the
		 * selection is cleared, checkedId is -1.</p>
		 *
		 * @param radioBar the group in which the checked radio button has changed
		 * @param checkedId the unique identifier of the newly checked radio button
		 */
		void onSelectedItemChange(RadioBar radioBar, int checkedId, int position);
	}

	private class CheckedStateTracker implements RadioImageView.OnToggleListener {
		@Override
		public void onToggle(RadioImageView view, boolean state) {
			// prevents from infinite recursion
			if (mProtectFromCheckedChange) {
				return;
			}

			mProtectFromCheckedChange = true;
			if (mCheckedId != -1) {
				setCheckedStateForView(mCheckedId, false);
			}
			mProtectFromCheckedChange = false;

			int id = view.getId();
			setCheckedId(id);
		}
	}

	/**
	 * <p>A pass-through listener acts upon the events and dispatches them
	 * to another listener. This allows the table layout to set its own internal
	 * hierarchy change listener without preventing the user to setup his.</p>
	 */
	private class PassThroughHierarchyChangeListener implements
			ViewGroup.OnHierarchyChangeListener {
		private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;

		/**
		 * {@inheritDoc}
		 */
		public void onChildViewAdded(View parent, View child) {
			if (parent == RadioBar.this && child instanceof RadioImageView) {
				int id = child.getId();
				// generates an id if it's missing
				if (id == View.NO_ID) {
					id = child.hashCode();
					child.setId(id);
				}
				((RadioImageView) child).setOnToggleListener(
						mChildOnCheckedChangeListener);
			}

			if (mOnHierarchyChangeListener != null) {
				mOnHierarchyChangeListener.onChildViewAdded(parent, child);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void onChildViewRemoved(View parent, View child) {
			if (parent == RadioBar.this && child instanceof RadioImageView) {
				((RadioImageView) child).setOnToggleListener(null);
			}

			if (mOnHierarchyChangeListener != null) {
				mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
			}
		}
	}
}
