/*
 * Copyright 2013 Roman Nurik, Tim Roes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.timroes.swipetodismiss;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * A {@link android.view.View.OnTouchListener} that makes the list items in a
 * {@link ListView} dismissable. {@link ListView} is given special treatment
 * because by default it handles touches for its list items... i.e. it's in
 * charge of drawing the pressed state (the list selector), handling list item
 * clicks, etc. 
 * 
 * Read the README file for a detailed explanation on how to use this class.
 */
public final class SwipeDismissList implements View.OnTouchListener {
	
	// Cached ViewConfiguration and system-wide constant values
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	private long mAnimationTime;
	
	// Fixed properties
	private AbsListView mListView;
	private OnDismissCallback mCallback;
	private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
	
	// Transient properties
	private SortedSet<PendingDismissData> mPendingDismisses = new TreeSet<PendingDismissData>();
	private int mDismissAnimationRefCount = 0;
	private float mDownX;
	private float mDownY;
	private boolean mSwiping;
	private VelocityTracker mVelocityTracker;
	private int mDownPosition;
	private View mDownView;
	private int mSwipeLayout;
	private boolean mPaused;
	private boolean mEnabled;
	private float mDensity;

	private UndoMode mMode;
	private List<Undoable> mUndoActions;
	private Handler mHandler;

	private PopupWindow mUndoPopup;
	private int mPopupXOffset = 0;
	private int mPopupYOffset = 15;
	private int mPopupGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
	private TextView mUndoText;
	private Button mUndoButton;

	private SwipeDirection mSwipeDirection = SwipeDirection.BOTH;
	private int mAutoHideDelay = 5000;
	private String mDeleteString = "Item deleted";
	private String mDeleteMultipleString = "%d items deleted";

	private int mDelayedMsgId;


	/**
	 * Defines the mode a {@link SwipeDismissList} handles multiple undos.
	 */
	public enum UndoMode {
		/**
		 * Only give the user the possibility to undo the last action.
		 * As soon as another item is deleted, there is no chance to undo
		 * the previous deletion.
		 */
		SINGLE_UNDO,
		
		/**
		 * Give the user the possibility to undo multiple deletions one by one.
		 * Every click on Undo will undo the previous deleted item. Undos will be
		 * collected as long as the undo popup stays open. As soon as the popup
		 * vanished (because {@link #setAutoHideDelay(int) autoHideDelay} is over)
		 * all saved undos will be discarded.
		 */
		MULTI_UNDO,

		/**
		 * Give the user the possibility to undo multiple deletions all together.
		 * As long as the popup stays open all further deletions will be collected.
		 * A click on the undo button will undo ALL deletions saved. As soon as
		 * the popup vanished (because {@link #setAutoHideDelay(int) autoHideDelay}
		 * is over) all saved undos will be discarded.
		 */
		COLLAPSED_UNDO
	};
	
	/**
	 * Defines the direction in which the swipe to delete can be done. The default
	 * is {@link SwipeDirection#BOTH}. Use {@link #setSwipeDirection(de.timroes.swipetodismiss.SwipeDismissList.SwipeDirection)}
	 * to set the direction.
	 */
	public enum SwipeDirection {
		/**
		 * The user can swipe each item into both directions (left and right)
		 * to delete it.
		 */
		BOTH,
		/**
		 * The user can only swipe the items to the beginning of the item to
		 * delete it. The start of an item is in Left-To-Right languages the left
		 * side and in Right-To-Left languages the right side. Before API level
		 * 17 this is always the left side.
		 */
		START,
		/**
		 * The user can only swipe the items to the end of the item to delete it.
		 * This is in Left-To-Right languages the right side in Right-To-Left
		 * languages the left side. Before API level 17 this will always be the
		 * right side.
		 */
		END
	}

	/**
	 * The callback interface used by {@link SwipeDismissListViewTouchListener}
	 * to inform its client about a successful dismissal of one or more list
	 * item positions.
	 */
	public interface OnDismissCallback {

		/**
		 * Called when the user has indicated they she would like to dismiss one
		 * or more list item positions.
		 *
		 * @param listView The originating {@link ListView}.
		 * @param position The position of the item to dismiss.
		 */
		Undoable onDismiss(AbsListView listView, int position);
	}

