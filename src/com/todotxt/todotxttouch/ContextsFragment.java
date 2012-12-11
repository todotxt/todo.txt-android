package com.todotxt.todotxttouch;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ContextsFragment extends Fragment {

	private ArrayList<String> projectsArr = null;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
	
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.tab_contexts, container, false);
		
		final ListView priorities = (ListView) layout.findViewById(R.id.contextslv);
		priorities.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		priorities.setAdapter(new ArrayAdapter<String>(getActivity(),
				R.layout.simple_list_item_multiple_choice, projectsArr));

		return layout;
    }

	public void setArguments(ArrayList<String> stringArrayListExtra) {
		// TODO Auto-generated method stub
		this.projectsArr = stringArrayListExtra;
	}

}
