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

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.todotxt.todotxttouch.remote.RemoteFolder;
import com.todotxt.todotxttouch.util.Tree;

import java.io.File;
import java.util.List;

public class TodoLocationPreference extends DialogPreference {
    final static String TAG = TodoLocationPreference.class.getSimpleName();
    private TodoApplication mApp;
    private DisplayMode mDisplayMode = DisplayMode.NORMAL;
    private boolean mDisplayWarning = false;
    private ArrayAdapter<String> mAdapter;
    private String mInitialPath;
    private Tree<RemoteFolder> mRootFolder;
    private Tree<RemoteFolder> mCurrentSelection;
    private ListView mListView;
    private View mEmptyView;
    private View mListFrame;
    private EditText mEditText;
    private TextView mCurrentFolderTextView;
    public TodoLocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.todo_location_dialog);
    }

    public boolean shouldDisplayWarning() {
        return mDisplayWarning;
    }

    public void setDisplayWarning(boolean shouldDisplay) {
        mDisplayWarning = shouldDisplay;
    }

    public void setApplication(TodoApplication app) {
        mApp = app;
    }

    private CharSequence getWarningMessage() {
        SpannableString ss = new SpannableString(getContext().getString(
                R.string.todo_path_warning));
        ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    @Override
    protected void onClick() {
        // Called when the preference is clicked
        // This method displays the dialog.
        // When mDisplayWarning is set, we want to display
        // a warning message instead of the actual dialog
        mDisplayMode = mDisplayWarning ? DisplayMode.WARNING : DisplayMode.NORMAL;

        super.onClick();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // If we are displaying the warning message and the user
        // clicked "I'm feeling dangerous", then redisplay the
        // dialog with the default layout
        if (mDisplayMode == DisplayMode.WARNING && positiveResult) {
            mDisplayMode = DisplayMode.NORMAL;
            showDialog(null);

            return;
        }

        if (mCurrentSelection != null && positiveResult) {
            String value = mCurrentSelection.getData().getPath();
            if (mDisplayMode == DisplayMode.ADD_NEW) {
                value = new File(value, mEditText.getText().toString())
                        .toString();
                addNew(value);
            }

            if (callChangeListener(value)) {
                persistString(value);
            }
        }

        mInitialPath = null;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (mInitialPath == null) {
            mInitialPath = restoreValue ? getPersistedString(null)
                    : (String) defaultValue;
        }
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        // Display the warning message if necessary.
        // Otherwise, just use the default layout.
        if (mDisplayMode == DisplayMode.WARNING) {
            builder.setMessage(getWarningMessage());
            builder.setPositiveButton(R.string.todo_path_warning_override, this);
        } else {
            // nothing to do here...
        }
    }

    @Override
    protected View onCreateDialogView() {
        if (mDisplayMode == DisplayMode.WARNING) {
            return null;
        }

        return super.onCreateDialogView();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mCurrentFolderTextView = (TextView) view.findViewById(R.id.folder_name);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.empty);
        mListFrame = view.findViewById(R.id.list_frame);
        mEditText = (EditText) view.findViewById(R.id.add_new);

        if (mDisplayMode == DisplayMode.ADD_NEW) {
            mEditText.setVisibility(View.VISIBLE);
            mListFrame.setVisibility(View.GONE);
        } else {
            mEditText.setVisibility(View.GONE);
            mListFrame.setVisibility(View.VISIBLE);
        }

        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mEmptyView);

        // initialize the view
        initFolderTree();
        selectFolder(mCurrentSelection);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (position == 0 && mCurrentSelection.getData().hasParent()) {
                    // go back up to previous directory
                    upToParent();
                } else if (position == mAdapter.getCount() - 1) {
                    // signal that AddNew was clicked
                    mDisplayMode = DisplayMode.ADD_NEW;
                    mEditText.setVisibility(View.VISIBLE);
                    mListFrame.setVisibility(View.GONE);
                    mEditText.requestFocus();
                } else {
                    // drill down to this directory
                    int index = mCurrentSelection.getData().hasParent() ? position - 1
                            : position;
                    selectFolder(mCurrentSelection.getChild(index));
                }
            }
        });
    }

    private void initFolderTree() {
        if (mRootFolder == null) {
            mRootFolder = mCurrentSelection = new Tree<RemoteFolder>(mApp
                    .getRemoteClientManager().getRemoteClient()
                    .getFolder(mInitialPath));
        } else {
            // use initialPath to find the correct folder in the tree
            Tree<RemoteFolder> tree = findFolderInTree(mRootFolder, mInitialPath);

            if (tree != null) {
                mCurrentSelection = tree;
            }
        }
    }

    private Tree<RemoteFolder> findFolderInTree(Tree<RemoteFolder> tree, String path) {
        if (tree.getData().getPath().equalsIgnoreCase(path)) {
            return tree;
        }

        if (tree.isLoaded()) {
            for (Tree<RemoteFolder> child : tree.getChildren()) {
                Tree<RemoteFolder> res = findFolderInTree(child, path);

                if (res != null) {
                    return res;
                }
            }
        }

        return null;
    }

    private void upToParent() {
        if (mCurrentSelection.getParent() != null) {
            selectFolder(mCurrentSelection.getParent());

            return;
        }

        RemoteFolder parent = mApp.getRemoteClientManager().getRemoteClient()
                .getFolder(mCurrentSelection.getData().getParentPath());
        mRootFolder = new Tree<RemoteFolder>(parent);
        selectFolder(mRootFolder);
    }

    private void addNew(String path) {
        Tree<RemoteFolder> tree = findFolderInTree(mCurrentSelection, path);

        if (tree == null) {
            RemoteFolder folder = mApp.getRemoteClientManager()
                    .getRemoteClient().getFolder(path);
            tree = mCurrentSelection.addChild(folder);
        }

        setCurrentSelection(tree);
    }

    private void setCurrentSelection(Tree<RemoteFolder> folder) {
        mCurrentSelection = folder;
        mCurrentFolderTextView.setText(folder.getData().getName());
        populateListView(folder.getChildren());
    }

    private void selectFolder(Tree<RemoteFolder> folder) {
        if (!folder.isLoaded()) {
            getRemoteDirectoryListing(folder);
        } else {
            setCurrentSelection(folder);
        }
    }

    private void populateListView(List<Tree<RemoteFolder>> list) {
        mAdapter.clear();

        if (mCurrentSelection.getData().hasParent()) {
            mAdapter.add(getContext().getString(R.string.todo_path_prev_folder,
                    mCurrentSelection.getData().getParentName()));
        }

        if (list != null) {
            for (Tree<RemoteFolder> folder : list) {
                mAdapter.add(folder.getData().getName());
            }
        }

        mAdapter.add(getContext().getString(R.string.todo_path_add_new));
    }

    private void getRemoteDirectoryListing(final Tree<RemoteFolder> folder) {
        new AsyncTask<Void, Void, List<RemoteFolder>>() {
            @Override
            protected void onPreExecute() {
                showProgressIndicator();
                mAdapter.clear();
            }

            @Override
            protected List<RemoteFolder> doInBackground(Void... params) {
                try {
                    return mApp.getRemoteClientManager().getRemoteClient()
                            .getSubFolders(folder.getData().getPath());
                } catch (Exception e) {
                    Log.d(TAG, "failed to get remote folder list", e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<RemoteFolder> result) {
                Dialog dialog = getDialog();

                if (dialog == null || !dialog.isShowing()) {
                    return;
                }

                if (result == null) {
                    showErrorMessage();

                    return;
                }

                // if we are loading the parent of our current folder
                // add the current folder as a child so we can keep it's
                // children
                boolean shouldAddCurrent = mCurrentSelection.getData().getParentPath()
                        .equals(folder.getData().getPath());
                for (RemoteFolder child : result) {
                    if (shouldAddCurrent && mCurrentSelection.getData().equals(child)) {
                        folder.addChild(mCurrentSelection);
                        shouldAddCurrent = false;
                    } else {
                        folder.addChild(child);
                    }
                }

                if (shouldAddCurrent) {
                    // if the user created the current folder,
                    // it won't really exist yet, but let's not
                    // wipe it out.
                    folder.addChild(mCurrentSelection);
                }

                folder.setLoaded();
                setCurrentSelection(folder);
            }
        }.execute();
    }

    protected void showErrorMessage() {
        mEmptyView.findViewById(R.id.loading_spinner).setVisibility(View.GONE);
        mEmptyView.findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
    }

    protected void showProgressIndicator() {
        mEmptyView.findViewById(R.id.empty_text).setVisibility(View.GONE);
        mEmptyView.findViewById(R.id.loading_spinner).setVisibility(View.VISIBLE);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final SavedState myState = new SavedState(superState);
        myState.displayMode = mDisplayMode.name();

        if (mCurrentSelection != null) {
            myState.initialPath = mCurrentSelection.getData().getPath();
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
        mDisplayMode = DisplayMode.valueOf(myState.displayMode);
        mInitialPath = myState.initialPath;
        super.onRestoreInstanceState(myState.getSuperState());
    }

    enum DisplayMode {
        NORMAL, WARNING, ADD_NEW
    }

    private static class SavedState extends BaseSavedState {
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
