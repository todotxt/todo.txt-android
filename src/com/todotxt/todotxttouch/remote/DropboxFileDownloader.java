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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.todotxt.todotxttouch.util.Util;

public class DropboxFileDownloader {

	private DropboxAPI<?> dropboxApi;
	private DropboxFileStatus status;
	private Collection<DropboxFile> files;

	/**
	 * @param files
	 */
	public DropboxFileDownloader(DropboxAPI<?> dropboxApi, Collection<DropboxFile> files) {
		this.dropboxApi = dropboxApi;
		this.files = files;
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

	public void pullFiles() {
		status = DropboxFileStatus.STARTED;

		// load each metadata
		for (DropboxFile file : files) {
			loadMetadata(file);
		}

		// load each file with a newer rev
		for (DropboxFile file : files) {
			if (file.getStatus() == DropboxFileStatus.FOUND) {
				loadFile(file);
			}
		}

		status = DropboxFileStatus.SUCCESS;
	}

	private void loadMetadata(DropboxFile file) {
		DropboxAPI.Entry metadata = null;
		try {
			metadata = dropboxApi.metadata(file.getRemoteFile(), 0, null,
					false, null);
		} catch (DropboxException e) {
			file.setStatus(DropboxFileStatus.NOT_FOUND);
			return;
		}

		file.setLoadedMetadata(metadata);

		if (metadata.rev.equals(file.getOriginalRev())) {
			// don't bother downloading if the rev is the same
			file.setStatus(DropboxFileStatus.NOT_CHANGED);
		} else if (metadata.isDeleted) {
			file.setStatus(DropboxFileStatus.NOT_FOUND);
		} else {
			file.setStatus(DropboxFileStatus.FOUND);
		}
	}

	private void loadFile(DropboxFile file) {
		File localFile = file.getLocalFile();
		try {
			if (!localFile.exists()) {
				Util.createParentDirectory(localFile);
				localFile.createNewFile();
			}
		} catch (IOException e) {
			throw new RemoteException("Failed to ensure that file exists", e);
		}

		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(localFile);
		} catch (FileNotFoundException e1) {
			throw new RemoteException("Failed to find file", e1);
		}

		try {
			dropboxApi.getFile(file.getRemoteFile(),
					file.getLoadedMetadata().rev, outputStream, null);
			outputStream.flush();
			outputStream.close();
		} catch (DropboxException e) {
			throw new RemoteException("Cannot get file from Dropbox");
		} catch (IOException e) {
			throw new RemoteException("Failed to find file", e);
		}

		file.setStatus(DropboxFileStatus.SUCCESS);
	}

}
