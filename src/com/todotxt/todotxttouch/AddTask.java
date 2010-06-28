package com.todotxt.todotxttouch;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddTask extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_task);
		
		Button addBtn = (Button)findViewById(R.id.addTask);
		addBtn.setOnClickListener(m_addListener);
	}
	
	private OnClickListener m_addListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			EditText taskText = (EditText)findViewById(R.id.newTaskText);
			String val = taskText.getText().toString();
			
			try {
				LocalFile localFile = LocalFile.getInstance();
				localFile.addTask(val);
			} catch (IOException e) {
				Log.e("addTask.onClick", e.getMessage());
			} finally {
				Intent mainAct = new Intent(getBaseContext(), TodoTxtTouch.class);
				startActivity(mainAct);
			}
		}
	};

}
