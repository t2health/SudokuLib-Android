/**
 * 
 */
package com.t2.sudoku;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import com.t2.sudoku.db.SudokuOpenHelper;

/**
 * @author wes
 * 
 */
public class SudokuBackupAgent extends BackupAgentHelper {

	static final String KEY_DB = SudokuOpenHelper.DB_NAME;
	static final String KEY_PREFS = "prefs";

	@Override
	public void onCreate() {
		super.onCreate();
		FileBackupHelper dbHelper = new FileBackupHelper(this, "../databases/" + SudokuOpenHelper.DB_NAME);
		addHelper(KEY_DB, dbHelper);
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, getPackageName() + "_preferences");
		addHelper(KEY_PREFS, helper);
	}

}
