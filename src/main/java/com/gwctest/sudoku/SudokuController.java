package com.gwctest.sudoku;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;
import java.util.List;

public class SudokuController {

    @FXML private GridPane gridPane;
    private TextField[][] cells = new TextField[9][9];
    private int[][] currentInitialBoard;
    private int boardIndex = 0;
    private final List<int[][]> boardLibrary = new ArrayList<>();

    @FXML
    public void initialize() {
        loadLibrary();
        currentInitialBoard = boardLibrary.get(0);
        buildGrid();
    }

    private void buildGrid() {
        gridPane.getChildren().clear();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                TextField tf = new TextField();
                tf.getStyleClass().add("sudoku-cell");

                int top = (row % 3 == 0) ? 2 : 0;
                int left = (col % 3 == 0) ? 2 : 0;
                tf.setStyle("-fx-border-color: #2c3e50; -fx-border-width: " + top + " 1 1 " + left + ";");

                int val = currentInitialBoard[row][col];
                if (val != 0) {
                    tf.setText(String.valueOf(val));
                    tf.setEditable(false);
                    tf.getStyleClass().add("sudoku-cell-initial");
                } else {
                    tf.getStyleClass().add("sudoku-cell-user");
                    setupValidationListener(tf, row, col);
                }

                cells[row][col] = tf;
                gridPane.add(tf, col, row);
            }
        }
    }

    private void setupValidationListener(TextField tf, int row, int col) {
        tf.textProperty().addListener((obs, oldVal, newVal) -> {
            tf.getStyleClass().remove("sudoku-cell-error");
            if (!newVal.matches("[1-9]")) {
                tf.setText("");
                return;
            }

            int num = Integer.parseInt(newVal);
            if (!isSafeToPlace(row, col, num)) {
                tf.getStyleClass().add("sudoku-cell-error");
            }
        });
    }

    @FXML
    public void handleAutoSolve() {
        //discard user input, Reset board to initial state
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (currentInitialBoard[r][c] == 0) {
                    cells[r][c].setText("");
                    cells[r][c].getStyleClass().removeAll("sudoku-cell-error", "sudoku-cell-solved");
                }
            }
        }

        //run in a background thread to allow updates
        Task<Void> solveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int[][] boardToSolve = copyBoard(currentInitialBoard);
                solveAnimated(boardToSolve, 0, 0);
                return null;
            }
        };
        new Thread(solveTask).start();
    }

    private boolean solveAnimated(int[][] board, int row, int col) throws InterruptedException {
        if (row == 9) return true;

        int nextRow = (col == 8) ? row + 1 : row;
        int nextCol = (col == 8) ? 0 : col + 1;

        if (board[row][col] != 0) {
            return solveAnimated(board, nextRow, nextCol);
        }

        for (int num = 1; num <= 9; num++) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num;

                //Update UI
                final int r = row, c = col, n = num;
                Platform.runLater(() -> {
                    cells[r][c].setText(String.valueOf(n));
                    cells[r][c].getStyleClass().add("sudoku-cell-solved");
                });

                Thread.sleep(10); // Speed of the animation (ms)

                if (solveAnimated(board, nextRow, nextCol)) return true;

                board[row][col] = 0;
                Platform.runLater(() -> cells[r][c].setText(""));
            }
        }
        return false;
    }

    private boolean isSafeToPlace(int row, int col, int num) {
        //check current board state for clashes
        for (int i = 0; i < 9; i++) {
            if (i != col && cells[row][i].getText().equals(String.valueOf(num))) return false;
            if (i != row && cells[i][col].getText().equals(String.valueOf(num))) return false;
        }
        //subgrid check
        int sr = row - row % 3, sc = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if ((sr+i != row || sc+j != col) && cells[sr+i][sc+j].getText().equals(String.valueOf(num))) return false;
            }
        }
        return true;
    }

    @FXML
    public void handleReset() {
        boardIndex = (boardIndex + 1) % boardLibrary.size();
        currentInitialBoard = boardLibrary.get(boardIndex);
        buildGrid();
    }

    private void loadLibrary() {
        boardLibrary.add(new int[9][9]);

        boardLibrary.add(new int[][]{
                {0, 3, 0, 4, 9, 0, 0, 1, 0}, {7, 4, 0, 0, 1, 8, 0, 0, 0}, {1, 9, 6, 7, 0, 0, 0, 2, 4},
                {0, 0, 0, 5, 0, 1, 7, 6, 2}, {0, 0, 3, 0, 2, 7, 0, 5, 9}, {0, 0, 0, 0, 4, 0, 3, 0, 0},
                {0, 7, 8, 9, 0, 0, 0, 0, 0}, {4, 2, 9, 0, 0, 0, 0, 7, 3}, {0, 0, 0, 3, 7, 0, 0, 9, 8}
        });

        boardLibrary.add(new int[][]{
                {5, 3, 0, 0, 7, 0, 0, 0, 0}, {6, 0, 0, 1, 9, 5, 0, 0, 0}, {0, 9, 8, 0, 0, 0, 0, 6, 0},
                {8, 0, 0, 0, 6, 0, 0, 0, 3}, {4, 0, 0, 8, 0, 3, 0, 0, 1}, {7, 0, 0, 0, 2, 0, 0, 0, 6},
                {0, 6, 0, 0, 0, 0, 2, 8, 0}, {0, 0, 0, 4, 1, 9, 0, 0, 5}, {0, 0, 0, 0, 8, 0, 0, 7, 9}
        });

        boardLibrary.add(new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 3, 0, 8, 5}, {0, 0, 1, 0, 2, 0, 0, 0, 0},
                {0, 0, 0, 5, 0, 7, 0, 0, 0}, {0, 0, 4, 0, 0, 0, 1, 0, 0}, {0, 9, 0, 0, 0, 0, 0, 0, 0},
                {5, 0, 0, 0, 0, 0, 0, 7, 3}, {0, 0, 2, 0, 1, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 9}
        });

        // 4: Expert
        boardLibrary.add(new int[][]{
                {8, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 3, 6, 0, 0, 0, 0, 0}, {0, 7, 0, 0, 9, 0, 2, 0, 0},
                {0, 5, 0, 0, 0, 7, 0, 0, 0}, {0, 0, 0, 0, 4, 5, 7, 0, 0}, {0, 0, 0, 1, 0, 0, 0, 3, 0},
                {0, 0, 1, 0, 0, 0, 0, 6, 8}, {0, 0, 8, 5, 0, 0, 0, 1, 0}, {0, 9, 0, 0, 0, 0, 4, 0, 0}
        });

        boardLibrary.add(new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 1, 0, 0, 2, 0, 0, 3, 0}, {0, 0, 4, 0, 0, 0, 5, 0, 0},
                {0, 0, 0, 6, 0, 7, 0, 0, 0}, {0, 8, 0, 0, 9, 0, 0, 1, 0}, {0, 0, 0, 2, 0, 3, 0, 0, 0},
                {0, 0, 5, 0, 0, 0, 6, 0, 0}, {0, 7, 0, 0, 8, 0, 0, 9, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}
        });

        //random boards coz variaty
        boardLibrary.add(new int[][]{{0,0,0,2,6,0,7,0,1}, {6,8,0,0,7,0,0,9,0}, {1,9,0,0,0,4,5,0,0}, {8,2,0,1,0,0,0,4,0}, {0,0,4,6,0,2,9,0,0}, {0,5,0,0,0,3,0,2,8}, {0,0,9,3,0,0,0,7,4}, {0,4,0,0,5,0,0,3,6}, {7,0,3,0,1,8,0,0,0}});
        boardLibrary.add(new int[][]{{1,0,0,4,8,9,0,0,6}, {7,3,0,0,0,0,0,4,0}, {0,0,0,0,0,1,2,9,5}, {0,0,7,1,2,0,6,0,0}, {5,0,0,7,0,3,0,0,8}, {0,0,6,0,9,5,7,0,0}, {9,1,4,6,0,0,0,0,0}, {0,2,0,0,0,0,0,3,7}, {8,0,0,5,1,2,0,0,4}});
        boardLibrary.add(new int[][]{{0,2,0,6,0,8,0,0,0}, {5,8,0,0,0,9,7,0,0}, {0,0,0,0,4,0,0,0,0}, {3,7,0,0,0,0,5,0,0}, {6,0,0,0,0,0,0,0,4}, {0,0,8,0,0,0,0,1,3}, {0,0,0,0,2,0,0,0,0}, {0,0,9,8,0,0,0,3,6}, {0,0,0,3,0,6,0,9,0}});
        boardLibrary.add(new int[][]{{0,0,0,6,0,0,4,0,0}, {7,0,0,0,0,3,6,0,0}, {0,0,0,0,9,1,0,8,0}, {0,0,0,0,0,0,0,0,0}, {0,5,0,1,8,0,0,0,3}, {0,0,0,3,0,6,0,4,5}, {0,4,0,2,0,0,0,6,0}, {9,0,3,0,0,0,0,0,0}, {0,2,0,0,0,0,1,0,0}});
        boardLibrary.add(new int[][]{{2,0,0,3,0,0,0,0,0}, {8,0,4,0,6,2,0,0,3}, {0,1,3,8,0,0,5,4,0}, {0,0,0,0,2,0,3,9,0}, {5,0,7,0,0,0,6,2,1}, {0,3,2,0,0,6,0,0,0}, {0,2,0,0,0,9,1,4,0}, {6,0,1,2,5,0,8,0,9}, {0,0,0,0,0,1,0,0,2}});
        boardLibrary.add(new int[][]{{0,0,0,0,0,4,0,9,0}, {8,0,2,9,7,0,0,0,0}, {9,0,1,2,0,0,3,0,0}, {0,0,0,0,4,9,1,5,7}, {0,1,3,0,5,0,9,2,0}, {5,7,9,1,2,0,0,0,0}, {0,0,7,0,0,2,6,0,3}, {0,0,0,0,3,8,2,0,5}, {0,2,0,5,0,0,0,0,0}});
        boardLibrary.add(new int[][]{{0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,3,0,8,5}, {0,0,1,0,2,0,0,0,0}, {0,0,0,5,0,7,0,0,0}, {0,0,4,0,0,0,1,0,0}, {0,9,0,0,0,0,0,0,0}, {5,0,0,0,0,0,0,7,3}, {0,0,2,0,1,0,0,0,0}, {0,0,0,0,4,0,0,0,9}});
        boardLibrary.add(new int[][]{{0,0,5,3,0,0,0,0,0}, {8,0,0,0,0,0,0,2,0}, {0,7,0,0,1,0,5,0,0}, {4,0,0,0,0,5,3,0,0}, {0,1,0,0,7,0,0,0,6}, {0,0,3,2,0,0,0,8,0}, {0,6,0,5,0,0,0,0,9}, {0,0,4,0,0,0,0,3,0}, {0,0,0,0,0,9,7,0,0}});
        boardLibrary.add(new int[][]{{0,0,0,0,0,0,0,0,2}, {0,0,0,0,0,0,9,4,0}, {0,0,3,0,0,0,0,0,5}, {0,9,2,3,0,5,0,7,4}, {8,4,0,0,0,0,0,0,0}, {0,6,7,0,9,8,0,0,0}, {0,0,0,7,0,6,0,0,0}, {0,0,0,5,0,0,0,0,3}, {4,0,0,1,2,0,6,0,0}});
        boardLibrary.add(new int[][]{{0,3,9,0,0,0,1,2,0}, {0,0,0,9,0,7,0,0,0}, {8,0,0,4,0,1,0,0,6}, {0,4,2,0,0,0,7,3,0}, {0,0,0,0,0,0,0,0,0}, {0,9,1,0,0,0,5,4,0}, {5,0,0,1,0,9,0,0,2}, {0,0,0,5,0,2,0,0,0}, {0,8,3,0,0,0,6,1,0}});
        boardLibrary.add(new int[][]{{0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,3,0,8,5}, {0,0,1,0,2,0,0,0,0}, {0,0,0,5,0,7,0,0,0}, {0,0,4,0,0,0,1,0,0}, {0,9,0,0,0,0,0,0,0}, {5,0,0,0,0,0,0,7,3}, {0,0,2,0,1,0,0,0,0}, {0,0,0,0,4,0,0,0,9}});
        boardLibrary.add(new int[][]{{0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,3,0,8,5}, {0,0,1,0,2,0,0,0,0}, {0,0,0,5,0,7,0,0,0}, {0,0,4,0,0,0,1,0,0}, {0,9,0,0,0,0,0,0,0}, {5,0,0,0,0,0,0,7,3}, {0,0,2,0,1,0,0,0,0}, {0,0,0,0,4,0,0,0,9}});
        boardLibrary.add(new int[][]{{0,7,0,0,0,0,0,5,0}, {0,0,0,0,1,0,0,0,0}, {0,0,2,0,0,0,3,0,0}, {0,0,0,0,0,0,0,0,0}, {5,0,0,0,4,0,0,0,6}, {0,0,0,0,0,0,0,0,0}, {0,0,3,0,0,0,7,0,0}, {0,0,0,0,8,0,0,0,0}, {0,1,0,0,0,0,0,9,0}});
        boardLibrary.add(new int[][]{{0,0,0,7,0,0,0,0,0}, {1,0,0,0,0,0,0,0,0}, {0,0,0,4,3,0,2,0,0}, {0,0,0,0,0,0,0,0,6}, {0,0,0,5,0,9,0,0,0}, {0,0,0,0,0,0,4,1,8}, {0,0,0,0,8,1,0,0,0}, {0,0,2,0,0,0,0,5,0}, {0,4,0,0,0,0,3,0,0}});
        boardLibrary.add(new int[][]{{0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0,1}});
    }

    //validation for the algorithm
    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == num || board[i][col] == num) return false;
        }
        int sr = row - row % 3, sc = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[sr + i][sc + j] == num) return false;
            }
        }
        return true;
    }

    private int[][] copyBoard(int[][] original) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) System.arraycopy(original[i], 0, copy[i], 0, 9);
        return copy;
    }
}