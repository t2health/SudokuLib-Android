package com.t2.sudoku;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.t2.sudoku.db.SudokuContract.Sudoku;
import com.t2.sudoku.db.SudokuContract.Sudoku.Difficulty;

public class SudokuTabFragment extends ListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener {

	private static final int LOADER_PUZZLES = 1;

	private SudokuPuzzleAdapter mAdapter;
	private Difficulty mDifficulty;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mDifficulty = Difficulty.valueOf(getArguments().getString("difficulty"));
		getLoaderManager().initLoader(LOADER_PUZZLES, null, this);
		mAdapter = new SudokuPuzzleAdapter(getActivity(), null);
		setListAdapter(mAdapter);
		getListView().setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Cursor cursor = (Cursor) mAdapter.getItem(arg2);
		int id = cursor.getInt(cursor.getColumnIndex(Sudoku._ID));
		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(getString(R.string.pref_sudoku_puzzle), id).commit();
		Intent intent = new Intent(getActivity(), getActivity().getClass());
		startActivity(intent);
		getActivity().finish();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(),
				Sudoku.CONTENT_URI.buildUpon().appendPath(mDifficulty.name()).build(),
				new String[] { Sudoku.COL_TITLE, Sudoku.COL_COMPLETE, Sudoku._ID },
				null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	private static final class SudokuPuzzleAdapter extends SimpleCursorAdapter {

		public SudokuPuzzleAdapter(Context context, Cursor c) {
			super(context, R.layout.sudoku_row, c, new String[] { Sudoku.COL_TITLE, Sudoku.COL_COMPLETE }, new int[] {
					R.id.lbl_name, R.id.img_complete }, 0);
		}

		@Override
		public void setViewImage(ImageView v, String value) {
			v.setVisibility(Integer.valueOf(value) == 1 ? View.VISIBLE : View.INVISIBLE);
		}

	}

}
