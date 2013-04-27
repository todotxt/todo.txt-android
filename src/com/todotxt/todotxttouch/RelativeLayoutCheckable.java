package com.todotxt.todotxttouch;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class RelativeLayoutCheckable extends RelativeLayout implements Checkable {


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

	        this.setBackgroundColor(checked ? getResources().getColor(R.color.activated_background) : getResources().getColor(R.color.abs__background_holo_light));
	    }

	    @Override
	    public void toggle() {
	        this.checked = !this.checked;
	    }
	}