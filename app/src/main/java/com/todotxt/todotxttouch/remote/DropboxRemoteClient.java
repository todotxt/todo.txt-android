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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
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
    private static final File TODO_TXT_TMP_FILE = new File(
            TodoApplication.getAppContetxt().getFilesDir(),
            "tmp/todo.txt");
    private static final File DONE_TXT_TMP_FILE = new File(
            TodoApplication.getAppContetxt().getFilesDir(),
            "tmp/done.txt");

    private DbxClientV2 client;
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
        String accessToken = sharedPreferences.getAccessToken();
        if (accessToken == null){
            Log.i(TAG, "Stored accessToken is null. Retrieving new token.");
            accessToken = Auth.getOAuth2Token();
            if(accessToken != null) {
                Log.i(TAG, "Successfully retrieved new token.");
                sharedPreferences.storeAccessToken(accessToken);
                DropboxClientFactory.init(accessToken);
                this.client = DropboxClientFactory.getClient();
                Log.i(TAG, "Successfully initialized new Dropbox client.");
                Log.i(TAG, "Authentication completed successfully.");
                return true;
            }
        } else {
            DropboxClientFactory.init(accessToken);
            this.client = DropboxClientFactory.getClient();
            Log.i(TAG, "Successfully initialized new Dropbox client.");
            Log.i(TAG, "Authentication completed successfully.");
            return true;
        }
        return false;
    }

    @Override
    public void deauthenticate() {
        clearAuthToken();
        TODO_TXT_TMP_FILE.delete();
        DONE_TXT_TMP_FILE.delete();
    }

    @Override
    public boolean isAuthenticated() {
        return hasToken();
    }

    @Override
    public boolean isLoggedIn() {
        return hasToken();
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
                client, dropboxFiles);
        downloader.pullFiles();

        File downloadedTodoFile = null;
        File downloadedDoneFile = null;

        if (todoFile.getStatus() == DropboxFileStatus.SUCCESS) {
            downloadedTodoFile = todoFile.getLocalFile();
            sharedPreferences.storeFileRevision(TodoPreferences.PREF_TODO_REV,
                    todoFile.getLoadedMetadata().getRev());
        }

        if (doneFile.getStatus() == DropboxFileStatus.SUCCESS) {
            downloadedDoneFile = doneFile.getLocalFile();
            sharedPreferences.storeFileRevision(TodoPreferences.PREF_DONE_REV,
                    doneFile.getLoadedMetadata().getRev());
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

        DropboxFileUploader uploader = new DropboxFileUploader(client,
                dropboxFiles, overwrite);
        uploader.pushFiles();

        if (uploader.getStatus() == DropboxFileStatus.SUCCESS) {
            if (dropboxFiles.size() > 0) {
                DropboxFile todoDropboxFile = dropboxFiles.get(0);
                if (todoDropboxFile.getStatus() == DropboxFileStatus.SUCCESS) {
                    sharedPreferences.storeFileRevision(
                            TodoPreferences.PREF_TODO_REV,
                            todoDropboxFile.getLoadedMetadata().getRev());
                }
            }
            if (dropboxFiles.size() > 1) {
                DropboxFile doneDropboxFile = dropboxFiles.get(1);
                if (doneDropboxFile.getStatus() == DropboxFileStatus.SUCCESS) {
                    sharedPreferences.storeFileRevision(
                            TodoPreferences.PREF_DONE_REV,
                            doneDropboxFile.getLoadedMetadata().getRev());
                }
            }
        }
    }

    @Override
    public boolean startLogin() {
        Context cxt = todoApplication.getApplicationContext();
        Auth.startOAuth2Authentication(
                cxt, cxt.getString(R.string.dropbox_consumer_key));

        return true;
    }

    @Override
    public boolean finishLogin() {
        String accessToken = Auth.getOAuth2Token();
        if (accessToken != null) {
            Log.i(TAG, "Dropbox authentication successful.");
            sharedPreferences.storeAccessToken(accessToken);
            Log.i(TAG, "Dropbox authentication complete.");
            DropboxClientFactory.init(accessToken);
            return true;
        }

        Log.i(TAG, "Dropbox authentication not successful.");

        return false;
    }

    boolean hasToken(){
        return sharedPreferences.getAccessToken() != null
                && sharedPreferences.getAccessToken() != "";
    }

    void sendBroadcast(Intent intent) {
        todoApplication.sendBroadcast(intent);
    }

    void showToast(String string) {
        Util.showToastLong(todoApplication, string);
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
            Log.d(TAG, "getting file listing for path " + path);
            if (path == "/") {
                Log.d(TAG, "setting path to '' from '/' to avoid bad request.");
                path = "";
            }

            ListFolderResult folders = client.files().listFolder(path);

            Log.d(TAG, "num entries returned: " + folders.getEntries().size());

            for (Metadata m : folders.getEntries()) {
                if (m instanceof FolderMetadata) {
                    results.add(new DropboxRemoteFolder((FolderMetadata) m));
                }
            }
        } catch (ListFolderErrorException lfe) {
            if (lfe.errorValue.toString() != "NOT_FOUND") {
                Log.e(TAG, "Error getting folders for path: " + path);
                Log.e(TAG, "Dropbox returned error code: " + lfe.errorValue);

                throw new RemoteException("Failed to get folder listing from Dropbox", lfe);
            }

            // A 404 is OK. We will create the directory if necessary when we
            // push
            Log.i(TAG, "Remote path not found: " + path);
        } catch (DbxException e) {
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
