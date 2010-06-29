package com.todotxt.todotxttouch.test;

import com.todotxt.todotxttouch.TodoTxtTouch;
import android.test.ActivityInstrumentationTestCase2;

public class TodoTxtTouchTest extends
		ActivityInstrumentationTestCase2<TodoTxtTouch> {

	public TodoTxtTouchTest() {
		super("com.todotxt.todotxttouch", TodoTxtTouch.class);
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_activity = this.getActivity();
	}
	
	@SuppressWarnings("unused") // TODO: Remove
	private TodoTxtTouch m_activity;
}
