package com.todotxt.todotxttouch;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class FilterListFragment extends Fragment {

	private ArrayList<String> items = null;
	private ArrayList<String> selectedItems = null;
	private ListView lv;
	private int layoutId;
	private int viewId;

	@Override
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
			int layoutId, int listId ) {
		this.items = items;
		this.selectedItems  = selectedItems;
		this.layoutId = layoutId;
		this.viewId = listId;
	}

}
