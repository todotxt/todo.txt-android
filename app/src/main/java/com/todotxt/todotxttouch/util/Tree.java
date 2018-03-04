/**
 * This file is part of Todo.txt for Android, an app for managing your todo.txt file (http://todotxt.com).
 *
 * Copyright (c) 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 *
 * LICENSE:
 *
 * Todo.txt for Android is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * Todo.txt for Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Todo.txt for Android. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Todo.txt for Android's source code is available at https://github.com/ginatrapani/todo.txt-android
 *
 * @author Todo.txt for Android contributors <todotxt@yahoogroups.com>
 * @license http://www.gnu.org/licenses/gpl.html
 * @copyright 2009-2013 Todo.txt for Android contributors (http://todotxt.com)
 */

package com.todotxt.todotxttouch.util;

import java.util.ArrayList;
import java.util.List;

public class Tree<E> {
    private Tree<E> parent = null;
    private List<Tree<E>> children = null;
    private E data;

    public Tree(E data) {
        this.data = data;
    }

    public Tree(Tree<E> parent, E data) {
        this.parent = parent;
        this.data = data;
    }

    public Tree<E> addChild(Tree<E> child) {
        if (children == null) {
            children = new ArrayList<Tree<E>>();
        }

        children.add(child);
        child.parent = this;

        return child;
    }

    public Tree<E> addChild(E data) {
        Tree<E> child = new Tree<E>(data);

        return addChild(child);
    }

    public E getData() {
        return data;
    }

    public Tree<E> getParent() {
        return parent;
    }

    public boolean isLoaded() {
        return children != null;
    }

    public void setLoaded() {
        if (children == null) {
            children = new ArrayList<Tree<E>>();
        }
    }

    public List<Tree<E>> getChildren() {
        return children;
    }

    public boolean contains(Tree<E> child) {
        if (children == null) {
            return false;
        }

        return children.contains(child);
    }

    public boolean contains(E data) {
        if (children == null) {
            return false;
        }

        for (Tree<E> child : children) {
            if (child.getData().equals(data)) {
                return true;
            }
        }

        return false;
    }

    public Tree<E> getChild(int position) {
        return children.get(position);
    }
}