	/**
	 * An implementation of this abstract class must be returned by the 
	 * {@link OnDismissCallback#onDismiss(android.widget.ListView, int)} method,
	 * if the user should be able to undo that dismiss. If the action will be undone
	 * by the user {@link #undo()} will be called. That method should undo the previous
	 * deletion of the item and add it back to the adapter. Read the README file for
	 * more details. If you implement the {@link #getTitle()} method, the undo popup
	 * will show an individual title for that item. Otherwise the default title 
	 * (set via {@link #setUndoString(java.lang.String)}) will be shown.
	 */
	public abstract static class Undoable {

		/**
		 * Returns the individual undo message for this item shown in the
		 * popup dialog.
		 * 
		 * @return The individual undo message.
		 */
		public String getTitle() {
			return null;
		}

		/**
		 * Undoes the deletion.
		 */
		public abstract void undo();

		/**
		 * Will be called when this Undoable won't be able to undo anymore,
		 * meaning the undo popup has disappeared from the screen.
		 */
		public void discard() { }
		
	}

	/**
	 * Constructs a new swipe-to-dismiss touch listener for the given list view.
	 *
	 * @param listView The list view whose items should be dismissable.
	 * @param callback The callback to trigger when the user has indicated that
	 * she would like to dismiss one or more list items.
	 */
	public SwipeDismissList(AbsListView listView, OnDismissCallback callback) {
		this(listView, callback, UndoMode.SINGLE_UNDO);
	}

