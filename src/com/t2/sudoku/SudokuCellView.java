package com.t2.sudoku;

import java.util.Iterator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.t2.sudoku.SudokuGridFragment.SudokuCell;

public class SudokuCellView extends View {

	private static Paint sTextPaint;
	private static Paint sFillPaint;
	private static Paint sMarkPaint;

	private SudokuCell mCell;
	private boolean mShowHint;
	private boolean mShowHighlight;
	private boolean mShowInvalid;

	Rect mMarkRect = new Rect();
	Rect mCellRect = new Rect();

	static {
		sTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sTextPaint.setColor(Color.BLACK);
		sTextPaint.setTextSize(40);
		sTextPaint.setStyle(Style.FILL);
		sTextPaint.setTextAlign(Align.CENTER);

		sMarkPaint = new Paint(sTextPaint);
		sMarkPaint.setTextSize(40);

		sFillPaint = new Paint();
		sFillPaint.setColor(0xFFa5bccd);
	}

	public SudokuCellView(Context context) {
		super(context);
		init();
	}

	public void setShowHint(boolean showHint) {
		mShowHint = showHint;
	}

	public boolean isShowHighlight() {
		return mShowHighlight;
	}

	public boolean isShowInvalid() {
		return mShowInvalid;
	}

	public void setShowInvalid(boolean showInvalid) {
		mShowInvalid = showInvalid;
	}

	public void setShowHighlight(boolean showHighlight) {
		mShowHighlight = showHighlight;
	}

	public SudokuCellView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SudokuCellView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setEnabled(true);
		setFocusable(true);
		if (isInEditMode()) {
			mCell = new SudokuCell();
			for (int i = 1; i <= 9; i++) {
				mCell.getMarks().add(i);
			}
		}
	}

	public SudokuCell getCell() {
		return mCell;
	}

	public void setCell(SudokuCell cell) {
		mCell = cell;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		sTextPaint.setTextSize(40);
		sMarkPaint.setTextSize(40);
		getDrawingRect(mCellRect);
		do {
			sTextPaint.getTextBounds("9".toCharArray(), 0, 1, mMarkRect);
			mMarkRect.inset(-10, -10);

			if (mCellRect.width() <= mMarkRect.width() || mCellRect.height() <= mMarkRect.height()) {
				break;
			}

			sTextPaint.setTextSize(sTextPaint.getTextSize() + 1);
		} while (true);

		do {
			sMarkPaint.getTextBounds("9 9 9".toCharArray(), 0, 5, mMarkRect);
			mMarkRect.inset(0, (int) (mMarkRect.height() * -1.5) + 3);

			if (mMarkRect.width() < mCellRect.width() && mMarkRect.height() < mCellRect.height()) {
				break;
			}

			sMarkPaint.setTextSize(sMarkPaint.getTextSize() - 1);
		} while (true);
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
			StringBuilder tts = new StringBuilder();
			final int row = ((int) (mCell.getPosition() / 9.0f)) + 65;

			final int col = mCell.getPosition() % 9;
			tts.append("Cell ").append((char) (row)).append(", ").append(col).append(", ");
			tts.append(mCell.isLocked() ? "Locked, " : "");

			if (mCell.getMarks().size() == 1) {
				tts.append(mCell.getMarks().first());
			} else if (mCell.getMarks().size() > 1) {
				tts.append("Marked, ");
				tts.append(TextUtils.join(" ", mCell.getMarks()));
			} else {
				tts.append("Empty");
			}
			tts.append(".");

			event.getText().add(tts.toString());
			return true;
		} else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
			StringBuilder tts = new StringBuilder();
			if (mCell.getMarks().size() == 1) {
				tts.append(mCell.getMarks().first());
			} else if (mCell.getMarks().size() > 1) {
				tts.append(TextUtils.join(" ", mCell.getMarks()));
			} else {
				tts.append("Empty");
			}
			tts.append(".");
			event.getText().add(tts.toString());
			event.setBeforeText("");
			event.setAddedCount(tts.length());
			return true;
		}

		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		getDrawingRect(mCellRect);
		if (mCell != null) {
			if (mShowInvalid) {
				sFillPaint.setColor(0xFFbc8585);
				canvas.drawRect(mCellRect, sFillPaint);
			} else if (mCell.isLocked() && isFocused()) {
				sFillPaint.setColor(0xFFE0E0E0);
				canvas.drawRect(mCellRect, sFillPaint);
			} else if (!mCell.isLocked() && isSelected()) {
				canvas.drawRect(mCellRect, sFillPaint);
			} else if (!mCell.isLocked() && isFocused()) {
				sFillPaint.setColor(0xFFbcAAAA);
				canvas.drawRect(mCellRect, sFillPaint);
			} else if (mShowHighlight) {
				sFillPaint.setColor(0xFFa5bc6e);
				canvas.drawRect(mCellRect, sFillPaint);
			} else if (mCell.isLocked()) {
				sFillPaint.setColor(0xFFC0C0C0);
				canvas.drawRect(mCellRect, sFillPaint);
			}
		}
		sFillPaint.setColor(0xFFa5bccd);

		if (mCell != null && mCell.getMarks().size() == 1) {
			String value = mCell.getMarks().first() + "";
			canvas.drawText(value, mCellRect.centerX(), mCellRect.centerY() + (sTextPaint.measureText(value) / 1.6f), sTextPaint);
		} else if (mCell != null && mCell.getMarks().size() > 1) {
			sMarkPaint.getTextBounds("9 9 9", 0, 5, mMarkRect);
			// mMarkRect.inset(0, -1);

			final Iterator<Integer> itr = mCell.getMarks().iterator();
			int index = 0;
			StringBuilder value = new StringBuilder();
			while (itr.hasNext()) {
				final int row = (int) Math.ceil(index / 3.0);
				if (index > 0 && index % 3 == 0) {
					canvas.drawText(value.substring(0, value.length() - 1), mCellRect.centerX(),
							(mCellRect.top + mMarkRect.height() * row) + (2 * row) + 1, sMarkPaint);
					value = new StringBuilder();
				}
				value.append(itr.next()).append(" ");
				index++;
			}

			if (value.length() > 0) {
				final int row = (int) Math.ceil(index / 3.0);
				canvas.drawText(value.substring(0, value.length() - 1).toString(), mCellRect.centerX(),
						(float) (mCellRect.top + mMarkRect.height() * row) + (2 * row) + 1, sMarkPaint);
			}

		}
	}
}
