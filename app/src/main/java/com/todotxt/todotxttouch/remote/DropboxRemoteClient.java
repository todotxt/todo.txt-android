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

package com.todotxt.todotxttouch.remote;

import android.content.Intent;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.todotxt.todotxttouch.Constants;
import com.todotxt.todotxttouch.R;
import com.todotxt.todotxttouch.TodoApplication;
import com.todotxt.todotxttouch.TodoPreferences;
import com.todotxt.todotxttouch.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class DropboxRemoteClient implements RemoteClient {
    final static String TAG = DropboxRemoteClient.class.getSimpleName();

    private static final String TODO_TXT_REMOTE_FILE_NAME = "todo.txt";
    private static final String DONE_TXT_REMOTE_FILE_NAME = "done.txt";
    private static final AccessType ACCESS_TYPE = AccessType.DROPBOX;
    private static final File TODO_TXT_TMP_FILE = new File(
            TodoApplication.getAppContetxt().getFilesDir(),
            "tmp/todo.txt");
    private static final File DONE_TXT_TMP_FILE = new File(
            TodoApplication.getAppContetxt().getFilesDir(),
            "tmp/done.txt");

    private DropboxAPI<AndroidAuthSession> dropboxApi;
    private TodoApplication todoApplication;
    private TodoPreferences sharedPreferences;

    public DropboxRemoteClient(TodoApplication todoApplication,
                               TodoPreferences sharedPreferences) {
        this.todoApplication = todoApplication;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public Client getClient() {
        return Client.DROPBOX;
    }

    /**
     * Get the stored key - secret pair for authenticating the user
     *
     * @return a string array with key and secret
     */
    private AccessTokenPair getStoredKeys() {
        String key = null;
        String secret = null;

        key = sharedPreferences.getAccessToken();
        secret = sharedPreferences.getAccessTokenSecret();

        if (key != null && secret != null) {
            return new AccessTokenPair(key, secret);
        }

        return null;
    }

    /**
     * Store the key - secret pair for an authenticated user.
     *
     * @param accessTokenKey
     * @param accessTokenSecret
     */
    void storeKeys(String accessTokenKey, String accessTokenSecret) {
        sharedPreferences.storeAccessToken(accessTokenKey, accessTokenSecret);
    }

    /**
     * Clear the stored keys, either because they are bad, or user has requested
     * it
     */
    private void clearAuthToken() {
        sharedPreferences.clear();
    }

    @Override
    public boolean authenticate() {
        String consumerKey = todoApplication.getResources().getText(R.string.dropbox_consumer_key)
                .toString();
        String consumerSecret = todoApplication.getText(R.string.dropbox_consumer_secret)
                .toString();
        consumerKey = consumerKey.replaceFirst("^db-", "");

        AppKeyPair appKeys = new AppKeyPair(consumerKey, consumerSecret);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        dropboxApi = new DropboxAPI<AndroidAuthSession>(session);

        AccessTokenPair access = getStoredKeys();

        if (access != null) {
            dropboxApi.getSession().setAccessTokenPair(access);
        }

        return true;
    }

    @Override
    public void deauthenticate() {
        clearAuthToken();
        dropboxApi.getSession().unlink();
        TODO_TXT_TMP_FILE.delete();
        DONE_TXT_TMP_FILE.delete();
    }

    @Override
    public boolean isAuthenticated() {
        return dropboxApi.getSession().isLinked();
    }

    @Override
    public boolean isLoggedIn() {
        return dropboxApi.getSession().isLinked();
    }

    @Override
    public PullTodoResult pullTodo() {
        if (!isAvailable()) {
            Log.d(TAG, "Offline. Not Pulling.");

            Intent i = new Intent(Constants.INTENT_SET_MANUAL);
            sendBroadcast(i);

            return new PullTodoResult(null, null);
        }

        DropboxFile todoFile = new DropboxFile(
                getTodoFileRemotePathAndFilename(), TODO_TXT_TMP_FILE,
                sharedPreferences
                        .getFileRevision(TodoPreferences.PREF_TODO_REV));

        DropboxFile doneFile = new DropboxFile(
                getDoneFileRemotePathAndFilename(), DONE_TXT_TMP_FILE,
                sharedPreferences
                        .getFileRevision(TodoPreferences.PREF_DONE_REV));

        ArrayList<DropboxFile> dropboxFiles = new ArrayList<DropboxFile>(2);
        dropboxFiles.add(todoFile);
        dropboxFiles.add(doneFile);

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxApi, dropboxFiles);
        downloader.pullFiles();

        File downloadedTodoFile = null;
        File downloadedDoneFile = null;

        if (todoFile.getStatus() == DropboxFileStatus.SUCCESS) {
            downloadedTodoFile = todoFile.getLocalFile();
            sharedPreferences.storeFileRevision(TodoPreferences.PREF_TODO_REV,
                    todoFile.getLoadedMetadata().rev);
        }

        if (doneFile.getStatus() == DropboxFileStatus.SUCCESS) {
            downloadedDoneFile = doneFile.getLocalFile();
            sharedPreferences.storeFileRevision(TodoPreferences.PREF_DONE_REV,
                    doneFile.getLoadedMetadata().rev);
        }

        return new PullTodoResult(downloadedTodoFile, downloadedDoneFile);
    }

    @Override
    public void pushTodo(File todoFile, File doneFile, boolean overwrite) {
        ArrayList<DropboxFile> dropboxFiles = new ArrayList<DropboxFile>(2);
        if (todoFile != null) {
            dropboxFiles.add(new DropboxFile(
                    getTodoFileRemotePathAndFilename(), todoFile,
                    sharedPreferences
                            .getFileRevision(TodoPreferences.PREF_TODO_REV)));
        }

        if (doneFile != null) {
            dropboxFiles.add(new DropboxFile(
                    getDoneFileRemotePathAndFilename(), doneFile,
                    sharedPreferences
                            .getFileRevision(TodoPreferences.PREF_DONE_REV)));
        }

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxApi,
                dropboxFiles, overwrite);
        uploader.pushFiles();

        if (uploader.getStatus() == DropboxFileStatus.SUCCESS) {
            if (dropboxFiles.size() > 0) {
                DropboxFile todoDropboxFile = dropboxFiles.get(0);
                if (todoDropboxFile.getStatus() == DropboxFileStatus.SUCCESS) {
                    sharedPreferences.storeFileRevision(
                            TodoPreferences.PREF_TODO_REV,
                            todoDropboxFile.getLoadedMetadata().rev);
                }
            }
            if (dropboxFiles.size() > 1) {
                DropboxFile doneDropboxFile = dropboxFiles.get(1);
                if (doneDropboxFile.getStatus() == DropboxFileStatus.SUCCESS) {
                    sharedPreferences.storeFileRevision(
                            TodoPreferences.PREF_DONE_REV,
                            doneDropboxFile.getLoadedMetadata().rev);
                }
            }
        }
    }

    @Override
    public boolean startLogin() {
        dropboxApi.getSession().startAuthentication(
                todoApplication.getApplicationContext());

        return true;
    }

    @Override
    public boolean finishLogin() {
        if (dropboxApi.getSession().authenticationSuccessful()) {
            Log.i(TAG, "Dropbox authentication successful.");

            try {
                dropboxApi.getSession().finishAuthentication();

                Log.i(TAG, "Dropbox authentication complete.");

                AccessTokenPair tokens = dropboxApi.getSession()
                        .getAccessTokenPair();

                storeKeys(tokens.key, tokens.secret);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);

                return false;
            }

            return true;
        }

        Log.i(TAG, "Dropbox authentication not successful.");

        return false;
    }

    void sendBroadcast(Intent intent) {
        todoApplication.sendBroadcast(intent);
    }

    void showToast(String string) {
        Util.showToastLong(todoApplication, string);
    }

    DropboxAPI<AndroidAuthSession> getAPI() {
        return dropboxApi;
    }

    String getRemotePath() {
        return sharedPreferences.getTodoFilePath();
    }

    String getTodoFileRemotePathAndFilename() {
        return getRemotePath() + "/" + TODO_TXT_REMOTE_FILE_NAME;
    }

    String getDoneFileRemotePathAndFilename() {
        return getRemotePath() + "/" + DONE_TXT_REMOTE_FILE_NAME;
    }

    @Override
    public boolean isAvailable() {
        return Util.isOnline(todoApplication.getApplicationContext());
    }

    @Override
    public List<RemoteFolder> getSubFolders(String path) {
        List<RemoteFolder> results = new ArrayList<RemoteFolder>();

        try {
            Log.d(TAG, "getting file listiing for path " + path);

            Entry metadata = dropboxApi.metadata(path, 0, null, true, null);

            Log.d(TAG, "num entries returned: " + metadata.contents.size());

            for (Entry e : metadata.contents) {
                if (e.isDir && !e.isDeleted) {
                    results.add(new DropboxRemoteFolder(e));
                }
            }
        } catch (DropboxServerException dse) {
            if (dse.error != DropboxServerException._404_NOT_FOUND) {
                Log.e(TAG, "Error getting folders for path: " + path);
                Log.e(TAG, "Dropbox returned error code: " + dse.error);

                throw new RemoteException("Failed to get folder listing from Dropbox", dse);
            }

            // A 404 is OK. We will create the directory if necessary when we
            // push
            Log.i(TAG, "Remote path not found: " + path);
        } catch (DropboxException e) {
            Log.e(TAG, "Error getting folders for path: " + path);

            throw new RemoteException("Failed to get folder listing from Dropbox", e);
        }

        return results;
    }

    @Override
    public RemoteFolder getFolder(final String path) {
        return new DropboxRemoteFolder(path);
    }
}
