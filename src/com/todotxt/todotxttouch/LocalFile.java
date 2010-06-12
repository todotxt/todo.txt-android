package com.todotxt.todotxttouch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LocalFile {

// *** SINGLETON CODE START ***
	
	private static boolean m_init = false;
	private static LocalFile m_singleFile = null;
	private static Object m_lock = new Object();
	public static LocalFile getInstance() throws IOException {
		if(!m_init)
		{
			synchronized(m_lock) {
				if(!m_init && m_singleFile == null)
					m_singleFile = new LocalFile();
			}
			synchronized(m_lock) {
				m_init = true;
			}
		}
		return m_singleFile;
	}
	
// *** SINGLETON CODE END ***
	
	private File m_file = null;
	private ArrayList<Task> m_tasks = null;
	
	private LocalFile() throws IOException
	{
		if(TodoUtil.createStorageDirectory())
			initializeFile();
		else
			throw new IOException("Unable to create storage directory");
	}
	
	public void update() throws IOException
	{
		synchronized(m_file) {
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
		
		synchronized(m_tasks) {
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
	}
	
	public void addTask(String task)
	{
		synchronized(m_tasks) {
			m_tasks.add(TodoUtil.createTask(TodoUtil.getNextId(), task));
		}
	}
	
	public void save() throws IOException
	{
		if(Util.isDeviceWritable() && m_file.canWrite())
		{
			synchronized(m_file)
			{
				FileWriter fw = new FileWriter(m_file);
				for(int i = 0; i < m_tasks.size(); ++i)
				{
					fw.write(m_tasks.get(i).toFileFormat());
					fw.write("\n");
				}
				fw.close();
			}
			
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
				if(TodoUtil.createStorageDirectory())
				{
					m_file = new File(TodoUtil.getStorageDirectory(), "todo.txt");
					m_file.createNewFile();
				}
				else
					throw new IOException("Unable to create or access app directory");
			}
		}
		else
			throw new IOException("SD Storage is not readable");
	}
}