	/**
	 * Constructs a new swipe-to-dismiss touch listener for the given list view.
	 *
	 * @param listView The list view whose items should be dismissable.
	 * @param callback The callback to trigger when the user has indicated that
	 * she would like to dismiss one or more list items.
	 * @param mode The mode this list handles multiple undos.
	 */
	public SwipeDismissList(AbsListView listView, OnDismissCallback callback, UndoMode mode) {

		if(listView == null) {
			throw new IllegalArgumentException("listview must not be null.");
		}

		mHandler = new HideUndoPopupHandler(this);
		mListView = listView;
		mCallback = callback;
		mMode = mode;
		
		ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = listView.getContext().getResources().getInteger(
			android.R.integer.config_shortAnimTime);

		mDensity = mListView.getResources().getDisplayMetrics().density;

		// -- Load undo popup --
		LayoutInflater inflater = (LayoutInflater) mListView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.undo_popup, null);
		mUndoButton = (Button)v.findViewById(R.id.undo);
		mUndoButton.setOnClickListener(new UndoHandler());
		mUndoButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// If user tabs "undo" button, reset delay time to remove popup
				mDelayedMsgId++;
				return false;
			}
		});
		mUndoText = (TextView)v.findViewById(R.id.text);
		
		mUndoPopup = new PopupWindow(v);
		mUndoPopup.setAnimationStyle(R.style.fade_animation);
        // Get scren width in dp and set width respectively
        int xdensity = (int)(mListView.getContext().getResources().getDisplayMetrics().widthPixels / mDensity);
        if(xdensity < 300) {
    		mUndoPopup.setWidth((int)(mDensity * 280));
        } else if(xdensity < 350) {
            mUndoPopup.setWidth((int)(mDensity * 300));
        } else if(xdensity < 500) {
            mUndoPopup.setWidth((int)(mDensity * 330));
        } else {
            mUndoPopup.setWidth((int)(mDensity * 450));
        }
		mUndoPopup.setHeight((int)(mDensity * 56));
		// -- END Load undo popu --

		listView.setOnTouchListener(this);
		listView.setOnScrollListener(this.makeScrollListener());
		mEnabled = true;

		switch(mode) {
			case SINGLE_UNDO:
				mUndoActions = new ArrayList<Undoable>(1);
				break;
			default:
				mUndoActions = new ArrayList<Undoable>(10);
				break;
		}

	}

	/**
	 * Enables or disables (pauses or resumes) watching for swipe-to-dismiss
	 * gestures.
	 *
	 * @param enabled Whether or not to watch for gestures.
	 */
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	/**
	 * Sets the time in milliseconds after which the undo popup automatically 
	 * disappears.
	 * 
	 * @param delay Delay in milliseconds.
	 */
	public void setAutoHideDelay(int delay) {
		mAutoHideDelay = delay;
	}
	
	public void setSwipeLayout(int swipeLayout) {
		mSwipeLayout = swipeLayout;
	}

	/**
	 * Sets the offset, in dips, of the x position of
	 * the undo popup.
	 * 
	 * @param popupXOffset x position in dips.
	 */
	public void setPopupXOffset(int popupXOffset) {
		mPopupXOffset = popupXOffset;
	}

	/**
	 * Sets the offset, in dips, of the y position of
	 * the undo popup.
	 * 
	 * @param popupYOffset y position in dips.
	 */
	public void setPopupYOffset(int popupYOffset) {
		mPopupYOffset = popupYOffset;
	}

	/**
	 * Sets the Gravity controlling the placement of
	 * the undo popup.
	 * 
	 * @param popupGravity the gravity which controls the placement of the popup window
	 */
	public void setPopupGravity(int popupGravity) {
		mPopupGravity = popupGravity;
	}

	/**
	 * Sets the directions in which a list item can be swiped to delete.
	 * By default this is set to {@link SwipeDirection#BOTH} so that an item
	 * can be swiped into both directions.
	 *
	 * @param direction The direction to limit the swipe to.
	 */
	public void setSwipeDirection(SwipeDirection direction) {
		mSwipeDirection = direction;
	}

	/**
	 * Sets the string shown in the undo popup. This will only show if
	 * the {@link Undoable} returned by the {@link OnDismissCallback} returns
	 * {@code null} from its {@link Undoable#getTitle()} method.
	 * 
	 * @param msg The string shown in the undo popup.
	 */
	public void setUndoString(String msg) {
		mDeleteString = msg;
	}

	/**
	 * Sets the string shown in the undo popup, when {@link UndoMode} is set to
	 * {@link UndoMode#MULTI_UNDO} or {@link UndoMode#COLLAPSED_UNDO} and
	 * multiple deletions has been stored for undo. If this string contains
	 * one {@code %d} inside, this will be filled by the numbers of stored undos.
	 * 
	 * @param msg The string shown in the undo popup for multiple undos.
	 */
	public void setUndoMultipleString(String msg) {
		mDeleteMultipleString = msg;
	}


    /**
     * Discard all stored undos and hide the undo popup dialog.
     */
    public void discardUndo() {
        for(Undoable undoable : mUndoActions) {
            undoable.discard();
        }
        mUndoActions.clear();
        mUndoPopup.dismiss();
    }

	/**
	 * Returns an {@link android.widget.AbsListView.OnScrollListener} to be
	 * added to the {@link ListView} using
	 * {@link ListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)}.
	 * If a scroll listener is already assigned, the caller should still pass
	 * scroll changes through to this listener. This will ensure that this
	 * {@link SwipeDismissListViewTouchListener} is paused during list view
	 * scrolling.</p>
	 *
	 * @see {@link SwipeDismissListViewTouchListener}
	 */
	private AbsListView.OnScrollListener makeScrollListener() {
		return new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				mPaused = !(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {
			}
		};
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (mViewWidth < 2) {
			mViewWidth = mListView.getWidth();
		}

		switch (MotionEventCompat.getActionMasked(motionEvent)) {
			case MotionEvent.ACTION_DOWN: {
				if (mPaused || !mEnabled) {
					return false;
				}
				resetSwipeState();
				
				// TODO: ensure this is a finger, and set a flag

				// Find the child view that was touched (perform a hit test)
				Rect rect = new Rect();
				int childCount = mListView.getChildCount();
				int[] listViewCoords = new int[2];
				mListView.getLocationOnScreen(listViewCoords);
				int x = (int) motionEvent.getRawX() - listViewCoords[0];
				int y = (int) motionEvent.getRawY() - listViewCoords[1];
				View child;
				for (int i = 0; i < childCount; i++) {
					child = mListView.getChildAt(i);
					child.getHitRect(rect);
					if (rect.contains(x, y)) {
						if (mSwipeLayout != 0) {
							View swipeView = child.findViewById(mSwipeLayout);
							if (swipeView != null) {
								mDownView = swipeView;
								break;
							}
						}
						mDownView = child;
						break;
					}
				}

				if (mDownView != null) {
					mDownX = motionEvent.getRawX();
					mDownY = motionEvent.getRawY();
					mDownPosition = mListView.getPositionForView(mDownView);

					mVelocityTracker = VelocityTracker.obtain();
					mVelocityTracker.addMovement(motionEvent);
				}
				view.onTouchEvent(motionEvent);
				return true;
			}

			case MotionEvent.ACTION_UP: {
				if (mVelocityTracker == null) {
					break;
				}

				float deltaX = motionEvent.getRawX() - mDownX;
				mVelocityTracker.addMovement(motionEvent);
				mVelocityTracker.computeCurrentVelocity(1000);
				float velocityX = Math.abs(mVelocityTracker.getXVelocity());
				float velocityY = Math.abs(mVelocityTracker.getYVelocity());
				boolean dismiss = false;
				boolean dismissRight = false;
				if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
					dismiss = true;
					dismissRight = deltaX > 0;
				} else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity
					&& velocityY < velocityX && mSwiping && isDirectionValid(mVelocityTracker.getXVelocity())
					&& deltaX >= mViewWidth * 0.2f) {
					dismiss = true;
					dismissRight = mVelocityTracker.getXVelocity() > 0;
				}
				if (dismiss) {
					// dismiss
					final View downView = mDownView; // mDownView gets null'd before animation ends
					final int downPosition = mDownPosition;
					++mDismissAnimationRefCount;
					mPaused = true;
					animate(mDownView)
						.translationX(dismissRight ? mViewWidth : -mViewWidth)
						.alpha(0)
						.setDuration(mAnimationTime)
						.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							performDismiss(downView, downPosition);
						}
					});
				} else {
					// cancel
					animate(mDownView)
						.translationX(0)
						.alpha(1)
						.setDuration(mAnimationTime)
						.setListener(null);
				}
				resetSwipeState();
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				
				if(mUndoPopup.isShowing()) {	
					// Send a delayed message to hide popup
					mHandler.sendMessageDelayed(mHandler.obtainMessage(mDelayedMsgId), 
						mAutoHideDelay);
				}
				
				if (mVelocityTracker == null || mPaused || !mEnabled) {
					break;
				}

				mVelocityTracker.addMovement(motionEvent);
				float deltaX = motionEvent.getRawX() - mDownX;
				float deltaY = motionEvent.getRawY() - mDownY;
				// Only start swipe in correct direction
				if(isDirectionValid(deltaX)) {
					if (Math.abs(deltaX) > mSlop && Math.abs(deltaX) > Math.abs(deltaY)) {
						mSwiping = true;
						mListView.requestDisallowInterceptTouchEvent(true);

						// Cancel ListView's touch (un-highlighting the item)
						MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
						cancelEvent.setAction(MotionEvent.ACTION_CANCEL
							| (MotionEventCompat.getActionIndex(motionEvent)
							<< MotionEvent.ACTION_POINTER_INDEX_SHIFT));
						mListView.onTouchEvent(cancelEvent);
						cancelEvent.recycle();
					}
				} else {
					// If we swiped into wrong direction, act like this was the new
					// touch down point
					mDownX = motionEvent.getRawX();
					deltaX = 0;
				}

				if (mSwiping) {
					setTranslationX(mDownView, deltaX);
					setAlpha(mDownView, Math.max(0f, Math.min(1f,
						1f - 2f * Math.abs(deltaX) / mViewWidth)));
					return true;
				}
				break;
			}
		}
		return false;
	}

	private void resetSwipeState() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
		}
		mVelocityTracker = null;
		mDownX = 0;
		mDownY = 0;
		mDownView = null;
		mDownPosition = ListView.INVALID_POSITION;
		mSwiping = false;
	}

	/**
	 * Checks whether the delta of a swipe indicates, that the swipe is in the
	 * correct direction, regarding the direction set via
	 * {@link #setSwipeDirection(de.timroes.swipetodismiss.SwipeDismissList.SwipeDirection)}
	 *
	 * @param deltaX The delta of x coordinate of the swipe.
	 * @return Whether the delta of a swipe is in the right direction.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private boolean isDirectionValid(float deltaX) {

		int rtlSign = 1;
		// On API level 17 and above, check if we are in a Right-To-Left layout
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if(mListView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
				rtlSign = -1;
			}
		}

		// Check if swipe has been done in the corret direction
		switch(mSwipeDirection) {
			default:
			case BOTH:
				return true;
			case START:
				return rtlSign * deltaX < 0;
			case END:
				return rtlSign * deltaX > 0;
		}

	}

	class PendingDismissData implements Comparable<PendingDismissData> {

		public int position;
		public View view;

		public PendingDismissData(int position, View view) {
			this.position = position;
			this.view = view;
		}

		@Override
		public int compareTo(PendingDismissData other) {
			// Sort by descending position
			return other.position - position;
		}
	}

	private void performDismiss(final View dismissView, final int dismissPosition) {
		// Animate the dismissed list item to zero-height and fire the dismiss callback when
		// all dismissed list item animations have completed. This triggers layout on each animation
		// frame; in the future we may want to do something smarter and more performant.

		final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
		final int originalHeight = dismissView.getHeight();

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				--mDismissAnimationRefCount;
				if (mDismissAnimationRefCount == 0) {
					// No active animations, process all pending dismisses.

					for(PendingDismissData dismiss : mPendingDismisses) {
						if(mMode == UndoMode.SINGLE_UNDO) {
							for(Undoable undoable : mUndoActions) {
								undoable.discard();
							}
							mUndoActions.clear();
						}
						Undoable undoable = mCallback.onDismiss(mListView, dismiss.position);
						if(undoable != null) {
							mUndoActions.add(undoable);
						}
						mDelayedMsgId++;
					}

					if(!mUndoActions.isEmpty()) {
						changePopupText();
						changeButtonLabel();

						// Show undo popup
						mUndoPopup.showAtLocation(mListView, 
							mPopupGravity,
							(int)(mDensity * mPopupXOffset), 
							(int)(mDensity * mPopupYOffset));
					}

					ViewGroup.LayoutParams lp;
					for (PendingDismissData pendingDismiss : mPendingDismisses) {
						// Reset view presentation
						setAlpha(pendingDismiss.view, 1f);
						setTranslationX(pendingDismiss.view, 0);
						lp = pendingDismiss.view.getLayoutParams();
						lp.height = originalHeight;
						pendingDismiss.view.setLayoutParams(lp);
					}

					mPendingDismisses.clear();
				}
				mPaused = false;
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});

		mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
		animator.start();
	}

	/**
	 * Changes text in the popup depending on stored undos.
	 */
	private void changePopupText() {
		String msg = "";
		if(mUndoActions.size() > 1 && mDeleteMultipleString != null) {
			msg = String.format(mDeleteMultipleString, mUndoActions.size());
		} else if(mUndoActions.size() >= 1) {
			// Set title from single undoable or when no multiple deletion string
			// is given
			if(mUndoActions.get(mUndoActions.size() - 1).getTitle() != null) {
				msg = mUndoActions.get(mUndoActions.size() - 1).getTitle();
			} else {
				msg = mDeleteString;
			}
		}
		mUndoText.setText(msg);
	}

	private void changeButtonLabel() {
		String msg;
		if(mUndoActions.size() > 1 && mMode == UndoMode.COLLAPSED_UNDO) {
			msg = mListView.getResources().getString(R.string.undoall);
		} else {
			msg = mListView.getResources().getString(R.string.undo);
		}
		mUndoButton.setText(msg);
	}

	/**
	 * Takes care of undoing a dismiss. This will be added as a 
	 * {@link View.OnClickListener} to the undo button in the undo popup.
	 */
	private class UndoHandler implements View.OnClickListener {

		public void onClick(View v) {
			if(!mUndoActions.isEmpty()) {
				switch(mMode) {
					case SINGLE_UNDO:
						mUndoActions.get(0).undo();
						mUndoActions.clear();
						break;
					case COLLAPSED_UNDO:
						Collections.reverse(mUndoActions);
						for(Undoable undo : mUndoActions) {
							undo.undo();	
						}
						mUndoActions.clear();
						break;
					case MULTI_UNDO:
						mUndoActions.get(mUndoActions.size() - 1).undo();
						mUndoActions.remove(mUndoActions.size() - 1);
						break;
				}
			}

			// Dismiss dialog or change text
			if(mUndoActions.isEmpty()) {
				mUndoPopup.dismiss();
			} else {
				changePopupText();
				changeButtonLabel();
			}

			mDelayedMsgId++;

		}
		
	}
	
	/**
	 * Handler used to hide the undo popup after a special delay.
	 */
	private static class HideUndoPopupHandler extends Handler {
		private final WeakReference<SwipeDismissList> parentRef;
		
		public HideUndoPopupHandler(SwipeDismissList parent) {
			this.parentRef = new WeakReference<SwipeDismissList>(parent);
		}
		
		@Override
		public void handleMessage(Message msg) {
			SwipeDismissList parent = parentRef.get();
			
			if(parent != null && msg.what == parent.mDelayedMsgId) {
				// Call discard on any element
				for(Undoable undo : parent.mUndoActions) {
					undo.discard();
				}
				parent.mUndoActions.clear();
				parent.mUndoPopup.dismiss();
			}
		}
		
	}
	
}