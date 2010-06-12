package com.todotxt.todotxttouch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Environment;

import com.todotxt.todotxttouch.TodoTxtTouch.TaskAdapter;

public class TodoUtil {
	
	private final static Pattern prioPattern = Pattern.compile("\\(([A-Z])\\) (.*)");

	private final static Pattern contextPattern = Pattern.compile("@(\\w+)");
	
	private static File storageDirectory = null;
	
	public static File getStorageDirectory()
	{
		return storageDirectory;
	}

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
		}
		return new Task(id, prio, text.trim(), contexts);
	}

	public static ArrayList<Task> loadTasksFromUrl(Context cxt, String url)
			throws IOException {
		ArrayList<Task> items = new ArrayList<Task>();
		BufferedReader in = null;
		InputStream is = Util.getInputStreamFromUrl(url);
		try {
			in = new BufferedReader(new InputStreamReader(is));
			String line;
			int counter = 0;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0){
					items.add(createTask(counter, line));
				}
				counter++;
			}
		} finally {
			Util.closeStream(in);
			Util.closeStream(is);
		}
		return items;
	}

	/*
	public static ArrayList<Task> loadTasksFromFile()
			throws IOException {
		ArrayList<Task> items = new ArrayList<Task>();
		BufferedReader in = null;
		InputStream is = new FileInputStream(TODOFILE);
		try {
			in = new BufferedReader(new InputStreamReader(is));
			String line;
			int counter = 0;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					items.add(createTask(counter, line));
				}
				counter++;
			}
		} finally {
			Util.closeStream(in);
			Util.closeStream(is);
		}
		return items;
	}*/

	public static void setTasks(TaskAdapter adapter, List<Task> tasks){
		if (tasks != null && tasks.size() > 0) {
			Collections.sort(tasks, TaskHelper.byPrio);
			adapter.clear();
			for (int i = 0; i < tasks.size(); i++){
				adapter.add(tasks.get(i));
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	// Creates our applications storage directory if it has not yet been created
	// Stores data in /<external-device>/data/com.todotxt.todotxttouch/
	public static boolean createStorageDirectory()
	{
		if(storageDirectory == null)
		{
			if(Util.isDeviceWritable())
			{
				File rootDir = Environment.getExternalStorageDirectory();
				storageDirectory = new File(rootDir, "data/com.todotxt.todotxttouch/");
				
				if(storageDirectory.isDirectory() || storageDirectory.mkdirs())
					return true;
				else
				{
					storageDirectory = null;
					return false;
				}
			}
			else
				return false;
		}
		else
			return true;
	}

	/*
	public static void writeToFile(List<Task> tasks){
		try{
			if(Util.isDeviceWritable()){
				FileWriter fw = new FileWriter(TODOFILE);
				for(int i = 0; i < tasks.size(); ++i)
				{
					String fileFormat = TaskHelper.toFileFormat(tasks.get(i));
					fw.write(fileFormat);
					fw.write("\n");
				}
				fw.close();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}*/

}
