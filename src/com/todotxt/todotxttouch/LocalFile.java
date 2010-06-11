package com.todotxt.todotxttouch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import android.os.Environment;

public class LocalFile {

	public static enum Type { TODO, DONE, TEMP };
	
	private static File m_storageDir = null;
	
	private File m_file = null;
	private ArrayList<Task> m_tasks = null;
	private Type m_type;
	
	public LocalFile(Type type)
	{
		m_type = type;
	}
	
	public void update() throws IOException
	{
		initializeFile();
		
		Scanner lineScan = new Scanner(m_file);
		lineScan.useDelimiter("\n");
		
		// Go through file line by line, adding tasks to the list
		m_tasks = new ArrayList<Task>();
		while(lineScan.hasNext())
		{
			String newTaskDesc = lineScan.next();
			newTaskDesc = newTaskDesc.trim();
			
			// Add to arraylist if string is not empty
			if(newTaskDesc.length() > 0)
			{
				Task t = TodoUtil.createTask(TodoUtil.getNextId(), newTaskDesc);				
				m_tasks.add(t);
			}
		}
	}
	
	public ArrayList<Task> getTasks() throws IOException
	{
		// Initializes the ArrayList and populates it with available tasks
		// Will only be run once from a getTasks call
		if(m_tasks == null)
			update();
		
		return m_tasks;
	}
	
	public void mergeTasks(ArrayList<Task> remoteSource)
	{
		ArrayList<Task> tmpNewValues = new ArrayList<Task>();
		
		for(int i = 0; i < remoteSource.size(); ++i)
		{
			String compVal = remoteSource.get(i).toFileFormat();
			if(compVal.length() == 0)
				continue;
			
			boolean newVal = true;
			for(int j = 0; j < m_tasks.size(); ++j)
			{
				if(m_tasks.get(j).toFileFormat().equalsIgnoreCase(compVal))
				{
					newVal = false;
					break;
				}
			}
			
			if(newVal)
				tmpNewValues.add(remoteSource.get(i));
		}
		
		m_tasks.addAll(tmpNewValues);
	}
	
	public void addTask(Task task)
	{
		m_tasks.add(task);
	}
	
	public void save() throws IOException
	{
		if(Util.isDeviceWritable() && m_file.canWrite())
		{
			FileWriter fw = new FileWriter(m_file);
			for(int i = 0; i < m_tasks.size(); ++i)
			{
				fw.write(m_tasks.get(i).toFileFormat());
				fw.write("\n");
			}
			fw.close();
			
			update();
		}
		else
			throw new IOException("Device or todo.txt is not writable");
	}
	
	private void initializeFile() throws IOException
	{
		if(Util.isDeviceReadable())
		{
			// Attempt to create file if it does not yet exist
			if(m_file == null)
			{	
				String fileName;
				switch(m_type)
				{
				case DONE:
					fileName = "done.txt";
					break;
				case TEMP:
					fileName = "tmp.txt";
					break;
				case TODO:
				default:
					fileName = "todo.txt";
					break;
				}
				
				if(createStorageDirectory())
				{
					m_file = new File(m_storageDir, fileName);
					m_file.createNewFile();
				}
				else
					throw new IOException("Unable to create or access app directory");
			}
		}
		else
			throw new IOException("SD Storage is not readable");
	}
	
	// Creates our applications storage directory if it has not yet been created
	// Stores data in /<external-device>/data/com.todotxt.todotxttouch/
	private static boolean createStorageDirectory()
	{
		if(m_storageDir == null)
		{
			if(Util.isDeviceWritable())
			{
				File rootDir = Environment.getExternalStorageDirectory();
				m_storageDir = new File(rootDir, "data/com.todotxt.todotxttouch/");
				
				if(m_storageDir.isDirectory() || m_storageDir.mkdirs())
					return true;
				else
				{
					m_storageDir = null;
					return false;
				}
			}
			else
				return false;
		}
		else
			return true;
	}
}