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
