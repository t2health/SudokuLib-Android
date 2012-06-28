/**
 * 
 */
package com.t2.sudoku.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.t2.sudoku.db.SudokuContract.Sudoku;

/**
 * @author wes
 * 
 */
public class SudokuProvider extends ContentProvider {

	private static final String TAG = "SudokuProvider";

	private static final int SUDOKU_ID = 1;
	private static final int SUDOKU_DIFFICULTY = 2;
	private static final int SUDOKU = 3;

	private static final UriMatcher sUriMatcher;

	private SudokuOpenHelper mHelper;

	/**
	 * Static Initialization
	 */
	static {
		// Initialize URI Matcher
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(SudokuContract.AUTHORITY, Sudoku.PATH, SUDOKU);
		sUriMatcher.addURI(SudokuContract.AUTHORITY, Sudoku.PATH_FOR_ID, SUDOKU_ID);
		sUriMatcher.addURI(SudokuContract.AUTHORITY, Sudoku.PATH_FOR_DIFFICULTY, SUDOKU_DIFFICULTY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "Delete called on URI " + uri);

		String table = null;
		String where = null;

		switch (sUriMatcher.match(uri)) {
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		int match = sUriMatcher.match(uri);
		switch (match) {
		case SUDOKU_ID:
			return Sudoku.CONTENT_ITEM_MIME_TYPE;
		case SUDOKU_DIFFICULTY:
			return Sudoku.CONTENT_MIME_TYPE;
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#insert(android.net.Uri,
	 * android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "Insert called on URI " + uri);

		Uri resultUri = null;
		String table = null;

		switch (sUriMatcher.match(uri)) {
		case SUDOKU:
			table = Sudoku.TABLE_NAME;
			resultUri = Sudoku.CONTENT_URI;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mHelper.getWritableDatabase();
		long rowId = db.insert(table, null, values);

		if (rowId > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
			return resultUri.buildUpon().appendPath("" + rowId).build();
		}

		throw new android.database.SQLException("Unable to insert row into " + uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		mHelper = new SudokuOpenHelper(getContext());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 * java.lang.String[], java.lang.String, java.lang.String[],
	 * java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Log.d(TAG, "Query called on URI " + uri);

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case SUDOKU_DIFFICULTY:
			String difficulty = uri.getPathSegments().get(Sudoku.SUDOKU_PUZZLE_DIFFICULTY_POSITION);
			builder.setTables(Sudoku.TABLE_NAME);
			builder.appendWhere(Sudoku.COL_DIFFICULTY + " = '" + difficulty + "'");
			break;
		case SUDOKU_ID:
			String id = uri.getPathSegments().get(Sudoku.SUDOKU_PUZZLE_ID_POSITION);
			builder.setTables(Sudoku.TABLE_NAME);
			builder.appendWhere(Sudoku._ID + " = " + id);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		Cursor result = builder.query(mHelper.getReadableDatabase(), projection, selection,
				selectionArgs, null, null,
				sortOrder);
		result.setNotificationUri(getContext().getContentResolver(), uri);

		return result;
	}

	public void refreshDatabase() {
		mHelper.close();
		mHelper = new SudokuOpenHelper(getContext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri,
	 * android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		Log.d(TAG, "Update called on URI " + uri);

		String table = null;
		String where = null;

		switch (sUriMatcher.match(uri)) {
		case SUDOKU_ID:
			String id = uri.getPathSegments().get(Sudoku.SUDOKU_PUZZLE_ID_POSITION);
			table = Sudoku.TABLE_NAME;
			where = Sudoku._ID + " = " + id;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (where != null && selection != null) {
			selection = where + " AND " + selection;
		} else if (where != null) {
			selection = where;
		}

		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count = db.update(table, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

}
