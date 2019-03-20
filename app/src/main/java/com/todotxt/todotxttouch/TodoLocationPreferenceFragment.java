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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.todotxt.todotxttouch.remote.RemoteFolder;
import com.todotxt.todotxttouch.util.Tree;

import java.io.File;
import java.util.List;

public class TodoLocationPreferenceFragment extends PreferenceDialogFragmentCompat {
    final static String TAG = TodoLocationPreferenceFragment.class.getSimpleName();
    private TodoApplication mApp;
    private ArrayAdapter<String> mAdapter;
    private String mInitialPath;
    private Tree<RemoteFolder> mRootFolder;
    private Tree<RemoteFolder> mCurrentSelection;
    private ListView mListView;
    private View mEmptyView;
    private View mListFrame;
    private EditText mEditText;
    private TextView mCurrentFolderTextView;
    private DisplayMode mDisplayMode = DisplayMode.NORMAL;
    private boolean mDisplayWarning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisplayMode = mDisplayWarning ? DisplayMode.WARNING : DisplayMode.NORMAL;
        mInitialPath = ((TodoLocationPreference)getPreference()).getInitialPath();
    }

    @Override
    protected View onCreateDialogView(Context context) {
        if (DisplayMode.WARNING == mDisplayMode) {
            return null;
        } else {
            return super.onCreateDialogView(context);
        }
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);


        mCurrentFolderTextView = (TextView) view.findViewById(R.id.folder_name);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.empty);
        mListFrame = view.findViewById(R.id.list_frame);
        mEditText = (EditText) view.findViewById(R.id.add_new);

        if (mDisplayMode == TodoLocationPreferenceFragment.DisplayMode.ADD_NEW) {
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

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (position == 0 && mCurrentSelection.getData().hasParent()) {
                    // go back up to previous directory
                    upToParent();
                } else if (position == mAdapter.getCount() - 1) {
                    // signal that AddNew was clicked
                    mDisplayMode = TodoLocationPreferenceFragment.DisplayMode.ADD_NEW;
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

//    @Override
//    public void onClick(DialogInterface dialog, int which) {
//
//        // I'm feeling dangerous
//        if (which == -1 && mDisplayMode == DisplayMode.WARNING) {
//            mDisplayMode = DisplayMode.NORMAL;
//        } else {
//            super.onClick(dialog, which);
//        }
//    }

    //    @Override
    public void onDialogClosed(boolean positiveResult) {
        // If we are displaying the warning message and the user
        // clicked "I'm feeling dangerous", then redisplay the
        // dialog with the default layout
        if (mDisplayMode == DisplayMode.WARNING && positiveResult) {
            mDisplayWarning = false;
            show(getFragmentManager(), "android.support.v7.preference" +
                    ".PreferenceFragment.DIALOG");

            return;
        }

        if (mCurrentSelection != null && positiveResult) {
            String value = mCurrentSelection.getData().getPath();
            if (mDisplayMode == DisplayMode.ADD_NEW) {
                value = new File(value, mEditText.getText().toString())
                        .toString();
                addNew(value);
            }

            TodoLocationPreference preference = (TodoLocationPreference) getPreference();

            if (preference.callChangeListener(mCurrentSelection.getData().getPath())) {
                preference.setCurrentSelection(mCurrentSelection.getData().getPath());
            }
        }

        mInitialPath = null;
    }

    @Override
    protected void onPrepareDialogBuilder(android.support.v7.app.AlertDialog.Builder builder) {
        // Display the warning message if necessary.
        // Otherwise, just use the default layout.
        if (mDisplayMode == DisplayMode.WARNING) {
            builder.setMessage(getWarningMessage());
            builder.setPositiveButton(R.string.todo_path_warning_override, this);
        } else {
            // nothing to do here...
        }
    }

    private CharSequence getWarningMessage() {
        SpannableString ss = new SpannableString(getContext().getString(
                R.string.todo_path_warning));
        ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
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
//                Dialog dialog = getDialog();

//                if (dialog == null || !dialog.isShowing()) {
//                    return;
//                }

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

    public static TodoLocationPreferenceFragment newInstance(String key) {
        final TodoLocationPreferenceFragment fragment = new TodoLocationPreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    public void setApp(TodoApplication mApp) {
        this.mApp = mApp;
    }

    public void setDisplayWarning(boolean mDisplayWarning) {
        this.mDisplayWarning = mDisplayWarning;
    }

    enum DisplayMode {
        NORMAL, WARNING, ADD_NEW
    }
}
