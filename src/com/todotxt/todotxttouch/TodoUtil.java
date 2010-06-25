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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

import com.dropbox.client.DropboxClient;
import com.dropbox.client.DropboxClientHelper;
import com.dropbox.client.DropboxException;

public class TodoUtil {
	
	private final static String TAG = TodoUtil.class.getSimpleName();
	
	private final static Pattern prioPattern = Pattern.compile("\\(([A-Z])\\) (.*)");

	private final static Pattern contextPattern = Pattern.compile("@(\\w+)");

	private final static Pattern projectPattern = Pattern.compile("\\+(\\w+)");

	private final static Pattern tagPattern = Pattern.compile("#(\\w+)");

	public static Task createTask(int id, String line){
		//prio and text
		Matcher m = prioPattern.matcher(line);
		int prio = 0;
		String text = null;
		if(m.find()){
			prio = TaskHelper.parsePrio(m.group(1));
			text = m.group(2);
		}else{
			text = line;
		}
		//contexts
		m = contextPattern.matcher(text);
		List<String> contexts = new ArrayList<String>();
		while(m.find()){
			String context = m.group(1);
			contexts.add(context);
		}
		//projects
		m = projectPattern.matcher(text);
		List<String> projects = new ArrayList<String>();
		while(m.find()){
			String project = m.group(1);
			projects.add(project);
		}
		//tags
		m = tagPattern.matcher(text);
		List<String> tags = new ArrayList<String>();
		while(m.find()){
			String tag = m.group(1);
			tags.add(tag);
		}
		return new Task(id, prio, text.trim(), contexts, projects, tags);
	}

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

	public static void writeToFile(List<Task> tasks, File file){
		try{
			if(Util.isDeviceWritable()){
				FileWriter fw = new FileWriter(file);
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
	}

	public static void addTask(DropboxClient client, List<Task> tasks,
			String input) throws DropboxException {
		if(client != null && !Util.isEmpty(input)){
			Task task = createTask(tasks.size(), input);
			tasks.add(task);
			pushTasks(client, tasks);
		}
	}

	public static void pushTasks(DropboxClient client, List<Task> tasks)
			throws DropboxException {
		writeToFile(tasks, Constants.TODOFILETEMP);
		DropboxClientHelper.putFile(client, "/", Constants.TODOFILETEMP);
		//TODO verify
//		Constants.TODOFILE.delete();
		Constants.TODOFILETEMP.renameTo(Constants.TODOFILE);
	}
}
