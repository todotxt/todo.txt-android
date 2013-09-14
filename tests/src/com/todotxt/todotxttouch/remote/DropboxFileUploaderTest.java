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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;

import junit.framework.TestCase;
import android.os.Environment;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;

public class DropboxFileUploaderTest extends TestCase {
    private static final String remotefile1 = "remotefile1";
    private static final String remotefile2 = "remotefile2";
    private static final String localpath1 = "data/com.todotxt.todotxttouch/tmp/test1.txt";
    private static final String localpath2 = "data/com.todotxt.todotxttouch/tmp/test2.txt";

    private static final String remoterev1 = "remoterev1";
    private static final String remoterev2 = "remoterev2";
    private static final String localrev1 = "localrev1";
    private static final String localrev2 = "localrev2";

    private File localFile1 = new File(
            Environment.getExternalStorageDirectory(), localpath1);
    private File localFile2 = new File(
            Environment.getExternalStorageDirectory(), localpath2);

    DropboxFile dbFile1;
    DropboxFile dbFile2;
    ArrayList<DropboxFile> dropboxFiles1;
    ArrayList<DropboxFile> dropboxFiles2;

    protected void setUp() throws Exception {
        if (localFile1.exists()) {
            localFile1.delete();
        }
        if (localFile2.exists()) {
            localFile2.delete();
        }
        dbFile1 = new DropboxFile(remotefile1, localFile1, localrev1);
        dbFile2 = new DropboxFile(remotefile2, localFile2, localrev2);
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

    private DropboxAPI.Entry create_metadata(String path, String rev) {
        return create_metadata(path, rev, false);
    }

    private DropboxAPI.Entry create_metadata(String path, String rev, boolean isDeleted) {
        DropboxAPI.Entry metadata = new DropboxAPI.Entry();
        metadata.path = path;
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

            public DropboxAPI.Entry putFile(String arg0,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                return create_metadata(remotefile1, localrev1);
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles1, false);

        uploader.pushFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                uploader.getStatus());
        assertEquals("Should have 1 file", 1, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
    }

    public void testRemoteFileDeleted() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(remotefile1, remoterev1, true);
            }

            public DropboxAPI.Entry putFile(String arg0,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                return create_metadata(remotefile1, localrev1);
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles1, false);

