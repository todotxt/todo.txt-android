/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/ui/BlurDialog.java
 *
 * Copyright (c) 2009-2011 Florian Behr
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
 * @author Florian Behr <mail[at]florianbehr[dot]de>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 Florian Behr
 */
package com.todotxt.todotxttouch.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BlurDialog extends Activity {

	public interface OnFinishClickListener {
		public void onClick();
	}

	private static View view;
	private static Button finishButton;
	private static OnFinishClickListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		setContentView(view);

		if (finishButton != null && listener != null) {
			finishButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					listener.onClick();
					finish();

				}

			});
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		setContentView(new View(this));
	}

	public static class Builder {
		private Context cont;

		public Builder(Context context) {
			cont = context;
		}

		/**
		 * Set a view object as the view of the dialog
		 * 
		 * @param v
		 */
		public void setView(View v) {
			view = v;
		}

		/**
		 * Set view from a resource ID
		 * 
		 * @param resid
		 *            Resource ID
		 */
		public void setView(int resid) {
			view = View.inflate(cont, resid, null);
		}

		/**
		 * Show the dialog
		 */
		public void show() {
			Intent i = new Intent(cont, BlurDialog.class);
			cont.startActivity(i);
		}

		/**
		 * Set a button that closes the dialog and has a custom click handler to
		 * do things right before closing
		 * 
		 * @param btn
		 *            Button that will finish (close) the dialog
		 * @param list
		 *            Click listener for things to do right before closing
		 */
		public void setFinishButton(Button btn, OnFinishClickListener list) {
			finishButton = btn;
			listener = list;
		}

		/**
		 * Set a button that closes the dialog, but can not handle additional
		 * events
		 * 
		 * @param btn
		 *            Button that will finish (close) the dialog
		 */
		public void setFinishButton(Button btn) {			
			OnFinishClickListener list = new OnFinishClickListener() {

				@Override
				public void onClick() {

				}
			};
			setFinishButton(btn, list);
		}
		/**
		 * Set a button that closes the dialog, but can not handle additional
		 * events
		 * 
		 * @param resid
		 *            Resource id of a button
		 */
		public void setFinishButton(int resid) {
			Button btn = (Button) view.findViewById(resid);
			setFinishButton(btn);
		}
		/**
		 * Set a button that closes the dialog and has a custom click handler to
		 * do things right before closing
		 * 
		 * @param resid
		 *            Resource id of a button
		 * @param list
		 *            Click listener for things to do right before closing
		 */
		public void setFinishButton(int resid, OnFinishClickListener list) {
			Button btn = (Button) view.findViewById(resid);
			setFinishButton(btn, list);
			
		}

	}
}
