package nl.mpcjanssen.todotxtholo;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class FilterListFragment extends Fragment {

	final static String TAG = FilterListFragment.class.getSimpleName();
	private ArrayList<String> items;
	private ArrayList<String> selectedItems;
	private ListView lv;
	private int layoutId;
	private int viewId;
	private GestureDetector gestureDetector;
	private OnTouchListener gestureListener;
	private ActionBar actionbar;

	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We have state we need to keep over config changes
		setRetainInstance(true);

	}

	public void onPause () {
		selectedItems.clear();
		selectedItems.addAll(getFilters());
		super.onPause();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		LinearLayout layout = (LinearLayout) inflater.inflate(layoutId, container, false);

		lv = (ListView) layout.findViewById(viewId);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		lv.setAdapter(new ArrayAdapter<String>(getActivity(),
				R.layout.simple_list_item_multiple_choice, items));


		for (int i = 0 ; i< items.size() ; i++) {
			if (selectedItems.contains(items.get(i))) {
				lv.setItemChecked(i,true);
			}
		}
		gestureDetector = new GestureDetector(TodoApplication.appContext, new FilterGestureDetector());
		gestureListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					MotionEvent cancelEvent = MotionEvent.obtain(event);
					cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
					v.onTouchEvent(cancelEvent);
					return true;
				}
				return false;
			}
		};
		
		lv.setOnTouchListener(gestureListener);
		return layout;
	}

	public ArrayList<String> getFilters() {

		ArrayList<String> arr = new ArrayList<String>();
		if(lv==null) {
			// Tab was not displayed so no selections made
			return arr;
		}
		int size = lv.getCount();
		for (int i = 0; i < size; i++) {
			if (lv.isItemChecked(i)) {
				arr.add((String)lv.getAdapter().getItem(i));
				Log.v("Filter", " Adding priority "
						+ (String)lv.getAdapter().getItem(i)
						+ " to applied filters.");				
			}
		}
		return arr;
	}

	public void setArguments(ArrayList<String> items, ArrayList<String> selectedItems,
			int layoutId, int listId , ActionBar actionbar) {
		this.items = items;
		if (this.selectedItems == null ) { 
			this.selectedItems  = selectedItems;
		}
		this.layoutId = layoutId;
		this.viewId = listId;
		this.actionbar = actionbar;
	}



class FilterGestureDetector extends SimpleOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
			return false;

		int index = actionbar.getSelectedNavigationIndex();
		// right to left swipe
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			Log.v(TAG, "Fling left");
			if (index<2) index++;
			actionbar.setSelectedNavigationItem(index);
			return true;
		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			// left to right swipe
			Log.v(TAG, "Fling right");
			if (index>0) index--;
			actionbar.setSelectedNavigationItem(index);
			return true;
		}
		return false;
	}
}
}