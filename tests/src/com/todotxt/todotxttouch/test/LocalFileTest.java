/**
 *
 * Todo.txt Touch/tests/src/com/todotxt/todotxttouch/test/LocalFileTest.java
 *
 * Copyright (c) 2009-2011 Stephen Henderson
 *
 * LICENSE:
 *
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
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
 * @author Stephen Henderson <me[at]steveh[dot]ca>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Stephen Henderson
 */
package com.todotxt.todotxttouch.test;

import java.io.IOException;

import com.todotxt.todotxttouch.LocalFile;

import junit.framework.Assert;
import junit.framework.TestCase;

public class LocalFileTest extends TestCase {
	
	@Override
	protected void setUp() {
		LocalFile local = null;
		try {
			local = LocalFile.getInstance();
		} catch(IOException e) {
			Assert.fail(e.getMessage());
		}
		
		m_localFile = local;
	}
	
	
	/**
	 * Make sure we received a valid reference from LocalFile.getInstance
	 * in the setUp method.
	 */
	public void testPrecondition() {
		assertNotNull(m_localFile);
	}
	
	/**
	 * Ensure that reference we receive from LocalFile.getInstance() is the
	 * same as the one we received in setUp.
	 */
	public void testSingleton() {
		LocalFile newLocal = null;
		try {
			newLocal = LocalFile.getInstance();
		} catch(IOException e) {
			Assert.fail(e.getMessage());
		}
		
		assertEquals(m_localFile, newLocal);
	}
	
	public void testAddition() {
		
	}

	private LocalFile m_localFile = null;
	
}