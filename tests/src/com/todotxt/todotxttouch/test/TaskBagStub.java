package com.todotxt.todotxttouch.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.todotxt.todotxttouch.task.Filter;
import com.todotxt.todotxttouch.task.Priority;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;

public class TaskBagStub implements TaskBag {

	@Override
	public void archive() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAsTask(String input) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Task> getTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Task> getTasks(Filter<Task> filter, Comparator<Task> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<String> getProjects(boolean includeNone) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getContexts(boolean includeNone) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Priority> getPriorities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pushToRemote(boolean overwrite) {
		// TODO Auto-generated method stub
		pushToRemote(false, overwrite);
	}

	@Override
	public void pushToRemote(boolean overridePreference, boolean overwrite) {
		// TODO Auto-generated method stub
		++pushToRemoteCalled;

	}
	public int pushToRemoteCalled = 0;

	@Override
	public void pullFromRemote() {
		// TODO Auto-generated method stub
		pullFromRemote(false);
	}

	@Override
	public void pullFromRemote(boolean overridePreference) {
		// TODO Auto-generated method stub
		++pullFromRemoteCalled;
	}
	public int pullFromRemoteCalled = 0;

}
