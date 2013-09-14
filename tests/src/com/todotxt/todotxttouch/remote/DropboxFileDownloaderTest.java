/**
 * This file is part of Todo.txtndroid app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.tTodo.txttware: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt is Todo.txt the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt.  If not,Todo.txt//www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;

import android.test.ApplicationTestCase;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.todotxt.todotxttouch.TodoApplication;

public class DropboxFileDownloaderTest extends ApplicationTestCase<TodoApplication> {
    private static final String remotefile1 = "remotefile1";
    private static final String remotefile2 = "remotefile2";
    private static final String localpath1 = "data/com.todotxt.todotxttouch/tmp/test1.txt";
    private static final String localpath2 = "data/com.todotxt.todotxttouch/tmp/test2.txt";

    private static final String remoterev1 = "remoterev1";
    private static final String remoterev2 = "remoterev2";
    private static final String origrev1 = "origrev1";
    private static final String origrev2 = "origrev2";

    private File localFile1;
    private File localFile2;
    DropboxFile dbFile1;
    DropboxFile dbFile2;
    ArrayList<DropboxFile> dropboxFiles1;
    ArrayList<DropboxFile> dropboxFiles2;

    public DropboxFileDownloaderTest(Class<TodoApplication> applicationClass) {
        super(applicationClass);
    }

    protected void setUp() throws Exception {
        createApplication();

        localFile1 = new File(TodoApplication.getAppContetxt().getFilesDir(), localpath1);
        localFile2 = new File(TodoApplication.getAppContetxt().getFilesDir(), localpath2);

        if (localFile1.exists()) {
            localFile1.delete();
        }
        if (localFile2.exists()) {
            localFile2.delete();
        }

        dbFile1 = new DropboxFile(remotefile1, localFile1, origrev1);
        dbFile2 = new DropboxFile(remotefile2, localFile2, origrev2);
        dropboxFiles1 = new ArrayList<DropboxFile>(1);
        dropboxFiles1.add(dbFile1);
        dropboxFiles2 = new ArrayList<DropboxFile>(2);
        dropboxFiles2.add(dbFile1);
        dropboxFiles2.add(dbFile2);
    }

    private DropboxServerException notFoundException() {
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory.newHttpResponse(new BasicStatusLine(
                HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, null), null);
        return new DropboxServerException(response);
    }

    private DropboxAPI.Entry create_metadata(String rev) {
        return create_metadata(rev, false);
    }

    private DropboxAPI.Entry create_metadata(String rev, boolean isDeleted) {
        DropboxAPI.Entry metadata = new DropboxAPI.Entry();
        metadata.rev = rev;
        metadata.isDeleted = isDeleted;
        return metadata;
    }

    public void testRemoteFileMissing() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                throw notFoundException();
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles1);

        downloader.pullFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 1 file", 1, downloader.getFiles().size());
        assertEquals("Status should be NOT_FOUND", DropboxFileStatus.NOT_FOUND,
                dbFile1.getStatus());
    }

    public void testRemoteFileDeleted() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(remoterev1, true);
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles1);

        downloader.pullFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 1 file", 1, downloader.getFiles().size());
        assertEquals("Status should be NOT_FOUND", DropboxFileStatus.NOT_FOUND,
                dbFile1.getStatus());
    }

    public void testRemoteFileExists() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(remoterev1);
            }

            public DropboxAPI.DropboxFileInfo getFile(String arg0, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                assertEquals(remotefile1, arg0);
                PrintWriter w = new PrintWriter(out);
                w.println("testRemoteFileExists");
                w.flush();
                return null;
            }

        };

        assertFalse(localFile1.getAbsolutePath()
                + " should not exist beforehand", localFile1.exists());

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles1);

        downloader.pullFiles();

        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 1 file", 1, downloader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertTrue(localFile1.getAbsolutePath() + " should be created",
                localFile1.exists());
        try {
            assertEquals("testRemoteFileExists", new BufferedReader(
                    new FileReader(localFile1)).readLine());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testRemoteFileUpToDate() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(origrev1);
            }

            public DropboxAPI.DropboxFileInfo getFile(String arg0, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                fail("getFile should not be called");
                return null;
            }

        };

        assertFalse(localFile1.getAbsolutePath()
                + " should not exist beforehand", localFile1.exists());

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles1);

        downloader.pullFiles();

        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 1 file", 1, downloader.getFiles().size());
        assertEquals("Status should be NOT_CHANGED",
                DropboxFileStatus.NOT_CHANGED, dbFile1.getStatus());
        assertFalse(localFile1.getAbsolutePath() + " should not be created",
                localFile1.exists());
    }

    public void testRemoteFileDownloadError() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(remoterev1);
            }

            public DropboxAPI.DropboxFileInfo getFile(String arg0, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                throw new DropboxException("stub throw");
            }

        };

        assertFalse(localFile1.getAbsolutePath()
                + " should not exist beforehand", localFile1.exists());

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles1);

        boolean thrown = false;
        try {
            downloader.pullFiles();
        } catch (RemoteException e) {
            thrown = true;
        } catch (Throwable t) {
            fail("Unexpected exception: " + t.toString());
        }
        assertTrue("RemoteException should be thrown", thrown);

        assertEquals("Status should be STARTED", DropboxFileStatus.STARTED,
                downloader.getStatus());
        assertEquals("Should have 1 file", 1, downloader.getFiles().size());
        assertEquals("Status should be FOUND", DropboxFileStatus.FOUND,
                dbFile1.getStatus());
        assertEquals(0, localFile1.length());
    }

    public void testBothFilesMissing() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                throw notFoundException();
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles2);

        downloader.pullFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 2 files", 2, downloader.getFiles().size());
        assertEquals("Status should be NOT_FOUND", DropboxFileStatus.NOT_FOUND,
                dbFile1.getStatus());
        assertEquals("Status should be NOT_FOUND", DropboxFileStatus.NOT_FOUND,
                dbFile2.getStatus());
    }

    public void testFirstRemoteFileExists() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remoterev1);
                } else {
                    throw notFoundException();
                }
            }

            public DropboxAPI.DropboxFileInfo getFile(String file, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    PrintWriter w = new PrintWriter(out);
                    w.println("testFirstRemoteFileExists");
                    w.flush();
                } else {
                    fail("getFile called for wrong file");
                }
                return null;
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles2);

        downloader.pullFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 2 files", 2, downloader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals("Status should be NOT_FOUND", DropboxFileStatus.NOT_FOUND,
                dbFile2.getStatus());
        assertTrue(localFile1.getAbsolutePath() + " should be created",
                localFile1.exists());
        try {
            assertEquals("testFirstRemoteFileExists", new BufferedReader(
                    new FileReader(localFile1)).readLine());
        } catch (Exception e) {
            fail(e.toString());
        }
        assertFalse(localFile2.getAbsolutePath() + " should not be created",
                localFile2.exists());
    }

    public void testSecondRemoteFileExists() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile2)) {
                    return create_metadata(remoterev1);
                } else {
                    throw notFoundException();
                }
            }

            public DropboxAPI.DropboxFileInfo getFile(String file, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                if (file.equals(remotefile2)) {
                    PrintWriter w = new PrintWriter(out);
                    w.println("testSecondRemoteFileExists");
                    w.flush();
                } else {
                    fail("getFile called for wrong file");
                }
                return null;
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles2);

        downloader.pullFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 2 files", 2, downloader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile2.getStatus());
        assertEquals("Status should be NOT_FOUND", DropboxFileStatus.NOT_FOUND,
                dbFile1.getStatus());
        assertTrue(localFile2.getAbsolutePath() + " should be created",
                localFile2.exists());
        try {
            assertEquals("testSecondRemoteFileExists", new BufferedReader(
                    new FileReader(localFile2)).readLine());
        } catch (Exception e) {
            fail(e.toString());
        }
        assertFalse(localFile1.getAbsolutePath() + " should not be created",
                localFile1.exists());
    }

    public void testBothRemoteFilesExist() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remoterev1);
                } else {
                    return create_metadata(remoterev2);
                }
            }

            public DropboxAPI.DropboxFileInfo getFile(String file, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                PrintWriter w = new PrintWriter(out);
                if (file.equals(remotefile1)) {
                    w.println("testFirstRemoteFileExists");
                } else {
                    w.println("testSecondRemoteFileExists");
                }
                w.flush();
                return null;
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles2);

        downloader.pullFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 2 files", 2, downloader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile2.getStatus());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertTrue(localFile1.getAbsolutePath() + " should be created",
                localFile1.exists());
        try {
            assertEquals("testFirstRemoteFileExists", new BufferedReader(
                    new FileReader(localFile1)).readLine());
        } catch (Exception e) {
            fail(e.toString());
        }
        assertTrue(localFile2.getAbsolutePath() + " should be created",
                localFile2.exists());
        try {
            assertEquals("testSecondRemoteFileExists", new BufferedReader(
                    new FileReader(localFile2)).readLine());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testFirstRemoteFileUpToDate() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(origrev1);
                } else {
                    return create_metadata(remoterev2);
                }
            }

            public DropboxAPI.DropboxFileInfo getFile(String file, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    fail("getFile should not be called for first file");
                } else {
                    PrintWriter w = new PrintWriter(out);
                    w.println("testSecondRemoteFileExists");
                    w.flush();
                }
                return null;
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles2);

        downloader.pullFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 2 files", 2, downloader.getFiles().size());
        assertEquals("Status should be NOT_CHANGED",
                DropboxFileStatus.NOT_CHANGED, dbFile1.getStatus());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile2.getStatus());
        assertFalse(localFile1.getAbsolutePath() + " should not be created",
                localFile1.exists());
        assertTrue(localFile2.getAbsolutePath() + " should be created",
                localFile2.exists());
        try {
            assertEquals("testSecondRemoteFileExists", new BufferedReader(
                    new FileReader(localFile2)).readLine());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testSecondRemoteFileUpToDate() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remoterev1);
                } else {
                    return create_metadata(origrev2);
                }
            }

            public DropboxAPI.DropboxFileInfo getFile(String file, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                if (file.equals(remotefile2)) {
                    fail("getFile should not be called for second file");
                } else {
                    PrintWriter w = new PrintWriter(out);
                    w.println("testFirstRemoteFileExists");
                    w.flush();
                }
                return null;
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles2);

        downloader.pullFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 2 files", 2, downloader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals("Status should be NOT_CHANGED",
                DropboxFileStatus.NOT_CHANGED, dbFile2.getStatus());
        assertTrue(localFile1.getAbsolutePath() + " should be created",
                localFile1.exists());
        try {
            assertEquals("testFirstRemoteFileExists", new BufferedReader(
                    new FileReader(localFile1)).readLine());
        } catch (Exception e) {
            fail(e.toString());
        }
        assertFalse(localFile2.getAbsolutePath() + " should not be created",
                localFile2.exists());
    }

    public void testBothRemoteFilesUpToDate() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(origrev1);
                } else {
                    return create_metadata(origrev2);
                }
            }

            public DropboxAPI.DropboxFileInfo getFile(String file, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                fail("getFile should not be called for either file");
                return null;
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles2);

        downloader.pullFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                downloader.getStatus());
        assertEquals("Should have 2 files", 2, downloader.getFiles().size());
        assertEquals("Status should be NOT_CHANGED",
                DropboxFileStatus.NOT_CHANGED, dbFile1.getStatus());
        assertEquals("Status should be NOT_CHANGED",
                DropboxFileStatus.NOT_CHANGED, dbFile2.getStatus());
        assertFalse(localFile1.getAbsolutePath() + " should not be created",
                localFile1.exists());
        assertFalse(localFile2.getAbsolutePath() + " should not be created",
                localFile2.exists());
    }

    public void testFirstRemoteFileError() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remoterev1);
                } else {
                    return create_metadata(remoterev2);
                }
            }

            public DropboxAPI.DropboxFileInfo getFile(String file, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    throw new DropboxException("stub throw");
                } else {
                    PrintWriter w = new PrintWriter(out);
                    w.println("testSecondRemoteFileExists");
                    w.flush();
                }
                return null;
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles2);

        boolean thrown = false;
        try {
            downloader.pullFiles();
        } catch (RemoteException e) {
            thrown = true;
        } catch (Throwable t) {
            fail("Unexpected exception: " + t.toString());
        }
        assertTrue("RemoteException should be thrown", thrown);

        assertEquals("Status should be STARTED", DropboxFileStatus.STARTED,
                downloader.getStatus());
        assertEquals("Should have 2 files", 2, downloader.getFiles().size());
        assertEquals("Status should be FOUND", DropboxFileStatus.FOUND,
                dbFile1.getStatus());
        assertEquals("Status should be FOUND", DropboxFileStatus.FOUND,
                dbFile2.getStatus());
        assertEquals(0, localFile1.length());
        assertFalse(localFile2.getAbsolutePath() + " should not be created",
                localFile2.exists());
    }

    public void testSecondRemoteFileError() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remoterev1);
                } else {
                    return create_metadata(remoterev2);
                }
            }

            public DropboxAPI.DropboxFileInfo getFile(String file, String arg1,
                    OutputStream out, ProgressListener arg3)
                    throws DropboxException {
                if (file.equals(remotefile2)) {
                    throw new DropboxException("stub throw");
                } else {
                    PrintWriter w = new PrintWriter(out);
                    w.println("testFirstRemoteFileExists");
                    w.flush();
                }
                return null;
            }
        };

        DropboxFileDownloader downloader = new DropboxFileDownloader(
                dropboxapi, dropboxFiles2);

        boolean thrown = false;
        try {
            downloader.pullFiles();
        } catch (RemoteException e) {
            thrown = true;
        } catch (Throwable t) {
            fail("Unexpected exception: " + t.toString());
        }
        assertTrue("RemoteException should be thrown", thrown);

        assertEquals("Status should be STARTED", DropboxFileStatus.STARTED,
                downloader.getStatus());
        assertEquals("Should have 2 files", 2, downloader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals("Status should be FOUND", DropboxFileStatus.FOUND,
                dbFile2.getStatus());
        assertEquals(0, localFile2.length());
        assertTrue(localFile1.getAbsolutePath() + " should be created",
                localFile1.exists());
        try {
            assertEquals("testFirstRemoteFileExists", new BufferedReader(
                    new FileReader(localFile1)).readLine());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

}
