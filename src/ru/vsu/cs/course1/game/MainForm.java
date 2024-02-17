package ru.vsu.cs.course1.game;

import ru.vsu.cs.course1.game.model.GameState;
import ru.vsu.cs.util.DrawUtils;
import ru.vsu.cs.util.JTableUtils;
import ru.vsu.cs.util.SwingUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainForm extends JFrame {
	private JPanel mainFrame;
	private JTable gameTable;
	private JLabel scoreLabel;
	public final int DEFAULT_COL_COUNT = 5;
	public final int DEFAULT_ROW_COUNT = 5;
	private static final int DEFAULT_GAP = 10;
	private static final int DEFAULT_CELL_SIZE = 70;

	public int selectedCellRow = -1;
	public int selectedCellCol = -1;

	private static final Color[] COLORS = {
			new Color(0xCCC3B3),
			new Color(0xEEE4DA),
			new Color(0xEDE0C8),
			new Color(0xF2B179),
			new Color(0xF49769),
			new Color(0xF67C5F),
			new Color(0xF2603E),
			new Color(0xEACF76),
			new Color(0xEBCA65),
			new Color(0xEBC658),
			new Color(0xE6C356),
			new Color(0xE9BD4F)
	};
	private final ParamsDialog dialogParams;
	private final GameParams params = new GameParams(DEFAULT_ROW_COUNT, DEFAULT_COL_COUNT);
	private final Game game = new Game();

	public MainForm() {
		this.setTitle("Twelve");
		this.setContentPane(mainFrame);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();

		setJMenuBar(createMenuBar());
		this.pack();

		gameTable.setRowHeight(DEFAULT_CELL_SIZE);
		JTableUtils.initJTableForArray(gameTable, DEFAULT_CELL_SIZE, false, false, false, false);
		gameTable.setIntercellSpacing(new Dimension(0, 0));
		gameTable.setEnabled(false);


		gameTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			final class DrawComponent extends Component {
				private int row = 0, column = 0;

				@Override
				public void paint(Graphics gr) {
					Graphics2D g2d = (Graphics2D) gr;
					int width = getWidth() - 2;
					int height = getHeight() - 2;
					paintCell(row, column, g2d, width, height);
				}
			}

			final DrawComponent comp = new DrawComponent();

			@Override
			public Component getTableCellRendererComponent(
					JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				comp.row = row;
				comp.column = column;
				return comp;
			}
		});

		newGame();

		updateWindowSize();
		updateView();

		dialogParams = new ParamsDialog(params, gameTable, e -> newGame());

		gameTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int row = gameTable.rowAtPoint(e.getPoint());
				int col = gameTable.columnAtPoint(e.getPoint());

				if (selectedCellRow == -1 && selectedCellCol == -1) {
					if (game.getCellValue(row, col) == 0) { //empty cell
						return;
					}
					selectedCellRow = row;
					selectedCellCol = col;

				} else {
					if (row == selectedCellRow && col == selectedCellCol) { //pass selection
						selectedCellRow = -1;
						selectedCellCol = -1;
					} else if (game.doesPathExist(selectedCellRow, selectedCellCol, row, col)) {
						game.mergeCells(selectedCellRow, selectedCellCol, row, col);
						updateView();
						selectedCellRow = -1;
						selectedCellCol = -1;
					}
				}
			}
		});

	}

	private JMenuItem createMenuItem(String text, String shortcut, ActionListener action) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.addActionListener(action);
		if (shortcut != null) {
			menuItem.setAccelerator(KeyStroke.getKeyStroke(shortcut.replace('+', ' ')));
		}
		return menuItem;
	}

	private JMenuBar createMenuBar() {
		JMenuBar gameMenuBar = new JMenuBar();
		//Game Menu
		JMenu gameMenu = new JMenu("Игра");


		gameMenuBar.add(gameMenu);
		gameMenu.add(createMenuItem("Новая игра", "ctrl+N", e -> newGame()));
		gameMenu.add(createMenuItem("Настройки", "ctrl+P", e -> {
			dialogParams.updateView();
			dialogParams.setVisible(true);
		}));
		gameMenu.addSeparator();
		gameMenu.add(createMenuItem("Выход", "ctrl+X", e -> System.exit(0)));
		JMenu menuView = new JMenu("Вид");
		gameMenuBar.add(menuView);
		menuView.add(createMenuItem("Подстроить размер окна", null, e -> updateWindowSize()));
		menuView.addSeparator();
		SwingUtils.initLookAndFeelMenu(menuView);

		JMenu menuHelp = new JMenu("Справка");
		gameMenuBar.add(menuHelp);
		menuHelp.add(createMenuItem("Правила игры", "ctrl+R", e -> {
			SwingUtils.showInfoMessageBox(
					"""
							После каждого хода игрока на игровом поле будет появляться по одной клетке.\s
							В случае перемещения клетки а не её объединения создаётся 2 клетки, а не одна.\s
							Задача игрока — объединять одинаковые цифры, путь между которыми свободен(!).\s
							Для победы в игре необходимо добиться того, чтобы на поле появился блок с числом 12.\s
							Но если в какой-то момент в игре становится невозможным походить\s
							(все клетки непустые и все клетки не имеют соседа такого же номинала), то игра проиграна.
							""",
					"Правила");
		}));
		menuHelp.add(createMenuItem("О программе", "ctrl+A", e -> {
			SwingUtils.showInfoMessageBox(
					"""
							Игра "Twelve"

							Автор: Александр Ерешкин (github: AlexEreh)
							""",
					"О программе"
			);
		}));
		return gameMenuBar;
	}

	private void updateWindowSize() {
		int menuSize = this.getJMenuBar() != null ? this.getJMenuBar().getHeight() : 0;
		SwingUtils.setFixedSize(
				this,
				gameTable.getWidth() + 2 * DEFAULT_GAP + 60,
				gameTable.getHeight() + mainFrame.getY() +
						menuSize + DEFAULT_GAP + 2 * DEFAULT_GAP + 60
		);
		this.setMaximumSize(null);
		this.setMinimumSize(null);
	}

	private void updateView() {
		gameTable.repaint();
		if (game.currentGameState() != GameState.PLAYING) {
			if (game.currentGameState() == GameState.WIN) {
				JOptionPane.showMessageDialog(new JFrame(), "ПОБЕДА!", "Результат матча", JOptionPane.INFORMATION_MESSAGE);
			} else if (game.currentGameState() == GameState.FAIL) {
				JOptionPane.showMessageDialog(new JFrame(), "ПОРАЖЕНИЕ!", "Результат матча", JOptionPane.ERROR_MESSAGE);
			}
			newGame();
		}
		scoreLabel.setText("Счёт: " + game.score);
	}

	private Font font = null;

	private Font getFont(int size) {
		if (font == null || font.getSize() != size) {
			font = new Font("Comic Sans MS", Font.BOLD, size);
		}
		return font;
	}

	private void paintCell(int row, int column, Graphics2D g2d, int cellWidth, int cellHeight) {
		int cellValue = game.getCellValue(row, column);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (cellValue <= 0) {
			return;
		}
		Color color = COLORS[cellValue - 1];

		int size = Math.min(cellWidth, cellHeight);
		int bound = (int) Math.round(size * 0.1);

		g2d.setColor(color);
		g2d.fillRoundRect(bound, bound, size - 2 * bound, size - 2 * bound, bound * 3, bound * 3);
		g2d.setColor(Color.DARK_GRAY);
		g2d.drawRoundRect(bound, bound, size - 2 * bound, size - 2 * bound, bound * 3, bound * 3);

		g2d.setFont(getFont(size - 2 * bound));
		g2d.setColor(DrawUtils.getContrastColor(color));
		DrawUtils.drawStringInCenter(g2d, font, "" + cellValue, 0, 0, cellWidth, (int) Math.round(cellHeight * 0.95));
	}

	private void newGame() {
		selectedCellRow = -1;
		selectedCellCol = -1;
		if (params.getRowCount() * params.getColumnCount() < 4) {
			JOptionPane.showMessageDialog(new JFrame(), "Введите такие значения ширины и высоты поля, чтобы клетки было хотя бы 3!", "", JOptionPane.ERROR_MESSAGE);
			return;
		}
		game.startNewGame(params.getRowCount(), params.getColumnCount());
		JTableUtils.resizeJTable(gameTable,
				game.getRowCount(), game.getColCount(),
				gameTable.getRowHeight(), gameTable.getRowHeight()
		);
		updateView();
	}
}
