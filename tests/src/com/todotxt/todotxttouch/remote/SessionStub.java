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

import java.util.Locale;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

public class SessionStub implements Session {

    @Override
    public String getAPIServer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccessTokenPair getAccessTokenPair() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccessType getAccessType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AppKeyPair getAppKeyPair() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContentServer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpClient getHttpClient() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProxyInfo getProxyInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getWebServer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLinked() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setRequestTimeout(HttpUriRequest arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sign(HttpRequest arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unlink() {
        // TODO Auto-generated method stub

    }

}
