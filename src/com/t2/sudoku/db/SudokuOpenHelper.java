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
package com.t2.sudoku.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.t2.sudoku.R;
import com.t2.sudoku.db.SudokuContract.Sudoku;
import com.t2.sudoku.db.SudokuContract.Sudoku.Difficulty;

public class SudokuOpenHelper extends SQLiteOpenHelper {

	public static final int DB_VERSION = 3;
	public static final String DB_NAME = "sudoku.db";

	private Context mContext;

	private static final String CREATE_SUDOKU = ""
			+ "CREATE TABLE " + Sudoku.TABLE_NAME + "( "
			+ Sudoku._ID + " 			INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ Sudoku.COL_COMPLETE + " 		INTEGER NOT NULL DEFAULT 0,"
			+ Sudoku.COL_CURRENT + " 		TEXT,"
			+ Sudoku.COL_DIFFICULTY + " 	TEXT NOT NULL,"
			+ Sudoku.COL_PUZZLE + " 		TEXT NOT NULL,"
			+ Sudoku.COL_TITLE + " 			TEXT NOT NULL,"
			+ Sudoku.COL_SOLUTION + " 		TEXT NOT NULL"
			+ ")";

	public SudokuOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SUDOKU);
		db.execSQL("PRAGMA foreign_keys=ON;");
		loadData(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Sudoku.TABLE_NAME);
		onCreate(db);
	}

	private void loadData(SQLiteDatabase db) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(mContext.getResources().openRawResource(R.raw.sudoku)));
			loadSudoku(db, in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Om nom nom
				}
			}
		}
	}

	private void loadSudoku(SQLiteDatabase db, BufferedReader in) throws IOException {
		String line;
		Sudoku.Difficulty difficulty = null;
		int count = 1;
		while ((line = in.readLine()) != null) {
			if (line.matches("[a-zA-Z].*?")) {
				count = 1;
				difficulty = Difficulty.valueOf(line);
			} else {
				ContentValues vals = new ContentValues();
				vals.put(Sudoku.COL_DIFFICULTY, difficulty.name());
				vals.put(Sudoku.COL_PUZZLE, line);
				vals.put(Sudoku.COL_TITLE, difficulty.toString() + " #" + count);
				line = in.readLine();
				vals.put(Sudoku.COL_SOLUTION, line);
				db.insert(Sudoku.TABLE_NAME, null, vals);
				count++;
			}
		}
	}
}