        uploader.pushFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                uploader.getStatus());
        assertEquals("Should have 1 file", 1, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
    }

    public void testRemoteFileExists() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(remotefile1, localrev1);
            }

            public DropboxAPI.Entry putFile(String arg0,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                return create_metadata(remotefile1, remoterev1);
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles1, false);

        uploader.pushFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                uploader.getStatus());
        assertEquals("Should have 1 file", 1, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals(remoterev1, dbFile1.getLoadedMetadata().rev);
    }

    public void testRemoteFileUppercase() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(remotefile1, localrev1);
            }

            public DropboxAPI.Entry putFile(String arg0,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                return create_metadata(remotefile1.toUpperCase(), remoterev1);
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles1, false);

        uploader.pushFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                uploader.getStatus());
        assertEquals("Should have 1 file", 1, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals(remoterev1, dbFile1.getLoadedMetadata().rev);
    }

    public void testRemoteFileConflict() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(remotefile1, remoterev1);
            }

            public DropboxAPI.Entry putFile(String arg0,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                fail("putFile should not be called");
                return null;
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles1, false);

        boolean thrown = false;
        try {
            uploader.pushFiles();
        } catch (RemoteConflictException e) {
            thrown = true;
        } catch (Throwable t) {
            fail("unexpected exception thown");
        }
        assertTrue("Should throw RemoteConflictException", thrown);
        assertEquals("Should have 1 file", 1, uploader.getFiles().size());
        assertEquals("Status should be CONFLICT", DropboxFileStatus.CONFLICT,
                dbFile1.getStatus());
        assertEquals(remoterev1, dbFile1.getLoadedMetadata().rev);
    }

    public void testRemoteFileOverwrite() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(remotefile1, remoterev1);
            }

            public DropboxAPI.Entry putFile(String arg0,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                return create_metadata(remotefile1, remoterev2);
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles1, true);

        uploader.pushFiles();

        assertEquals("Should have 1 file", 1, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals(remoterev2, dbFile1.getLoadedMetadata().rev);
    }

    public void testRemoteFileUploadError() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                return create_metadata(remotefile1, localrev1);
            }

            public DropboxAPI.Entry putFile(String arg0,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                throw new DropboxException("stub throw");
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles1, false);

        boolean thrown = false;
        try {
            uploader.pushFiles();
        } catch (RemoteException e) {
            thrown = true;
        } catch (Throwable t) {
            fail("unexpected exception thown");
        }
        assertTrue("Should throw RemoteException", thrown);
        assertEquals("Should have 1 file", 1, uploader.getFiles().size());
        assertEquals("Status should be FOUND", DropboxFileStatus.FOUND,
                dbFile1.getStatus());
    }

    public void testBothFilesMissing() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String arg0, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                throw notFoundException();
            }

            public DropboxAPI.Entry putFile(String file,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, localrev1);
                } else {
                    return create_metadata(remotefile2, localrev2);
                }
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles2, false);

        uploader.pushFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                uploader.getStatus());
        assertEquals("Should have 2 file", 2, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile2.getStatus());
    }

    public void testFirstRemoteFileExists() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, localrev1);
                } else {
                    throw notFoundException();
                }
            }

            public DropboxAPI.Entry putFile(String file,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, remoterev1);
                } else {
                    return create_metadata(remotefile2, remoterev2);
                }
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles2, false);

        uploader.pushFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                uploader.getStatus());
        assertEquals("Should have 2 file", 2, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile2.getStatus());
    }

    public void testSecondRemoteFileExists() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    throw notFoundException();
                } else {
                    return create_metadata(remotefile2, localrev2);
                }
            }

            public DropboxAPI.Entry putFile(String file,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, remoterev1);
                } else {
                    return create_metadata(remotefile2, remoterev2);
                }
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles2, false);

        uploader.pushFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                uploader.getStatus());
        assertEquals("Should have 2 file", 2, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile2.getStatus());
    }

    public void testBothRemoteFilesExist() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, localrev1);
                } else {
                    return create_metadata(remotefile2, localrev2);
                }
            }

            public DropboxAPI.Entry putFile(String file,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, remoterev1);
                } else {
                    return create_metadata(remotefile2, remoterev2);
                }
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles2, false);

        uploader.pushFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                uploader.getStatus());
        assertEquals("Should have 2 file", 2, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile2.getStatus());
    }

    public void testFirstRemoteFileError() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, localrev1);
                } else {
                    return create_metadata(remotefile2, localrev2);
                }
            }

            public DropboxAPI.Entry putFile(String file,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                if (file.equals(remotefile1)) {
                    throw new DropboxException("stub throw");
                } else {
                    fail("putFile should not be called for the second file");
                }
                return null;
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles2, false);

        boolean thrown = false;
        try {
            uploader.pushFiles();
        } catch (RemoteException e) {
            thrown = true;
        } catch (Throwable t) {
            fail("unexpected exception thown");
        }
        assertTrue("Should throw RemoteException", thrown);
        assertEquals("Should have 2 file", 2, uploader.getFiles().size());
        assertEquals("Status should be FOUND", DropboxFileStatus.FOUND,
                dbFile1.getStatus());
        assertEquals("Status should be FOUND", DropboxFileStatus.FOUND,
                dbFile2.getStatus());
    }

    public void testSecondRemoteFileError() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, localrev1);
                } else {
                    return create_metadata(remotefile2, localrev2);
                }
            }

            public DropboxAPI.Entry putFile(String file,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, remoterev1);
                } else {
                    throw new DropboxException("stub throw");
                }
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles2, false);

        boolean thrown = false;
        try {
            uploader.pushFiles();
        } catch (RemoteException e) {
            thrown = true;
        } catch (Throwable t) {
            fail("unexpected exception thown");
        }
        assertTrue("Should throw RemoteException", thrown);
        assertEquals("Should have 2 file", 2, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals("Status should be FOUND", DropboxFileStatus.FOUND,
                dbFile2.getStatus());
    }

    public void testFirstRemoteFileConflict() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, remoterev1);
                } else {
                    fail("metadata should not be called for second file");
                }
                return null;
            }

            public DropboxAPI.Entry putFile(String file,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                fail("putFile should not be called for either file");
                return null;
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles2, false);

        boolean thrown = false;
        try {
            uploader.pushFiles();
        } catch (RemoteConflictException e) {
            thrown = true;
        } catch (Throwable t) {
            fail("unexpected exception thown");
        }
        assertTrue("Should throw RemoteConflictException", thrown);
        assertEquals("Should have 2 file", 2, uploader.getFiles().size());
        assertEquals("Status should be CONFLICT", DropboxFileStatus.CONFLICT,
                dbFile1.getStatus());
        assertEquals("Status should be INITIALIZED", DropboxFileStatus.INITIALIZED,
                dbFile2.getStatus());
    }

    public void testSecondRemoteFileConflict() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, localrev1);
                } else {
                    return create_metadata(remotefile2, remoterev2);
                }
            }

            public DropboxAPI.Entry putFile(String file,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                fail("putFile should not be called for either file");
                return null;
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles2, false);

        boolean thrown = false;
        try {
            uploader.pushFiles();
        } catch (RemoteConflictException e) {
            thrown = true;
        } catch (Throwable t) {
            fail("unexpected exception thown");
        }
        assertTrue("Should throw RemoteConflictException", thrown);
        assertEquals("Should have 2 file", 2, uploader.getFiles().size());
        assertEquals("Status should be FOUND", DropboxFileStatus.FOUND,
                dbFile1.getStatus());
        assertEquals("Status should be CONFLICT", DropboxFileStatus.CONFLICT,
                dbFile2.getStatus());
    }

    public void testBothRemoteFilesOverwrite() {
        DropboxAPI<?> dropboxapi = new DropboxAPIStub() {
            public DropboxAPI.Entry metadata(String file, int arg1,
                    String arg2, boolean arg3, String arg4)
                    throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, remoterev1);
                } else {
                    return create_metadata(remotefile2, remoterev2);
                }
            }

            public DropboxAPI.Entry putFile(String file,
                    InputStream arg1, long arg2, String arg3,
                    ProgressListener arg4) throws DropboxException {
                if (file.equals(remotefile1)) {
                    return create_metadata(remotefile1, "newrev1");
                } else {
                    return create_metadata(remotefile2, "newrev2");
                }
            }
        };

        DropboxFileUploader uploader = new DropboxFileUploader(dropboxapi,
                dropboxFiles2, true);

        uploader.pushFiles();
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                uploader.getStatus());
        assertEquals("Should have 2 file", 2, uploader.getFiles().size());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile1.getStatus());
        assertEquals("Status should be SUCCESS", DropboxFileStatus.SUCCESS,
                dbFile2.getStatus());
        assertEquals("newrev1", dbFile1.getLoadedMetadata().rev);
        assertEquals("newrev2", dbFile2.getLoadedMetadata().rev);
    }

}
