/**
 * This file is part of Todo.txtndroid app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.tTodo.txttware: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt is Todo.txt the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt.  If not,Todo.txt//www.gnu.org/licenses/>.
 *
 * @author Todo.txt contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.task;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class TaskTest extends TestCase {

    public void testConstructor_simple() {
        String input = "A Simple test with no curve balls";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals(input, task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_simple_prependDate() throws Exception {
        String input = "A Simple test with no curve balls";
        Date date = new SimpleDateFormat("yyyyMMdd").parse("20110228");
        Task task = new Task(1, input, date);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals(input, task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("2011-02-28", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals("2011-02-28 A Simple test with no curve balls",
                task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withPriority() {
        String input = "(A) A priority test with no curve balls";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals("A priority test with no curve balls",
                task.getOriginalText());
        assertEquals("A priority test with no curve balls", task.getText());
        assertEquals(Priority.A, task.getOriginalPriority());
        assertEquals(Priority.A, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("A priority test with no curve balls",
                task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withPrependedDate() {
        String input = "2011-11-28 A priority test with no curve balls";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals("A priority test with no curve balls",
                task.getOriginalText());
        assertEquals("A priority test with no curve balls", task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("2011-11-28", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("A priority test with no curve balls",
                task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withPrependedDate_prependDate()
            throws Exception {
        String input = "2011-11-28 A priority test with no curve balls";
        Date date = new SimpleDateFormat("yyyyMMdd").parse("20110228");
        Task task = new Task(1, input, date);

        assertEquals(1, task.getId());
        assertEquals("A priority test with no curve balls",
                task.getOriginalText());
        assertEquals("A priority test with no curve balls", task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("2011-11-28", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("A priority test with no curve balls",
                task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withPriorityAndPrependedDate() {
        String input = "(C) 2011-11-28 A priority test with no curve balls";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals("A priority test with no curve balls",
                task.getOriginalText());
        assertEquals("A priority test with no curve balls", task.getText());
        assertEquals(Priority.C, task.getOriginalPriority());
        assertEquals(Priority.C, task.getPriority());
        assertEquals("2011-11-28", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("A priority test with no curve balls",
                task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withContext() {
        String input = "A simple test @phone";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals("A simple test @phone", task.getOriginalText());
        assertEquals("A simple test @phone", task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(1, task.getContexts().size());
        assertTrue(task.getContexts().contains("phone"));
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withMultipleContexts() {
        String input = "A simple test with @multiple @contexts @phone";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals("A simple test with @multiple @contexts @phone",
                task.getOriginalText());
        assertEquals("A simple test with @multiple @contexts @phone",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("multiple"));
        assertTrue(task.getContexts().contains("contexts"));
        assertTrue(task.getContexts().contains("phone"));
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withInterspersedContexts() {
        String input = "@simple test @with multiple contexts @phone";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals(input, task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("simple"));
        assertTrue(task.getContexts().contains("with"));
        assertTrue(task.getContexts().contains("phone"));
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withProject() {
        String input = "A simple test +myproject";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals("A simple test +myproject", task.getOriginalText());
        assertEquals("A simple test +myproject", task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(1, task.getProjects().size());
        assertTrue(task.getProjects().contains("myproject"));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withMultipleProjects() {
        String input = "A simple test with +multiple +projects +associated";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals("A simple test with +multiple +projects +associated",
                task.getOriginalText());
        assertEquals("A simple test with +multiple +projects +associated",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(3, task.getProjects().size());
        assertTrue(task.getProjects().contains("multiple"));
        assertTrue(task.getProjects().contains("projects"));
        assertTrue(task.getProjects().contains("associated"));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withInterspersedProjects() {
        String input = "A +simple test +with +multiple projects +myproject";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals("A +simple test +with +multiple projects +myproject",
                task.getOriginalText());
        assertEquals("A +simple test +with +multiple projects +myproject",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(4, task.getProjects().size());
        assertTrue(task.getProjects().contains("simple"));
        assertTrue(task.getProjects().contains("with"));
        assertTrue(task.getProjects().contains("multiple"));
        assertTrue(task.getProjects().contains("myproject"));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withLink() throws MalformedURLException {
        String input = "A simple test with an http://www.url.com";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals("A simple test with an http://www.url.com",
                task.getOriginalText());
        assertEquals("A simple test with an http://www.url.com", task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertEquals(1, task.getLinks().size());
        assertTrue(task.getLinks().contains(new URL("http://www.url.com")));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withMultipleLinks()
            throws MalformedURLException {
        String input = "A simple test with two http://www.urls.com http://www.another.one";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals(
                "A simple test with two http://www.urls.com http://www.another.one",
                task.getOriginalText());
        assertEquals(
                "A simple test with two http://www.urls.com http://www.another.one",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertEquals(2, task.getLinks().size());
        assertTrue(task.getLinks().contains(new URL("http://www.urls.com")));
        assertTrue(task.getLinks().contains(new URL("http://www.another.one")));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_withInterspersedLinks()
            throws MalformedURLException {
        String input = "A simple https://ww.url.com test with two http://www.urls.com http://www.another.one";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals(
                "A simple https://ww.url.com test with two http://www.urls.com http://www.another.one",
                task.getOriginalText());
        assertEquals(
                "A simple https://ww.url.com test with two http://www.urls.com http://www.another.one",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertEquals(3, task.getLinks().size());
        assertTrue(task.getLinks().contains(new URL("https://ww.url.com")));
        assertTrue(task.getLinks().contains(new URL("http://www.urls.com")));
        assertTrue(task.getLinks().contains(new URL("http://www.another.one")));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_complex() throws MalformedURLException {
        String input = "(D) 2011-12-01 A @complex test +with @multiple projects and @contexts +myproject and links http://www.link.com";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject and links http://www.link.com",
                task.getOriginalText());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject and links http://www.link.com",
                task.getText());
        assertEquals(Priority.D, task.getOriginalPriority());
        assertEquals(Priority.D, task.getPriority());
        assertEquals("2011-12-01", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("complex"));
        assertTrue(task.getContexts().contains("multiple"));
        assertTrue(task.getContexts().contains("contexts"));
        assertEquals(2, task.getProjects().size());
        assertTrue(task.getProjects().contains("with"));
        assertTrue(task.getProjects().contains("myproject"));
        assertEquals(1, task.getLinks().size());
        assertTrue(task.getLinks().contains(new URL("http://www.link.com")));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject and links http://www.link.com",
                task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_email() {
        String input = "Email me@steveh.ca about unit testing";

        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals(input, task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertEquals(Collections.<String> emptyList(), task.getLinks());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(input, task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_empty() {
        String input = "";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals(input, task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertEquals(Collections.<String> emptyList(), task.getLinks());
        assertTrue(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("", task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_null() {
        Task task = new Task(1, null);

        assertEquals(1, task.getId());
        assertEquals("", task.getOriginalText());
        assertEquals("", task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertEquals(Collections.<String> emptyList(), task.getLinks());
        assertTrue(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("", task.inScreenFormat());
        assertEquals("", task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testConstructor_completedTask() throws MalformedURLException {
        String input = "x 2011-02-28 A @complex test +with @multiple projects and @contexts +myproject with http://www.link.com";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject with http://www.link.com",
                task.getOriginalText());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject with http://www.link.com",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("complex"));
        assertTrue(task.getContexts().contains("multiple"));
        assertTrue(task.getContexts().contains("contexts"));
        assertEquals(2, task.getProjects().size());
        assertTrue(task.getProjects().contains("with"));
        assertTrue(task.getProjects().contains("myproject"));
        assertEquals(1, task.getLinks().size());
        assertTrue(task.getLinks().contains(new URL("http://www.link.com")));
        assertFalse(task.isDeleted());
        assertTrue(task.isCompleted());
        assertEquals(
                "x 2011-02-28 A @complex test +with @multiple projects and @contexts +myproject with http://www.link.com",
                task.inScreenFormat());
        assertEquals(input, task.inFileFormat());
        assertEquals("2011-02-28", task.getCompletionDate());
    }

    public void testConstructor_completedTask_upperCase()
            throws MalformedURLException {
        String input = "X 2011-02-28 A @complex test +with @multiple projects and @contexts +myproject with http://www.link.com";
        Task task = new Task(1, input);

        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject with http://www.link.com",
                task.getOriginalText());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject with http://www.link.com",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("complex"));
        assertTrue(task.getContexts().contains("multiple"));
        assertTrue(task.getContexts().contains("contexts"));
        assertEquals(2, task.getProjects().size());
        assertTrue(task.getProjects().contains("with"));
        assertTrue(task.getProjects().contains("myproject"));
        assertEquals(1, task.getLinks().size());
        assertTrue(task.getLinks().contains(new URL("http://www.link.com")));
        assertFalse(task.isDeleted());
        assertTrue(task.isCompleted());
        assertEquals(
                "x 2011-02-28 A @complex test +with @multiple projects and @contexts +myproject with http://www.link.com",
                task.inScreenFormat());
        assertEquals(
                "x 2011-02-28 A @complex test +with @multiple projects and @contexts +myproject with http://www.link.com",
                task.inFileFormat());
        assertEquals("2011-02-28", task.getCompletionDate());
    }

    public void testEqualsAndHashCode_simple() {
        String input = "(D) 2011-12-01 A @complex test with @multiple projects and @contexts myproject";
        Task task1 = new Task(1, input);
        Task task2 = new Task(1, input);

        assertTrue(task1.equals(task1));
        assertTrue(task2.equals(task2));
        assertTrue(task1.equals(task2));
        assertTrue(task2.equals(task1));
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    public void testCopyInto() {
        String input1 = "(D) 2011-12-01 A @complex test with @multiple projects and @contexts myproject";
        String input2 = "A simple text input";
        Task task1 = new Task(1, input1);
        Task task2 = new Task(2, input2);
        task1.copyInto(task2);

        assertEquals(task1.getId(), task2.getId());
        assertFalse(task1.getOriginalText().equals(task2.getOriginalText()));
        assertEquals(
                "A @complex test with @multiple projects and @contexts myproject",
                task1.getOriginalText());
        assertEquals("A simple text input", task2.getOriginalText());
        assertEquals(task1.getText(), task2.getText());
        assertFalse(task1.getOriginalPriority() == task2.getOriginalPriority());
        assertEquals(Priority.D, task1.getOriginalPriority());
        assertEquals(Priority.NONE, task2.getOriginalPriority());
        assertEquals(task1.getPriority(), task2.getPriority());
        assertEquals(task1.getPrependedDate(), task2.getPrependedDate());
        assertEquals(task1.getContexts(), task2.getContexts());
        assertEquals(task1.getProjects(), task2.getProjects());
        assertEquals(task1.isDeleted(), task2.isDeleted());
        assertEquals(task1.isCompleted(), task2.isCompleted());
        assertEquals(task1, task2);
        assertEquals(
                "A @complex test with @multiple projects and @contexts myproject",
                task1.inScreenFormat());
        assertEquals(
                "A @complex test with @multiple projects and @contexts myproject",
                task2.inScreenFormat());
        assertEquals(input1, task1.inFileFormat());
        assertEquals(input1, task2.inFileFormat());
        assertEquals("", task1.getCompletionDate());
        assertEquals(task1.getCompletionDate(), task2.getCompletionDate());
    }

    public void testMarkComplete() throws Exception {
        String input = "(D) 2011-12-01 A @complex test +with @multiple projects and @contexts +myproject";
        Task task = new Task(1, input);
        Date date = new SimpleDateFormat("yyyyMMdd").parse("20110228");
        task.markComplete(date);

        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getOriginalText());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getText());
        assertEquals(Priority.D, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("2011-12-01", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("complex"));
        assertTrue(task.getContexts().contains("multiple"));
        assertTrue(task.getContexts().contains("contexts"));
        assertEquals(2, task.getProjects().size());
        assertTrue(task.getProjects().contains("with"));
        assertTrue(task.getProjects().contains("myproject"));
        assertFalse(task.isDeleted());
        assertTrue(task.isCompleted());
        assertEquals(
                "x 2011-02-28 2011-12-01 A @complex test +with @multiple projects and @contexts +myproject",
                task.inScreenFormat());
        assertEquals(
                "x 2011-02-28 2011-12-01 A @complex test +with @multiple projects and @contexts +myproject",
                task.inFileFormat());
        assertEquals("2011-02-28", task.getCompletionDate());
    }

    public void testMarkComplete_twice() throws Exception {
        String input = "(D) 2011-12-01 A @complex test +with @multiple projects and @contexts +myproject";
        Task task = new Task(1, input);
        Date date = new SimpleDateFormat("yyyyMMdd").parse("20110228");
        task.markComplete(date);
        task.markComplete(date);

        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getOriginalText());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getText());
        assertEquals(Priority.D, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("2011-12-01", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("complex"));
        assertTrue(task.getContexts().contains("multiple"));
        assertTrue(task.getContexts().contains("contexts"));
        assertEquals(2, task.getProjects().size());
        assertTrue(task.getProjects().contains("with"));
        assertTrue(task.getProjects().contains("myproject"));
        assertFalse(task.isDeleted());
        assertTrue(task.isCompleted());
        assertEquals(
                "x 2011-02-28 2011-12-01 A @complex test +with @multiple projects and @contexts +myproject",
                task.inScreenFormat());
        assertEquals(
                "x 2011-02-28 2011-12-01 A @complex test +with @multiple projects and @contexts +myproject",
                task.inFileFormat());
        assertEquals("2011-02-28", task.getCompletionDate());
    }

    public void testMarkIncomplete() {
        String input = "x 2011-02-28 A @complex test +with @multiple projects and @contexts +myproject";
        Task task = new Task(1, input);

        task.markIncomplete();
        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getOriginalText());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("complex"));
        assertTrue(task.getContexts().contains("multiple"));
        assertTrue(task.getContexts().contains("contexts"));
        assertEquals(2, task.getProjects().size());
        assertTrue(task.getProjects().contains("with"));
        assertTrue(task.getProjects().contains("myproject"));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.inScreenFormat());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testMarkIncomplete_twice() {
        String input = "x 2011-02-28 A @complex test +with @multiple projects and @contexts +myproject";
        Task task = new Task(1, input);

        task.markIncomplete();
        task.markIncomplete();
        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getOriginalText());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("complex"));
        assertTrue(task.getContexts().contains("multiple"));
        assertTrue(task.getContexts().contains("contexts"));
        assertEquals(2, task.getProjects().size());
        assertTrue(task.getProjects().contains("with"));
        assertTrue(task.getProjects().contains("myproject"));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.inScreenFormat());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testMarkIncomplete_withPrependedDate() {
        String input = "x 2011-02-28 2011-02-17 A @complex test +with @multiple projects and @contexts +myproject";
        Task task = new Task(1, input);

        task.markIncomplete();
        task.markIncomplete();
        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getOriginalText());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("2011-02-17", task.getPrependedDate());
        assertEquals(3, task.getContexts().size());
        assertTrue(task.getContexts().contains("complex"));
        assertTrue(task.getContexts().contains("multiple"));
        assertTrue(task.getContexts().contains("contexts"));
        assertEquals(2, task.getProjects().size());
        assertTrue(task.getProjects().contains("with"));
        assertTrue(task.getProjects().contains("myproject"));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(
                "A @complex test +with @multiple projects and @contexts +myproject",
                task.inScreenFormat());
        assertEquals(
                "2011-02-17 A @complex test +with @multiple projects and @contexts +myproject",
                task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testDelete_simple() {
        String input = "(D) 2011-12-01 A @complex test with @multiple projects and @contexts myproject";
        Task task = new Task(1, input);
        task.delete();

        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test with @multiple projects and @contexts myproject",
                task.getOriginalText());
        assertEquals("", task.getText());
        assertEquals(Priority.D, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertTrue(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("", task.inScreenFormat());
        assertEquals("", task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testDelete_twice() {
        String input = "(D) 2011-12-01 A @complex test with @multiple projects and @contexts myproject";
        Task task = new Task(1, input);
        task.delete();
        task.delete();

        assertEquals(1, task.getId());
        assertEquals(
                "A @complex test with @multiple projects and @contexts myproject",
                task.getOriginalText());
        assertEquals("", task.getText());
        assertEquals(Priority.D, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertTrue(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("", task.inScreenFormat());
        assertEquals("", task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testInFileFormat_simple() {
        String input = "A Simple test with no curve balls";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_withPriority() {
        String input = "(A) Simple test with a priority";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_withPrependedDate() {
        String input = "2011-01-29 Simple test with a prepended date";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_withPriorityAndPrependedDate() {
        String input = "(B) 2011-01-29 Simple test with a priority and a prepended date";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_withContext() {
        String input = "Simple test with a context @home";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_withMultipleContexts() {
        String input = "Simple test @phone @home";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_withInterspersedContexts() {
        String input = "Simple @phone test @home";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_withProject() {
        String input = "Simple test with a context home";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_withMultipleProjects() {
        String input = "Simple test phone home";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_withInterspersedProjects() {
        String input = "Simple phone test home";
        Task task = new Task(0, input);

        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_complex() {
        String input = "(D) 2011-12-01 A @complex test with @multiple projects and @contexts myproject";
        Task task = new Task(1, input);
        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_empty() {
        String input = "";
        Task task = new Task(1, input);
        assertEquals(input, task.inFileFormat());
    }

    public void testInFileFormat_null() {
        String input = null;
        Task task = new Task(1, input);
        assertEquals("", task.inFileFormat());
    }

    public void testSetPriority_noExisting() {
        String input = "A Simple test with no curve balls";
        Task task = new Task(1, input);
        task.setPriority(Priority.C);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals(input, task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.C, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("A Simple test with no curve balls", task.inScreenFormat());
        assertEquals("(C) A Simple test with no curve balls",
                task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testSetPriority_noExistingWithPrependedDate() {
        String input = "2011-11-01 A Simple test with no curve balls";
        Task task = new Task(1, input);
        task.setPriority(Priority.B);

        assertEquals(1, task.getId());
        assertEquals("A Simple test with no curve balls",
                task.getOriginalText());
        assertEquals("A Simple test with no curve balls", task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.B, task.getPriority());
        assertEquals("2011-11-01", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("A Simple test with no curve balls", task.inScreenFormat());
        assertEquals("(B) 2011-11-01 A Simple test with no curve balls",
                task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testSetPriority_existing() {
        String input = "(A) A Simple test with no curve balls";
        Task task = new Task(1, input);
        task.setPriority(Priority.C);

        assertEquals(1, task.getId());
        assertEquals("A Simple test with no curve balls",
                task.getOriginalText());
        assertEquals("A Simple test with no curve balls", task.getText());
        assertEquals(Priority.A, task.getOriginalPriority());
        assertEquals(Priority.C, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("A Simple test with no curve balls", task.inScreenFormat());
        assertEquals("(C) A Simple test with no curve balls",
                task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testSetPriority_existingWithPrependedDate() {
        String input = "(A) 2011-11-01 A Simple test with no curve balls";
        Task task = new Task(1, input);
        task.setPriority(Priority.C);

        assertEquals(1, task.getId());
        assertEquals("A Simple test with no curve balls",
                task.getOriginalText());
        assertEquals("A Simple test with no curve balls", task.getText());
        assertEquals(Priority.A, task.getOriginalPriority());
        assertEquals(Priority.C, task.getPriority());
        assertEquals("2011-11-01", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("A Simple test with no curve balls", task.inScreenFormat());
        assertEquals("(C) 2011-11-01 A Simple test with no curve balls",
                task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testUpdate_simpleToSimple() {
        String expectedResult = "Another simple test with no curve balls";
        String input = "A Simple test with no curve balls";
        Task task = new Task(1, input);
        task.update(expectedResult);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals(expectedResult, task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(expectedResult, task.inScreenFormat());
        assertEquals(expectedResult, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testUpdate_simpleToWithContexts() {
        String expectedResult = "Another simple @test with @contexts";
        String input = "A Simple test with no curve balls";
        Task task = new Task(1, input);
        task.update(expectedResult);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals(expectedResult, task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(2, task.getContexts().size());
        assertTrue(task.getContexts().contains("test"));
        assertTrue(task.getContexts().contains("contexts"));
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(expectedResult, task.inScreenFormat());
        assertEquals(expectedResult, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testUpdate_simpleToWithProjects() {
        String expectedResult = "Another simple +test with +projects";
        String input = "A Simple test with no curve balls";
        Task task = new Task(1, input);
        task.update(expectedResult);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals(expectedResult, task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(2, task.getProjects().size());
        assertTrue(task.getProjects().contains("test"));
        assertTrue(task.getProjects().contains("projects"));
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals(expectedResult, task.inScreenFormat());
        assertEquals(expectedResult, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testUpdate_simpleToPriority() {
        String expectedResult = "(A) Another simple test with a priority curve ball";
        String input = "A Simple test with no curve balls";
        Task task = new Task(1, input);
        task.update(expectedResult);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals("Another simple test with a priority curve ball",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.A, task.getPriority());
        assertEquals("", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("Another simple test with a priority curve ball",
                task.inScreenFormat());
        assertEquals(expectedResult, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testSetText_simpleToPrependedDate() {
        String expectedResult = "2011-10-01 Another simple test with a prepended date curve ball";
        String input = "A Simple test with no curve balls";
        Task task = new Task(1, input);
        task.update(expectedResult);

        assertEquals(1, task.getId());
        assertEquals(input, task.getOriginalText());
        assertEquals("Another simple test with a prepended date curve ball",
                task.getText());
        assertEquals(Priority.NONE, task.getOriginalPriority());
        assertEquals(Priority.NONE, task.getPriority());
        assertEquals("2011-10-01", task.getPrependedDate());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertFalse(task.isDeleted());
        assertFalse(task.isCompleted());
        assertEquals("Another simple test with a prepended date curve ball",
                task.inScreenFormat());
        assertEquals(expectedResult, task.inFileFormat());
        assertEquals("", task.getCompletionDate());
    }

    public void testFilterInit_nulls() {
        Task task = new Task(1, "");
        task.initWithFilters(null, null, null);

        assertEquals(Priority.NONE, task.getPriority());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
    }

    public void testFilterInit_empty() {
        Task task = new Task(1, "");
        task.initWithFilters(new ArrayList<Priority>(), new ArrayList<String>(),
                new ArrayList<String>());

        assertEquals(Priority.NONE, task.getPriority());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
    }

    public void testFilterInit_onePriority() {
        ArrayList<Priority> ps = new ArrayList<Priority>();
        ps.add(Priority.A);

        Task task = new Task(1, "");
        task.initWithFilters(ps, new ArrayList<String>(), new ArrayList<String>());

        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());

        assertEquals("initialized with A", Priority.A, task.getPriority());

        ps.remove(0);
        ps.add(Priority.D);

        task = new Task(1, "");
        task.initWithFilters(ps, new ArrayList<String>(), new ArrayList<String>());

        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());

        assertEquals("initialized with D", Priority.D, task.getPriority());
    }

    public void testFilterInit_multiPriority() {
        ArrayList<Priority> ps = new ArrayList<Priority>();
        ps.add(Priority.A);
        ps.add(Priority.D);

        Task task = new Task(1, "");
        task.initWithFilters(ps, new ArrayList<String>(), new ArrayList<String>());

        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertEquals(Priority.NONE, task.getPriority());
    }

    public void testFilterInit_oneProject() {
        ArrayList<String> projects = new ArrayList<String>();
        projects.add("fred");

        Task task = new Task(1, "");
        task.initWithFilters(null, null, projects);

        assertEquals(Priority.NONE, task.getPriority());
        assertEquals(Collections.<String> emptyList(), task.getContexts());

        List<String> tp = task.getProjects();
        assertEquals("project count", 1, tp.size());
        assertEquals("fred", tp.get(0));
    }

    public void testFilterInit_multiProject() {
        ArrayList<String> projects = new ArrayList<String>();
        projects.add("fred");
        projects.add("barney");

        Task task = new Task(1, "");
        task.initWithFilters(null, null, projects);

        assertEquals(Priority.NONE, task.getPriority());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
    }

    public void testFilterInit_oneContext() {
        ArrayList<String> contexts = new ArrayList<String>();
        contexts.add("quarry");

        Task task = new Task(1, "");
        task.initWithFilters(null, contexts, null);

        assertEquals(Priority.NONE, task.getPriority());
        assertEquals(Collections.<String> emptyList(), task.getProjects());

        List<String> tc = task.getContexts();
        assertEquals("context count", 1, tc.size());
        assertEquals("quarry", tc.get(0));
    }

    public void testFilterInit_multiContext() {
        ArrayList<String> contexts = new ArrayList<String>();
        contexts.add("quarry");
        contexts.add("home");

        Task task = new Task(1, "");
        task.initWithFilters(null, contexts, null);

        assertEquals(Priority.NONE, task.getPriority());
        assertEquals(Collections.<String> emptyList(), task.getProjects());
        assertEquals(Collections.<String> emptyList(), task.getContexts());
    }
}
