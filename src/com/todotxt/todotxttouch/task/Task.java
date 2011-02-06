/**
 *
 * Todo.txt Touch/src/com/todotxt/todotxttouch/task/Task.java
 *
 * Copyright (c) 2009-2011 mathias, Gina Trapani, Tim Barlotta
 *
 * LICENSE:
 *
 * This file is part of Todo.txt Touch, an Android app for managing your todo.txt file (http://todotxt.com).
 *
 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author mathias <mathias[at]x2[dot](none)>
 * @author Gina Trapani <ginatrapani[at]gmail[dot]com>
 * @author Tim Barlotta <tim[at]barlotta[dot]net>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2011 mathias, Gina Trapani, Tim Barlotta
 */
package com.todotxt.todotxttouch.task;

import com.todotxt.todotxttouch.Util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class Task implements Serializable {
    public static final char NO_PRIORITY = '-';
    private static final String COMPLETED = "x ";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final String originalText;
    private final char originalPriority;

    private long id;
    private char priority;
    private boolean deleted = false;
    private boolean completed = false;
    private String text;
    private String completionDate;
    private String prependedDate;
    private List<String> contexts;
    private List<String> projects;

    public Task(long id, String rawText) {
        this.id = id;
        this.init(rawText);
        this.originalPriority = priority;
        this.originalText = text;
    }

    public void update(String rawText) {
        this.init(rawText);
    }

    private void init(String rawText) {
        TextSplitter splitter = TextSplitter.getInstance();
        TextSplitter.SplitResult splitResult = splitter.split(rawText);
        this.priority = splitResult.priority;
        this.text = splitResult.text;
        this.prependedDate = splitResult.prependedDate;
        this.completed = splitResult.completed;
        this.completionDate = splitResult.completedDate;

        this.contexts = ContextParser.getInstance().parse(text);
        this.projects = ProjectParser.getInstance().parse(text);
        this.deleted = Util.isEmpty(text);
    }

    public char getOriginalPriority() {
        return originalPriority;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getText() {
        return text;
    }

    public long getId() {
        return id;
    }

    public void setPriority(char priority) {
        this.priority = priority;
    }

    public char getPriority() {
        return priority;
    }

    public List<String> getContexts() {
        return contexts;
    }

    public List<String> getProjects() {
        return projects;
    }

    public String getPrependedDate() {
        return prependedDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void markComplete(Date date) {
        if(!this.completed) {
            this.priority = Task.NO_PRIORITY;
            this.completionDate = new SimpleDateFormat(Task.DATE_FORMAT).format(date);
            this.deleted = false;
            this.completed = true;
        }
    }

    public void markIncomplete() {
        if(this.completed) {
            this.completionDate = "";
            this.completed = false;
        }
    }

    public void delete() {
        this.update("");
    }

    //TODO need a better solution (TaskFormatter?) here
    public String inScreenFormat() {
        StringBuilder sb = new StringBuilder();
        if(this.completed) {
            sb.append(COMPLETED).append(this.completionDate).append(" ");
            if(!Util.isEmpty(this.prependedDate)) {
                sb.append(this.prependedDate).append(" ");
            }
        }
        sb.append(this.text);
        return sb.toString();
    }

    public String inFileFormat() {
        StringBuilder sb = new StringBuilder();
        if(this.completed) {
            sb.append(COMPLETED).append(this.completionDate).append(" ");
            if(!Util.isEmpty(this.prependedDate)) {
                sb.append(this.prependedDate).append(" ");
            }
        }
        else {
            if(this.priority >= 'A' && this.priority <= 'Z') {
                sb.append("(").append(this.priority).append(") ");
            }
            if(!Util.isEmpty(this.prependedDate)) {
                sb.append(this.prependedDate).append(" ");
            }
        }
        sb.append(this.text);
        return sb.toString();
    }

    public void copyInto(Task destination) {
        destination.id = this.id;
        destination.init(this.inFileFormat());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        Task task = (Task)o;

        if(completed != task.completed) {
            return false;
        }
        if(deleted != task.deleted) {
            return false;
        }
        if(id != task.id) {
            return false;
        }
        if(priority != task.priority) {
            return false;
        }
        if(!contexts.equals(task.contexts)) {
            return false;
        }
        if(!prependedDate.equals(task.prependedDate)) {
            return false;
        }
        if(!projects.equals(task.projects)) {
            return false;
        }
        if(!text.equals(task.text)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int)(id ^ (id >>> 32));
        result = 31 * result + (int)priority;
        result = 31 * result + (deleted ? 1 : 0);
        result = 31 * result + (completed ? 1 : 0);
        result = 31 * result + text.hashCode();
        result = 31 * result + prependedDate.hashCode();
        result = 31 * result + contexts.hashCode();
        result = 31 * result + projects.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[id=").append(id).append("]");
        sb.append("[prio=").append(priority).append("]");
        sb.append("[text=").append(text).append("]");
        sb.append("[deleted=").append(this.isDeleted()).append("]");
        sb.append("[completed=").append(this.isCompleted()).append("]");
        // contexts
        sb.append("[contexts:");
        for(String cxt : contexts) {
            sb.append("[context=").append(cxt).append("]");
        }
        sb.append("]");
        // projects
        sb.append("[projects:");
        for(String prj : projects) {
            sb.append("[project=").append(prj).append("]");
        }
        sb.append("]");
        return sb.toString();
    }
}