/**
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.todotxt.todotxttouch.util.Util;

public class DropboxFileUploader {

	private DropboxAPI<?> dropboxApi;
	private DropboxFileStatus status;
	private Collection<DropboxFile> files;
	private boolean overwrite;

	/**
	 * @param files
	 */
	public DropboxFileUploader(DropboxAPI<?> dropboxApi,
			Collection<DropboxFile> files, boolean overwrite) {
		this.dropboxApi = dropboxApi;
		this.files = files;
		this.overwrite = overwrite;
		status = DropboxFileStatus.INITIALIZED;
	}

	/**
	 * @return the status
	 */
	public DropboxFileStatus getStatus() {
		return status;
	}

	/**
	 * @return the files
	 */
	public Collection<DropboxFile> getFiles() {
		return files;
	}

	public void pushFiles() {
		status = DropboxFileStatus.STARTED;

		// load each metadata
		for (DropboxFile file : files) {
			loadMetadata(file);
		}

		// upload each file that has changed
		for (DropboxFile file : files) {
			if (file.getStatus() == DropboxFileStatus.FOUND
					|| file.getStatus() == DropboxFileStatus.NOT_FOUND) {
				uploadFile(file);
			}
		}

		status = DropboxFileStatus.SUCCESS;
	}

	private void loadMetadata(DropboxFile file) {
		DropboxAPI.Entry metadata = null;
		try {
			metadata = dropboxApi.metadata(file.getRemoteFile(), 1, null,
					false, null);
		} catch (DropboxException e) {
			file.setStatus(DropboxFileStatus.NOT_FOUND);
			return;
		}

		if (metadata.isDeleted) {
			file.setStatus(DropboxFileStatus.NOT_FOUND);
		} else {

			file.setLoadedMetadata(metadata);

			if (!overwrite && !metadata.rev.equals(file.getOriginalRev())) {
				file.setStatus(DropboxFileStatus.CONFLICT);
				throw new RemoteConflictException("Local file "
						+ file.getRemoteFile()
						+ " conflicts with remote version.");
			} else {
				file.setStatus(DropboxFileStatus.FOUND);
			}
		}
	}

	private void uploadFile(DropboxFile file) {
		File localFile = file.getLocalFile();
		try {
			if (!localFile.exists()) {
				Util.createParentDirectory(localFile);
				localFile.createNewFile();
			}
		} catch (IOException e) {
			throw new RemoteException("Failed to ensure that file exists", e);
		}

		String rev = null;
		if (file.getLoadedMetadata() != null) {
			rev = file.getLoadedMetadata().rev;
		}

		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(localFile);
		} catch (FileNotFoundException e1) {
			throw new RemoteException("File " + localFile.getAbsolutePath()
					+ " not found", e1);
		}

		DropboxAPI.Entry metadata = null;

		try {
			metadata = dropboxApi.putFile(file.getRemoteFile(), inputStream,
					localFile.length(), rev, null);
			inputStream.close();

		} catch (DropboxUnlinkedException e) {
			throw new RemoteException("User has unlinked.", e);
		} catch (DropboxException e) {
			e.printStackTrace();
			throw new RemoteException("Something went wrong while uploading.",
					e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RemoteException("Problem with IO", e);
		}

		file.setLoadedMetadata(metadata);
		if (!metadata.path.equals(file.getRemoteFile())) {
			// If the uploaded remote path does not match our expected
			// remotePath,
			// then a conflict occurred and we should announce the conflict to
			// the user.
			file.setStatus(DropboxFileStatus.CONFLICT);
			throw new RemoteConflictException("Local file "
					+ file.getRemoteFile() + " conflicts with remote version.");
		} else {
			file.setStatus(DropboxFileStatus.SUCCESS);
		}
	}

}
