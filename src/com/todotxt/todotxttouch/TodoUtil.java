package com.todotxt.todotxttouch;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.todotxt.todotxttouch.TodoTxtTouch.TaskAdapter;

import android.content.Context;
import android.util.Log;

public class TodoUtil {
	
	private final static String TAG = TodoUtil.class.getSimpleName();

	private final static Pattern prioPattern = Pattern.compile("\\(([A-Z])\\) (.*)");

	private final static Pattern contextPattern = Pattern.compile("@(\\S+)");

	public static Task createTask(int id, String line){
		Matcher m = prioPattern.matcher(line);
		int prio = 0;
		String text = null;
		if(m.find()){
			prio = TaskHelper.parsePrio(m.group(1));
			text = m.group(2);
		}else{
			text = line;
		}
		m = contextPattern.matcher(text);
		List<String> contexts = new ArrayList<String>();
		while(m.find()){
			String context = m.group(1);
			contexts.add(context);
			text = text.replace("@"+context, "");
		}
		return new Task(id, prio, text, contexts);
	}

	public static ArrayList<Task> loadTasksFromUrl(Context cxt, String url)
			throws IOException {
		ArrayList<Task> items = new ArrayList<Task>();
		BufferedReader in = null;
		InputStream is = Util.getInputStreamFromUrl(url);
		try {
			in = new BufferedReader(new InputStreamReader(is));
			String line;
			int counter = 1;
			while ((line = in.readLine()) != null) {
				items.add(createTask(counter, line));
				counter++;
			}
		} finally {
			Util.closeStream(in);
			Util.closeStream(is);
		}
		return items;
	}

	public static void writeTasks(List<Task> items){
		Collections.sort(items, TaskHelper.byId);
		FileWriter fw = null;
		try {
			//TODO dropbox api?
			fw = new FileWriter("DUMMY");
//			BufferedWriter bw = new BufferedWriter(fw);
			for (Task item : items) {
//				bw.write(item.toFileFormat());
				Log.v(TAG, "Writing: "+item.toFileFormat());
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}finally{
			try {
				if(fw != null){
					fw.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public static void setTasks(TaskAdapter adapter, List<Task> tasks){
		if (tasks != null && tasks.size() > 0) {
			adapter.clear();
			for (int i = 0; i < tasks.size(); i++){
				adapter.add(tasks.get(i));
			}
			adapter.notifyDataSetChanged();
		}
	}

}
