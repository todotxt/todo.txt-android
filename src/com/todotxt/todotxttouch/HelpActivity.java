/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/HelpActivity.java
 *
 * Copyright (c) 2009-2011 Hrayr Artunyan
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
 * @author Hrayr Artunyan <hrayr[dot]artunyan[at]gmail[dot]com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Hrayr Artunyan
 */
package com.todotxt.todotxttouch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class HelpActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	     getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
	                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		setContentView(R.layout.help);
	}
	
	public void onCloseClick(View v){
		finish();
	}
}
