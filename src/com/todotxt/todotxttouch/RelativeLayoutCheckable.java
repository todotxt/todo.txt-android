/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */
package com.todotxt.todotxttouch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class RelativeLayoutCheckable extends RelativeLayout implements
		Checkable {
	public RelativeLayoutCheckable(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private boolean checked;

	@Override
	public boolean isChecked() {
		return checked;
	}

	@Override
	public void setChecked(boolean checked) {
		this.checked = checked;
		View swipeView = this.findViewById(R.id.swipe_view);

		if (swipeView != null) {
			// FIXME: this is a hack to get a grey background when swiping
			// without breaking highlight when selected:
			this.setBackgroundColor(checked ? getResources().getColor(
					R.color.activated_background) : getResources().getColor(
					R.color.grey));
			swipeView.setBackgroundColor(checked ? getResources().getColor(
					android.R.color.transparent) : getResources().getColor(
					R.color.white));
		} else {
			this.setBackgroundColor(checked ? getResources().getColor(
					R.color.activated_background) : getResources().getColor(
					android.R.color.transparent));
		}
	}

	@Override
	public void toggle() {
		this.checked = !this.checked;
	}
}