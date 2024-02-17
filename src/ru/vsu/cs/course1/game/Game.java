package ru.vsu.cs.course1.game;

import ru.vsu.cs.course1.game.model.GameState;

import java.util.Random;

public class Game {
	private final Random rnd = new Random();
	private int maxValue = 0;
	private int emptyCells = 3;

	public int score = 0;

	public int getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(int value) {
		maxValue = value;
	}

	private int[][] field;
	public GameState state = GameState.NOT_STARTED;

	public Game() {
	}


	public int getRowCount() {
		return field == null ? 0 : field.length;
	}

	public int getColCount() {
		return field == null ? 0 : field[0].length;
	}

	public GameState currentGameState() {
		if (getMaxValue() >= 12) {
			return GameState.WIN;
		} else if (!isFieldNotLost()) {
			return GameState.FAIL;
		} else {
			return GameState.PLAYING;
		}
	}

	/**
	 * Проверка, существуют ли вокруг клетки клетки такого же номинала
	 *
	 * @param check игровое поле
	 * @param row   строка клетки, которую нужно проверить
	 * @param col   столбец клетки, которую нужно проверить
	 * @return <p>
	 * true - существуют
	 * </p>
	 * <p>
	 * false - не существуют
	 * </p>
	 */
	private boolean hasNeighbourCellsWithSameNominal(int[][] check, int row, int col) {
		int chk = check[row][col];
		boolean existsLeftSide = (col - 1 >= 0);
		boolean existsRightSide = (col + 1 < check[0].length);
		boolean existsTopSide = (row - 1 >= 0);
		boolean existsDownSide = (row + 1 < check.length);
		boolean ans = false;
		if (existsTopSide) {
			ans = chk == check[row - 1][col];
		}
		if (existsDownSide) {
			ans = ans || chk == check[row + 1][col];
		}
		if (existsLeftSide) {
			ans = ans || chk == check[row][col - 1];
		}
		if (existsRightSide) {
			ans = ans || chk == check[row][col + 1];
		}

		return ans;
	}

