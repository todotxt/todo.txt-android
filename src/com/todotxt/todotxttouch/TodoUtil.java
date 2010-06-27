package com.todotxt.todotxttouch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class TodoUtil {
	
	private final static String TAG = TodoUtil.class.getSimpleName();
	
	public static ArrayList<Task> loadTasksFromUrl(Context cxt, String url)
			throws IOException {
		InputStream is = Util.getInputStreamFromUrl(url);
		return loadTasksFromStream(cxt, is);
	}

	public static ArrayList<Task> loadTasksFromStream(Context cxt, InputStream is)
			throws IOException {
		ArrayList<Task> items = new ArrayList<Task>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(is));
			String line;
			int counter = 0;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					items.add(TaskHelper.createTask(counter, line));
				}
				counter++;
			}
		} finally {
			Util.closeStream(in);
			Util.closeStream(is);
		}
		return items;
	}

	public static ArrayList<Task> loadTasksFromFile()
			throws IOException {
		ArrayList<Task> items = new ArrayList<Task>();
		BufferedReader in = null;
		InputStream is = new FileInputStream(Constants.TODOFILE);
		try {
			in = new BufferedReader(new InputStreamReader(is));
			String line;
			int counter = 0;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					items.add(TaskHelper.createTask(counter, line));
				}
				counter++;
			}
		} finally {
			Util.closeStream(in);
			Util.closeStream(is);
		}
		return items;
	}

	public static void writeToFile(List<Task> tasks, File file){
		try{
			if(!Util.isDeviceWritable()){
				throw new IOException("Device Not Writable!");
			}
			FileWriter fw = new FileWriter(file);
			for(int i = 0; i < tasks.size(); ++i)
			{
				String fileFormat = TaskHelper.toFileFormat(tasks.get(i));
				fw.write(fileFormat);
				fw.write("\n");
			}
			fw.close();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

}
