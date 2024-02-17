package ru.vsu.cs.course1.game;

public class GameParams {
	private int rowCount;
	private int columnCount;

	public GameParams(int rowCount, int columnCount) {
		this.rowCount = rowCount;
		this.columnCount = columnCount;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}
}