	/**
	 * Функция для проверки поля на соответствие состоянию поражения (эффективно)
	 */
	private boolean isFieldNotLost() {
		int countEmpty = 0;
		boolean notCheckOtherRows = false;
		// Проверка на наличие пустых элементов
		for (int[] row : field) {
			if (notCheckOtherRows) break;
			for (int element : row) {
				if (element == 0) {
					countEmpty++;
				}
				if (countEmpty == 3) {
					notCheckOtherRows = true;
					break;
				}
			}
		}
		// Вспомогательное установление количества пустых ячеек - используется при создании новой клетки
		emptyCells = countEmpty;
		//System.out.println("Пустых клеток по расчёту (максимум 3): " + emptyCells);
		if (countEmpty != 0) return true;
		// Проверка на отсутствие у каждой клетки соседней клетки того же номинала
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[i].length; j++) {
				if (hasNeighbourCellsWithSameNominal(field, i, j)) {
					return true;
				}
			}
		}
		// Если обе проверки не были успешно пройдены - игра окончена.
		return false;
	}

	private void createRandomCell(int howManyToCreate) {
		int rowRand = rnd.nextInt(0, getRowCount());
		int colRand = rnd.nextInt(0, getColCount());
		int count = 0;
		// Минимум берётся чтобы не подбирались пустые клетки вечно, когда их просто нет.
		while (count < Math.min(howManyToCreate, emptyCells)) {
			// Проверка, чтобы не было попытки создать клетку в месте, где она уже существует
			while (field[rowRand][colRand] != 0) {
				rowRand = rnd.nextInt(0, getRowCount());
				colRand = rnd.nextInt(0, getColCount());
				//System.err.println("Поиск не удался");
			}
			if (maxValue == 2) {
				field[rowRand][colRand] = 2;
			} else if (maxValue == 0) {
				field[rowRand][colRand] = rnd.nextInt(1, 4);
			} else {
				int r = rnd.nextInt(1, maxValue / 2 + 1);
				field[rowRand][colRand] = r;
			}
			count++;
		}
	}

	/**
	 * Запуск новой игры
	 *
	 * @param colCount количество столбцов
	 * @param rowCount количество строк
	 */
	public void startNewGame(int colCount, int rowCount) {
		field = new int[colCount][rowCount];
		// Создание игрового поля
		for (int i = 0; i < colCount; i++) {
			for (int j = 0; j < rowCount; j++) {
				// Заполнение поля пустыми ячейками
				field[i][j] = 0;
			}
		}
		// Обнуление числа очков
		score = 0;
		// Установление числа пустых клеток в 3, чтобы могли создаться хотя бы 3 начальные клетки.
		// Далее они уже рассчитываются при проверке поля на выигрыш / проигрыш
		// (по идее это происходит во время перерисовки во FrameMain)
		emptyCells = 5;
		// Обнуление счётчика максимального набранного числа
		// (для появления клеток правильного номинала в последующих раундах)
		setMaxValue(0);
		// Создание 3 стартовых ячеек
		createRandomCell(Math.min(3, colCount * rowCount));
		state = GameState.PLAYING;
	}

	private int[][] generateMatrixForPathfinding(int[][] arr) {
		int[][] ans = new int[arr.length][];
		for (int i = 0; i < arr.length; i++) {
			int[] tmpArr = new int[arr[i].length];
			for (int j = 0; j < arr[i].length; j++) {
				tmpArr[j] = (arr[i][j] == 0 ? 0 : -1);
			}
			ans[i] = tmpArr;
		}
		return ans;
	}

	/**
	 * Функция для заполнения массива результатами поиска пути относительно клетки [row][column]
	 *
	 * @param maze массив вспомогательного двумерного массива игрового поля, заполняемый по мере выполнения
	 * @param row  текущая строка клетки, относительно которой проверяется
	 * @param col  текущий столбец
	 * @param step шаг за который можно дойти до клетки
	 */
	private void mazePathFinder(int[][] maze, int row, int col, int step) {
		int rowCount = maze.length;
		int colCount = maze[0].length;

		if (row < 0 || row >= rowCount ||
				col < 0 || col >= colCount) {
			return;
		}
		if (maze[row][col] != 0) {
			return;
		}
		maze[row][col] = step;

		mazePathFinder(maze, row - 1, col, step + 1);
		mazePathFinder(maze, row, col - 1, step + 1);
		mazePathFinder(maze, row, col + 1, step + 1);
		mazePathFinder(maze, row + 1, col, step + 1);
	}

	/**
	 * Функция, которая проверяет, существует ли путь между клетками
	 *
	 * @param row1 строка 1 клетки
	 * @param col1 столбец 1 клетки
	 * @param row2 строка 2 клетки
	 * @param col2 столбец 2 клетки
	 */
	public boolean doesPathExist(int row1, int col1, int row2, int col2) {
		int[][] maze = generateMatrixForPathfinding(field);

		maze[row1][col1] = 0;
		maze[row2][col2] = 0;
		mazePathFinder(maze, row1, col1, 1);
		//System.err.println("Шагов надо: " + maze[row2][col2]);
		return (maze[row2][col2] > 0);
	}

	/**
	 * Функция объединения клеток с добавлением новой случайной клетки
	 */
	public void mergeCells(int fromRow, int fromColumn, int toRow, int toColumn) {
		int fromCellValue = field[fromRow][fromColumn];
		int toCellValue = field[toRow][toColumn];
		boolean createRandomCells = toCellValue == fromCellValue || toCellValue == 0;
		int createCellsNumber = 5;
		if (toCellValue != 0 && toCellValue == fromCellValue) {
			emptyCells++;
			//System.err.println("Объединение клетки");
			createCellsNumber = 1;
			score += toCellValue + 1;
			++field[toRow][toColumn];
			if (field[toRow][toColumn] > getMaxValue()) {
				setMaxValue(field[toRow][toColumn]);
			}
			field[fromRow][fromColumn] = 0;
		} else if (field[toRow][toColumn] == 0 && field[fromRow][fromColumn] != 0) {
			//System.err.println("Перемещение клетки");
			field[toRow][toColumn] = field[fromRow][fromColumn];
			field[fromRow][fromColumn] = 0;
		}
		if (createRandomCells) createRandomCell(Math.min(createCellsNumber, emptyCells));
	}

	/**
	 * Метод получения значения ячейки (используется во внешнем контексте).
	 * Во внутреннем контексте используется обращение к матрице поля напрямую.
	 * @param row строка
	 * @param col столбец
	 * @return значение клетки по заданной строке и заданному столбцу
	 */
	public int getCellValue(int row, int col) {
		int rowCount = getRowCount(), colCount = getColCount();
		if (row < 0 || row >= rowCount || col < 0 || col >= colCount) {
			return 0;
		}
		return field[row][col];
	}
}