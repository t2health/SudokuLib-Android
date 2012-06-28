package com.t2.sudoku.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the Virtual Hope Box content provider and its
 * clients.
 * 
 * @author wes
 * 
 */
public class SudokuContract {

	public static final String AUTHORITY = "com.t2.sudoku";

	public static final String SCHEME = "content://";

	private static final Uri BASE_URI = Uri.parse(SCHEME + AUTHORITY);

	private static final String DIR_MIME_TYPE_BASE = "vnd.android.cursor.dir";

	private static final String ITEM_MIME_TYPE_BASE = "vnd.android.cursor.item";

	public static final class Sudoku implements BaseColumns {
		public static enum Difficulty {
			SIMPLE("Easy"),
			EASY("Moderate"),
			INTERMEDIATE("Difficult"),
			EXPERT("Expert");

			private String mName;

			private Difficulty(String name) {
				mName = name;
			}

			@Override
			public String toString() {
				return mName;
			}
		}

		public static final String TABLE_NAME = "sudoku";
		public static final int SUDOKU_PUZZLE_ID_POSITION = 1;
		public static final int SUDOKU_PUZZLE_DIFFICULTY_POSITION = 1;
		public static final String PATH = TABLE_NAME;
		public static final String PATH_FOR_ID = PATH + "/#";
		public static final String PATH_FOR_DIFFICULTY = PATH + "/*";
		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
		public static final String CONTENT_MIME_TYPE = DIR_MIME_TYPE_BASE
				+ "/vnd.t2.sudoku.sudoku";
		public static final String CONTENT_ITEM_MIME_TYPE = ITEM_MIME_TYPE_BASE
				+ "/vnd.t2.sudoku.sudoku";
		public static final String COL_DIFFICULTY = "difficulty";
		public static final String COL_COMPLETE = "contact_id";
		public static final String COL_SOLUTION = "solution";
		public static final String COL_CURRENT = "current";
		public static final String COL_PUZZLE = "puzzle";
		public static final String COL_TITLE = "title";

	}

}
