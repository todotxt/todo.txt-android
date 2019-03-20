/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 * <p>
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 * <p>
 * LICENSE:
 * <p>
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * <p>
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 * <p>
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

public class TodoLocationPreference extends DialogPreference {
    final static String TAG = TodoLocationPreference.class.getSimpleName();

    private String mInitialPath;
    private TodoLocationPreferenceFragment.DisplayMode mDisplayMode;
    private String mCurrentSelection;

    @Override
    public CharSequence getPositiveButtonText() {
        return getContext().getResources().getString(R.string.ok);
    }
    @Override
    public CharSequence getNegativeButtonText() {
        return getContext().getResources().getString(R.string.cancel);
    }

    public TodoLocationPreference(Context context) {
        this(context, null);
    }

    public TodoLocationPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TodoLocationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TodoLocationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String getInitialPath() { return  mInitialPath; }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (mInitialPath == null) {
            mInitialPath = restoreValue ? getPersistedString(null)
                    : (String) defaultValue;
        }
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.todo_location_dialog;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final SavedState myState = new SavedState(superState);
        myState.displayMode = mDisplayMode.name();

        if (mCurrentSelection != null) {
            myState.initialPath = mCurrentSelection;
            // FIXME: need to save the entire tree.
        } else {
            myState.initialPath = mInitialPath;
        }

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
        mDisplayMode = TodoLocationPreferenceFragment.DisplayMode.valueOf(myState.displayMode);
        mInitialPath = myState.initialPath;
        super.onRestoreInstanceState(myState.getSuperState());
    }

    public void setCurrentSelection(String mCurrentSelection) {
        this.mCurrentSelection = mCurrentSelection;
        persistString(mCurrentSelection);
    }

    public String  getCurrentSelection() {
        return mCurrentSelection;
    }


    private static class SavedState extends Preference.BaseSavedState {
        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String displayMode;
        String initialPath;

        public SavedState(Parcel source) {
            super(source);
            displayMode = source.readString();
            initialPath = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(displayMode);
            dest.writeString(initialPath);
        }
    }
}