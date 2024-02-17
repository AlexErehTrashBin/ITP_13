package ru.vsu.cs.course1.game;

import ru.vsu.cs.util.JTableUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ParamsDialog extends JDialog {

	private JPanel mainDialog;
	private JSpinner rowSpinner;
	private JSpinner colSpinner;
	private JButton buttonApply;
	private JButton buttonNewGame;
	private JButton buttonCancel;
	private JSlider sliderCellSize;

	private final GameParams params;
	private final JTable gameFieldJTable;

	private int oldCellSize;

	public ParamsDialog(GameParams params, JTable gameFieldJTable, ActionListener newGameAction) {
		this.setTitle("Параметры");
		this.setContentPane(mainDialog);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.pack();

		this.setResizable(false);

		this.params = params;
		this.gameFieldJTable = gameFieldJTable;

		this.oldCellSize = gameFieldJTable.getRowHeight();
		sliderCellSize.addChangeListener(e -> {
			int value = sliderCellSize.getValue();
			JTableUtils.resizeJTableCells(gameFieldJTable, value, value);
		});
		buttonCancel.addActionListener(e -> {
			JTableUtils.resizeJTableCells(gameFieldJTable, oldCellSize, oldCellSize);
			this.setVisible(false);
		});
		buttonNewGame.addActionListener(e -> {
			buttonApply.doClick();
			if (newGameAction != null) {
				newGameAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "newGame"));
			}
		});
		buttonApply.addActionListener(e -> {
			params.setRowCount((int) rowSpinner.getValue());
			params.setColumnCount((int) colSpinner.getValue());

			oldCellSize = gameFieldJTable.getRowHeight();
			this.setVisible(false);
		});
	}

	public void updateView() {
		rowSpinner.setValue(params.getRowCount());
		colSpinner.setValue(params.getColumnCount());
		sliderCellSize.setValue(gameFieldJTable.getRowHeight());
	}
}
