/*
 * 
 * T2SudokuLib
 * 
 * Copyright © 2009-2012 United States Government as represented by 
 * the Chief Information Officer of the National Center for Telehealth 
 * and Technology. All Rights Reserved.
 * 
 * Copyright © 2009-2012 Contributors. All Rights Reserved. 
 * 
 * THIS OPEN SOURCE AGREEMENT ("AGREEMENT") DEFINES THE RIGHTS OF USE, 
 * REPRODUCTION, DISTRIBUTION, MODIFICATION AND REDISTRIBUTION OF CERTAIN 
 * COMPUTER SOFTWARE ORIGINALLY RELEASED BY THE UNITED STATES GOVERNMENT 
 * AS REPRESENTED BY THE GOVERNMENT AGENCY LISTED BELOW ("GOVERNMENT AGENCY"). 
 * THE UNITED STATES GOVERNMENT, AS REPRESENTED BY GOVERNMENT AGENCY, IS AN 
 * INTENDED THIRD-PARTY BENEFICIARY OF ALL SUBSEQUENT DISTRIBUTIONS OR 
 * REDISTRIBUTIONS OF THE SUBJECT SOFTWARE. ANYONE WHO USES, REPRODUCES, 
 * DISTRIBUTES, MODIFIES OR REDISTRIBUTES THE SUBJECT SOFTWARE, AS DEFINED 
 * HEREIN, OR ANY PART THEREOF, IS, BY THAT ACTION, ACCEPTING IN FULL THE 
 * RESPONSIBILITIES AND OBLIGATIONS CONTAINED IN THIS AGREEMENT.
 * 
 * Government Agency: The National Center for Telehealth and Technology
 * Government Agency Original Software Designation: T2SudokuLib001
 * Government Agency Original Software Title: T2SudokuLib
 * User Registration Requested. Please send email 
 * with your contact information to: robert.kayl2@us.army.mil
 * Government Agency Point of Contact for Original Software: robert.kayl2@us.army.mil
 * 
 */
package com.t2.sudoku;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.t2.sudoku.db.SudokuContract.Sudoku.Difficulty;

public class SudokuPuzzlesFragment extends Fragment implements OnTabChangeListener {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getTabHost().setup();
		TabHost.TabSpec spec;
		spec = getTabHost()
				.newTabSpec(Difficulty.SIMPLE.name())
				.setIndicator(Difficulty.SIMPLE.toString())
				.setContent(R.id.tab_simple);
		getTabHost().addTab(spec);
		spec = getTabHost()
				.newTabSpec(Difficulty.EASY.name())
				.setIndicator(Difficulty.EASY.toString())
				.setContent(R.id.tab_easy);
		getTabHost().addTab(spec);
		spec = getTabHost()
				.newTabSpec(Difficulty.INTERMEDIATE.name())
				.setIndicator(Difficulty.INTERMEDIATE.toString())
				.setContent(R.id.tab_intermediate);
		getTabHost().addTab(spec);
		spec = getTabHost()
				.newTabSpec(Difficulty.EXPERT.name())
				.setIndicator(Difficulty.EXPERT.toString())
				.setContent(R.id.tab_expert);
		getTabHost().addTab(spec);

		getTabHost().setOnTabChangedListener(this);

		int defaultTab = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(
				getString(R.string.pref_sudoku_difficulty_tab), 0);
		if (defaultTab == 0) {
			updateTab(Difficulty.SIMPLE.name());
		} else {
			getTabHost().setCurrentTab(defaultTab);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.sudoku_puzzles, null);
	}

	private TabHost getTabHost() {
		return (TabHost) getView().findViewById(android.R.id.tabhost);
	}

	public void onTabChanged(String tabId) {
		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
				.putInt(getString(R.string.pref_sudoku_difficulty_tab), getTabHost().getCurrentTab()).commit();
		updateTab(tabId);
	}

	private void updateTab(String tabId) {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(tabId) == null) {
			Fragment frag = new SudokuTabFragment();
			Bundle bundle = new Bundle();
			bundle.putString("difficulty", tabId);
			frag.setArguments(bundle);
			fm.beginTransaction()
					.replace(android.R.id.tabcontent, frag, tabId)
					.commit();
		}
	}
}
