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