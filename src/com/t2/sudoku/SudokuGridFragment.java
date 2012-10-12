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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import com.t2.sudoku.SudokuGridView.OnSudokuCellSelectedListener;
import com.t2.sudoku.db.SudokuContract.Sudoku;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SudokuGridFragment extends Fragment implements LoaderCallbacks<Cursor>, OnSudokuCellSelectedListener, OnClickListener,
        OnKeyListener {

    private static final int LOADER_SUDOKU = 1;

    private int mPuzzleId;
    private boolean mCompleted;
    private int mPrevSelection = -1;
    private String mPuzzleName;

    private SudokuListener mSudokuListener;

    public void setSudokuListener(SudokuListener sudokuListener) {
        mSudokuListener = sudokuListener;
    }

    public SudokuGridView getGridView() {
        return (SudokuGridView) getView().findViewById(R.id.grd_sudoku);
    }

    public SudokuGridFragment() {
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mPrevSelection = savedInstanceState.getInt("selection");
        }

        mPuzzleId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(getString(R.string.pref_sudoku_puzzle), 0);

        getGridView().setOnSudokuCellSelectedListener(this);
        getLoaderManager().initLoader(LOADER_SUDOKU, null, this);
    }

    public ToggleButton getButton(int value) {
        switch (value) {
            case 1:
                return (ToggleButton) getView().findViewById(R.id.btn_1);
            case 2:
                return (ToggleButton) getView().findViewById(R.id.btn_2);
            case 3:
                return (ToggleButton) getView().findViewById(R.id.btn_3);
            case 4:
                return (ToggleButton) getView().findViewById(R.id.btn_4);
            case 5:
                return (ToggleButton) getView().findViewById(R.id.btn_5);
            case 6:
                return (ToggleButton) getView().findViewById(R.id.btn_6);
            case 7:
                return (ToggleButton) getView().findViewById(R.id.btn_7);
            case 8:
                return (ToggleButton) getView().findViewById(R.id.btn_8);
            case 9:
                return (ToggleButton) getView().findViewById(R.id.btn_9);
        }

        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mnu_sudoku, menu);
    }

    private void showHint() {
        Random random = new Random();
        do {
            int index = random.nextInt(81);
            SudokuCellView scv = getGridView().getCell(index);
            SudokuCell cell = scv.getCell();
            if (!cell.isValid()) {
                cell.getMarks().clear();
                cell.getMarks().add(cell.getSolution());
                onSudokuCellSelected(scv);
                if (!getGridView().isInTouchMode()) {
                    scv.requestFocus();
                }
                scv.invalidate();
                break;
            }
        } while (true);

        evaluatePuzzleCompletion();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnu_hint && !mCompleted) {
            showHint();
            return true;
        } else if (item.getItemId() == R.id.mnu_verify) {
            getGridView().validateBoard();
            return true;
        } else if (item.getItemId() == R.id.mnu_change_puzzle) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove(getString(R.string.pref_sudoku_puzzle)).commit();
            Intent intent = new Intent(getActivity(), getActivity().getClass());
            startActivity(intent);
            getActivity().finish();
            return true;
        } else if (item.getItemId() == R.id.mnu_reset_puzzle) {
            ContentValues cv = new ContentValues();
            cv.put(Sudoku.COL_COMPLETE, false);
            cv.put(Sudoku.COL_CURRENT, (String) null);
            getActivity().getContentResolver().update(ContentUris.withAppendedId(Sudoku.CONTENT_URI, mPuzzleId), cv, null, null);
            getLoaderManager().getLoader(LOADER_SUDOKU).forceLoad();
            getGridView().clearHighlights();
            getGridView().clearValidation();
            getGridView().clearFocus();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void evaluatePuzzleCompletion() {
        if (getGridView().isComplete()) {
            if (mSudokuListener != null) {
                mSudokuListener.onPuzzleComplete(mPuzzleName);
            }
            mCompleted = true;
            ContentValues cv = new ContentValues();
            cv.put(Sudoku.COL_COMPLETE, true);
            cv.put(Sudoku.COL_CURRENT, getGridView().getCurrentState());
            getActivity().getContentResolver().update(ContentUris.withAppendedId(Sudoku.CONTENT_URI, mPuzzleId), cv, null, null);
            Toast.makeText(getActivity(), "Puzzle Completed", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        SudokuCellView scv = getSelectedCell();
        if (event.getAction() != KeyEvent.ACTION_UP || scv == null || scv.getCell() == null) {
            return false;
        }

        int num = -1;
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_Q:
                num = 1;
                break;
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_W:
                num = 2;
                break;
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_E:
                num = 3;
                break;
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_R:
                num = 4;
                break;
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_T:
                num = 5;
                break;
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_Y:
                num = 6;
                break;
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_U:
                num = 7;
                break;
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_I:
                num = 8;
                break;
            case KeyEvent.KEYCODE_9:
            case KeyEvent.KEYCODE_O:
                num = 9;
                break;
            case KeyEvent.KEYCODE_DEL:
                clearCell(scv);
                scv.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
                return true;
            case KeyEvent.KEYCODE_SPACE:
                invertCell(scv);
                scv.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
                return true;
            default:
                return false;
        }

        toggleNumber(scv, num);
        scv.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        return true;
    }

    /**
     * @param scv
     * @param num
     */
    private void toggleNumber(SudokuCellView scv, int num) {
        SudokuCell cell = scv.getCell();
        if (cell.getMarks().contains(num)) {
            cell.getMarks().remove(num);
        } else {
            cell.getMarks().add(num);
        }

        if (!mCompleted && cell.getMarks().size() == 1) {
            evaluatePuzzleCompletion();
        }
        scv.invalidate();
        updateToggles(scv);
        updateHighlights(scv);
    }

    private void clearCell(SudokuCellView scv) {
        scv.getCell().getMarks().clear();
        updateToggles(scv);
        scv.invalidate();
        updateHighlights(scv);
    }

    private void invertCell(SudokuCellView scv) {
        SortedSet<Integer> marks = new TreeSet<Integer>();
        for (int i = 1; i <= 9; i++) {
            marks.add(i);
        }
        marks.removeAll(scv.getCell().getMarks());
        scv.getCell().setMarks(marks);
        updateToggles(scv);
        scv.invalidate();
        updateHighlights(scv);
    }

    public void onClick(View v) {
        SudokuCellView scv = getSelectedCell();
        if (scv == null || scv.getCell() == null) {
            return;
        }

        if (v.getId() == R.id.btn_clear) {
            clearCell(scv);
        } else if (v.getId() == R.id.btn_invert) {
            invertCell(scv);
        } else {
            String value = (String) v.getTag();
            int intVal = Integer.valueOf(value);
            toggleNumber(scv, intVal);
        }
        scv.invalidate();
        updateHighlights(scv);
    }

    private SudokuCellView getSelectedCell() {
        if (mPrevSelection == -1) {
            return null;
        }

        return getGridView().getCell(mPrevSelection);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selection", mPrevSelection);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ContentUris.withAppendedId(Sudoku.CONTENT_URI, mPuzzleId), null, null, null, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sudoku_view, null);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor cursor = data;
        cursor.moveToFirst();
        String current = cursor.getString(cursor.getColumnIndex(Sudoku.COL_CURRENT));
        String puzzle = cursor.getString(cursor.getColumnIndex(Sudoku.COL_PUZZLE));
        String solution = cursor.getString(cursor.getColumnIndex(Sudoku.COL_SOLUTION));

        mPuzzleName = cursor.getString(cursor.getColumnIndex(Sudoku.COL_TITLE));
        mCompleted = cursor.getInt(cursor.getColumnIndex(Sudoku.COL_COMPLETE)) == 1;
        getActivity().setTitle("Sudoku - " + mPuzzleName);
        getGridView().setBoard(generateBoard(current, puzzle, solution));
        if (mPrevSelection >= 0) {
            onSudokuCellSelected(getSelectedCell());
        }

        if (mCompleted) {
            getGridView().clearHighlights();
            getGridView().clearValidation();
            getGridView().clearFocus();
            getGridView().lockCells();
        }

        for (int i = 1; i <= 9; i++) {
            final ToggleButton button = getButton(i);
            // button.setOnCheckedChangeListener(this);
            button.setOnClickListener(this);
        }

        for (int row = 0; row < getGridView().getChildCount(); row++) {
            LinearLayout rowView = (LinearLayout) getGridView().getChildAt(row);
            for (int col = 0; col < rowView.getChildCount(); col++) {
                SudokuCellView scv = (SudokuCellView) rowView.getChildAt(col);
                scv.setOnKeyListener(this);
            }
        }

        getView().findViewById(R.id.btn_clear).setOnClickListener(this);
        getView().findViewById(R.id.btn_invert).setOnClickListener(this);
        // getLoaderManager().destroyLoader(LOADER_SUDOKU);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getGridView() != null) {
            ContentValues cv = new ContentValues();
            cv.put(Sudoku.COL_CURRENT, getGridView().getCurrentState());
            getActivity().getContentResolver().update(ContentUris.withAppendedId(Sudoku.CONTENT_URI, mPuzzleId), cv, null, null);
        }
    }

    public void onSudokuCellSelected(SudokuCellView v) {
        if (mPrevSelection >= 0) {
            getSelectedCell().setSelected(false);
            getSelectedCell().invalidate();
        }
        v.setSelected(true);

        mPrevSelection = v.getCell().getPosition();

        updateToggles(v);
        updateHighlights(v);
    }

    private void updateToggles(SudokuCellView v) {
        for (int i = 1; i <= 9; i++) {
            boolean marked = v.getCell().getMarks().contains(i);
            getButton(i).setChecked(marked);
        }
    }

    private void updateHighlights(SudokuCellView v) {
        getGridView().clearValidation();
        if (v.getCell().getMarks().size() == 1) {
            getGridView().highlightValue(v.getCell().getMarks().first());
        } else {
            getGridView().clearHighlights();
        }
    }

    private List<SudokuCell> generateBoard(String current, String puzzle, String solution) {
        List<SudokuCell> board = new ArrayList<SudokuCell>();
        for (int i = 0; i < puzzle.length(); i++) {
            boolean locked = puzzle.charAt(i) != '.';
            int val = Integer.valueOf(solution.charAt(i) + "");
            SudokuCell cell = new SudokuCell();
            cell.setLocked(locked);
            cell.setSolution(val);
            cell.setPosition(i);
            if (locked) {
                cell.getMarks().add(val);
            }
            board.add(cell);
        }

        SudokuCell cell = null;
        int currentPosition = 0;
        if (current != null && current.length() > 0) {
            for (int i = 0; i < current.length(); i++) {
                char c = current.charAt(i);
                if (c == '[') {
                    cell = board.get(currentPosition);
                } else if (c == ']') {
                    cell = null;
                    currentPosition++;
                } else if (cell != null) {
                    cell.getMarks().add(Integer.valueOf(c + ""));
                } else {
                    currentPosition++;
                }
            }
        }

        return board;
    }

    public static interface SudokuListener {
        public void onPuzzleComplete(String puzzleName);
    }

    public static final class SudokuCell implements Parcelable {

        public static final Parcelable.Creator<SudokuCell> CREATOR = new Parcelable.Creator<SudokuCell>() {
            public SudokuCell createFromParcel(Parcel in) {
                return new SudokuCell(in);
            }

            public SudokuCell[] newArray(int size) {
                return new SudokuCell[size];
            }
        };

        private SortedSet<Integer> mMarks;
        private int mSolution;
        private int mPosition;
        private boolean mLocked;

        public SudokuCell(Parcel in) {
            mSolution = in.readInt();
            mPosition = in.readInt();
            mLocked = in.readInt() == 1;
            mMarks = new TreeSet<Integer>();
            if (in.readInt() > 0) {
                int[] marks = new int[] {};
                in.readIntArray(marks);
                for (int mark : marks) {
                    mMarks.add(mark);
                }
            }
        }

        public SudokuCell() {
            mMarks = new TreeSet<Integer>();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mSolution);
            dest.writeInt(mPosition);
            dest.writeInt(mLocked ? 1 : 0);
            dest.writeInt(mMarks.size() > 0 ? 1 : 0);
            int[] marks = new int[mMarks.size()];

            int i = 0;
            for (Integer mark : mMarks) {
                marks[i] = mark;
                i++;
            }
            dest.writeIntArray(marks);
        }

        public boolean isValid() {
            if (mMarks.size() == 1) {
                return mMarks.first() == mSolution;
            }
            return false;
        }

        public SortedSet<Integer> getMarks() {
            return mMarks;
        }

        public int getSolution() {
            return mSolution;
        }

        public boolean isLocked() {
            return mLocked;
        }

        public void setLocked(boolean locked) {
            mLocked = locked;
        }

        public void setMarks(SortedSet<Integer> marks) {
            mMarks = marks;
        }

        public void setSolution(int solution) {
            mSolution = solution;
        }

        public int getPosition() {
            return mPosition;
        }

        public void setPosition(int position) {
            mPosition = position;
        }

        public String toString() {
            if (mMarks.isEmpty()) {
                return ".";
            } else {
                return "[" + TextUtils.join("", mMarks) + "]";
            }
        }
    }

}
