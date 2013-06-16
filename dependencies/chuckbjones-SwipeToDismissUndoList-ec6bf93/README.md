SwipeToDismissUndoList
=======================

The SwipeToDismissUndoList is a library to add swipe to dismiss functionality to
a `ListView` and undo deletions again. The lib is based on 
[Jake Wharton's SwipeToDismissNOA](https://github.com/JakeWharton/SwipeToDismissNOA)
that is based on [Roman Nurik's SwipeToDismiss sample](https://gist.github.com/romannurik/2980593).

The code is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

You can view a demonstration of this lib in the [Demonstration App](https://play.google.com/store/apps/details?id=de.timroes.swipetodismiss.demo).
The source code of this demonstration app can be found in the [SwipteToDismissUndoDemo](https://github.com/timroes/SwipeToDismissUndoDemo) project.

Changes
-------

*2013-05-28* Introduced `setSwipeDirection` and improved detection of swipes.

*2013-05-13* Use `AbsListView` instead of `ListView` to make this library also available for GridViews

**Changes required:** You need to change the parameter in the `onDismiss` method to `AbsListView` (see below).

*2013-03-22* Added minimum SDK version to manifest

*2013-02-24* Properly discard undo items (BUG FIX)

Usage
-----

[Add this project](http://developer.android.com/tools/projects/projects-cmdline.html#ReferencingLibraryProject)
as an Android library to your project. The project should work from API level 3 upwards. If you find
that this isn't true (might be, that I missed some newer methods), please inform me about that :)

To use the list create a regular `ListView` (e.g. via a `ListActivity`) and wrap
it up in the `SwipeDismissList` of this lib:

```java
ListView listView = // ... (findById or getListView)
SwipeDismissList.OnDismissCallback callback = // .. see below
UndoMode mode = // .. see below
SwipeDismissList swipeList = new SwipeDismissList(listView, callback, mode);
```

You would normally want to do that in your `onCreate` method.

## OnDismissCallback

The second parameter to the constructor of `SwipeDismissList` is an `OnDismissCallback`.
You must implement that, to handle the deletion of elements:

```java
SwipeDismissList.OnDismissCallback callback = new SwipeDismissList.OnDismissCallback() {
	public SwipeDismissList.Undoable onDismiss(ListView listView, int position) {
		// Delete the item from your adapter (sample code):
		final String itemToDelete = mAdapter.get(position);
		mAdapter.remove(itemToDelete);
		return null;
	}
}
```

If you return `null` from the `onDismiss` method, your deletion won't be undoable.
To make your deletion undoable, you must return a valid `Undoable` (implementing 
at least its `undo` method), that restores the element again:

```java
SwipeDismissList.OnDismissCallback callback = new SwipeDismissList.OnDismissCallback() {
	public SwipeDismissList.Undoable onDismiss(AbsListView listView, final int position) {
		// Delete the item from your adapter (sample code):
		final String itemToDelete = mAdapter.get(position);
		mAdapter.remove(itemToDelete);
		return new SwipeDismissList.Undoable() {
			public void undo() {
				// Return the item at its previous position again
				mAdapter.insert(itemToDelete, position);
			}
		};
	}
}
```

You can override `getTitle` in the `Undoable` to provide an individual title for 
the item, that has been deleted. That title will be shown beside the undo button
in the popup. If you don't override that method (or it returns `null`) the default
deletion message will be shown in the popup. You can change this message with
`SwipeDismissList.setUndoString(String)`.

You can return `null` from the `onDismiss` method in general to disable undo on the 
list or just on special items, you don't want (or cannot) undo.

### Notification about final delete

If you want to get notified when the user doesn't have a chance to make the undo anymore,
meaning the popup dialog vanished (see below), just override the `Undoable.discard()`
method in your `Undoable`. This will be called as soon as the popup dialog vanishes.
You can e.g. really delete items from the database in that callback, that was just hidden
beforehand.

If you implement the discard method you need to call `SwipeDismissList.discardUndo()` in
the `onStop` method of your Activity. (See also [Leaky Popup](#leaky-popup) below). Otherwise
some items might not receive the `discard` call, when e.g. the device is rotated.

### Complete onDismissCallback example

An (pseudo) example of a complete `OnDismissCallback`:

```java
SwipeDismissList.OnDismissCallback callback = new SwipeDismissList.OnDismissCallback() {
	// Gets called whenever the user deletes an item.
	public SwipeDismissList.Undoable onDismiss(AbsListView listView, final int position) {
		// Get your item from the adapter (mAdapter being an adapter for MyItem objects)
		final MyItem deletedItem = mAdapter.getItem(position);
		// Delete item from adapter
		mAdapter.remove(deletedItem);
		// Return an Undoable implementing every method
		return new SwipeDismissList.Undoable() {

			// Method is called when user undoes this deletion
			public void undo() {
				// Reinsert item to list
				mAdapter.insert(deletedItem, position)
			}
			
			// Return an undo message for that item
			public String getTitle() {
				return deletedItem.toString() + " deleted";
			}

			// Called when user cannot undo the action anymore
			public void discard() {
				// Use this place to e.g. delete the item from database
				finallyDeleteFromSomeStorage(deletedItem);
			}
		};
	}
};
```

## setAutoHideDelay

The undo popup will be hidden automatically after some time, after the user has
touched the screen after the deletion. The delay until it will hide is by default
5 seconds. You can change that value with the `setAutoHideDelay(int)` method,
that takes a new delay in milliseconds. 

## setSwipeDirection

By default the user can swipe an item left or right to delete it. With this method
you can limit it to the left or right side. See the Javadoc of `setSwipeDirection`
and `SwipeDismissList.SwipeDirection` for further information.

## UndoMode

The undo list can handle multiple undos in three different ways. You define the way
with the third constructor parameter. If you don't pass in an argument, the default
mode will be `SwipeDismissList.UndoMode.SINGLE_UNDO`.

### SwipeDismissList.UndoMode.SINGLE_UNDO

Only the last deletion can be undone. As soon as the user deletes another item
from the list, this will be undoable, but the previous won't be anymore.

### SwipeDismissList.UndoMode.MULTI_UNDO

This mode is a multilevel undo. When the user deletes an item, while there is
still the undo popup shown for a previous deleted one, both items will be saved
for undo. Pressing now undo will undo the last deletion. Pressing undo after that
the deletion that has done before, and so on ...

As soon as the undo popup vanishes all stored undos will be discarded.

By default the undo popup shows a message with the amount of deleted items in this
mode. You can change this message with the `setUndoMultipleString(String)`.
This message can contain one placeholder (`%d`) that will be filled with the
amount of stored undos. 

If you pass `null` to that method, the undo popup
will always show the individual message (returned by `Undoable.getTitle()`) for
the last deletion (the one that will be undone, by a click on undo). If there is
no individual message for an `Undoable` the default message (`setUndoString(String)`)
will be shown instead.

### SwipeDismissList.UndoMode.COLLAPSE_UNDO

This mode collapsed multiple undos into one. When the user deletes an item, while
an undo popup is already shown, the new undo is stored. From now on the user sees
an "Undo all" instead of "Undo" button. A click on that will undo all stored deletions
at once.

If the popup vanished (due to the auto hide delay passed) all stored undos will be
discarded.

You can again use `setUndoMultipleString(String)` to set an individual message.
Also passig `null` is possible, but doesn't make too much sense in that case, since
the user will only see the last undo messgae, but all deletions will be undone.

## Customizing and Internationalization

If you want to customize the look and feel, just modify the resources as you like.

You can internationalize the "Undo" and "Undo all" string, in your own resources
by adding a string for the name "undo" and one for "undoall". This library only provides
the english translation.

You can pause the dismiss behavior of the list for some time by using the `setEnabled(boolean)`
method on the `SwipeDismissList`.

Bugs
----

For bugs and feature requests please use the GitHub issue tracker. I haven't limited
the library to any android api versions, but it might be, that it doesn't work on 
very old versions, so please feel free to tell me via an issue, I will then try to fix it.

### Leaky Popup

When the popup is shown and the device is rotate, it causes an exception to be thrown.
This is nothing really bad, but you can (and should) prevent that exception by calling 
`SwipeDismissList.discardUndo()` in the `onStop` method of your activity.

If you have implemented the `discard` method (see [above](#notification-about-final-delete)) you 
*MUST* call this method.


Contact
-------

For other questions or help, you can find contact data on [my page](http://www.timroes.de)
or you will find me often in #android-dev on irc.freenode.net.
