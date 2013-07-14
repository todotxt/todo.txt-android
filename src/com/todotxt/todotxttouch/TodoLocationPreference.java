package com.todotxt.todotxttouch;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.EditTextPreference;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class TodoLocationPreference extends EditTextPreference {

	private boolean mWarningMode = false;
	private boolean mDisplayWarning = false;

	public TodoLocationPreference(Context context) {
		super(context);
	}

	public TodoLocationPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TodoLocationPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean shouldDisplayWarning() {
		return mDisplayWarning;
	}

	public void setDisplayWarning(boolean shouldDisplay) {
		mDisplayWarning = shouldDisplay;
	}

	private CharSequence getWarningMessage() {
		SpannableString ss = new SpannableString(getContext().getString(R.string.todo_path_warning));
		ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		// Display the warning message if necessary.
		// Otherwise, just use the default layout.
		EditText view = this.getEditText();
		if (mWarningMode) {
			view.setEnabled(false);
			view.setVisibility(View.GONE);
			builder.setMessage(getWarningMessage());
			builder.setPositiveButton(R.string.todo_path_warning_override, this);
		} else {
			view.setVisibility(View.VISIBLE);
			view.setEnabled(true);
		}
	}

	protected boolean needInputMethod() {
		// We want the input method to show, if possible, when edit dialog is
		// displayed, but not when warning message is displayed
		return !mWarningMode;
	}

	@Override
	protected void onClick() {
		// Called when the preference is clicked
		// This method displays the dialog.
		// When mDisplayWarning is set, we want to display
		// a warning message instead of the actual dialog
		mWarningMode = mDisplayWarning;
		super.onClick();
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// If we are displaying the warning message and the user
		// clicked "I'm feeling dangerous", then redisplay the
		// dialog with the default layout
		if (mWarningMode && positiveResult) {
			mWarningMode = false;
			showDialog(null);
			return;
		}

		// If we are already displaying the default layout
		// do the default processing (either persist the change
		// or cancel, depending on which button was pressed)
		super.onDialogClosed(positiveResult);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();

		final SavedState myState = new SavedState(superState);
		myState.warningMode = mWarningMode;
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		mWarningMode = myState.warningMode;
		super.onRestoreInstanceState(myState.getSuperState());
	}

	private static class SavedState extends BaseSavedState {
		boolean warningMode;

		public SavedState(Parcel source) {
			super(source);
			boolean[] array = new boolean[1];
			source.readBooleanArray(array);
			warningMode = array[0];
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeBooleanArray(new boolean[]{warningMode});
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}
