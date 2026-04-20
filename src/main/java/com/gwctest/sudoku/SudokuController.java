package com.gwctest.sudoku;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import java.util.*;

public class SudokuController {

    @FXML private GridPane gridPane;
    @FXML private Label boardLabel;

    private final TextField[][] cells = new TextField[9][9];
    private final StackPane[][] cellPanes = new StackPane[9][9];

    private int[][] initialBoard;
    private int boardIndex = 0;
    private final List<int[][]> boardLibrary = new ArrayList<>();

    private boolean isKillerMode = false;
    private KillerBoard currentKillerBoard;
    private int killerBoardIndex = 0;
    private final List<KillerBoard> killerLibrary = new ArrayList<>();

    private volatile boolean isSolving = false;

    @FXML
    public void initialize() {
        loadStandardLibrary();
        loadKillerLibrary();
        initialBoard = boardLibrary.get(0);
        buildGrid();
    }

    @FXML
    public void handleToggleMode() {
        if (isSolving) return;
        isKillerMode = !isKillerMode;
        if (isKillerMode) {
            currentKillerBoard = killerLibrary.get(killerBoardIndex);
        } else {
            initialBoard = boardLibrary.get(boardIndex);
        }
        buildGrid();
    }

    @FXML
    public void handleReset() {
        if (isSolving) return;
        if (isKillerMode) {
            killerBoardIndex = (killerBoardIndex + 1) % killerLibrary.size();
            currentKillerBoard = killerLibrary.get(killerBoardIndex);
        } else {
            boardIndex = (boardIndex + 1) % boardLibrary.size();
            initialBoard = boardLibrary.get(boardIndex);
        }
        buildGrid();
    }

    private void buildGrid() {
        gridPane.getChildren().clear();

        if (isKillerMode) {
            boardLabel.setText("Killer Sudoku: " + (killerBoardIndex + 1) + " / " + killerLibrary.size());
            buildKillerGrid();
        } else {
            boardLabel.setText("Classic Sudoku: " + (boardIndex + 1) + " / " + boardLibrary.size());
            buildStandardGrid();
        }
    }

    //grid builder for sudoku below

    private void buildStandardGrid() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                TextField tf = new TextField();
                tf.getStyleClass().add("sudoku-cell");

                String top = (r % 3 == 0) ? "2px" : "0.5px";
                String left = (c % 3 == 0) ? "2px" : "0.5px";
                String bottom = (r == 8) ? "2px" : "0.5px";
                String right = (c == 8) ? "2px" : "0.5px";
                tf.setStyle("-fx-border-color: #2c3e50; -fx-border-width: "+top+" "+right+" "+bottom+" "+left+"; -fx-border-style: solid;");

                if (initialBoard[r][c] != 0) {
                    tf.setText(String.valueOf(initialBoard[r][c]));
                    tf.setEditable(false);
                    tf.getStyleClass().add("sudoku-cell-initial");
                } else {
                    tf.getStyleClass().add("sudoku-cell-user");
                    addInputValidation(tf, r, c);
                }
                cells[r][c] = tf;
                gridPane.add(tf, c, r);
            }
        }
    }

    private void buildKillerGrid() {
        int[][] startVals = currentKillerBoard.initialNumbers;
        int[][] cages = currentKillerBoard.cageMap;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                StackPane pane = new StackPane();
                pane.getStyleClass().add("sudoku-cell");

                applyKillerBorders(pane, r, c, cages);

                TextField tf = new TextField();
                tf.getStyleClass().addAll("sudoku-cell", "killer-input");
                tf.setStyle("-fx-border-width: 0;");

                if (isTopLeftOfCage(r, c, cages)) {
                    int cageId = cages[r][c];
                    Label sumLabel = new Label(String.valueOf(currentKillerBoard.cageSums.get(cageId)));
                    sumLabel.getStyleClass().add("cage-sum-label");
                    StackPane.setAlignment(sumLabel, Pos.TOP_LEFT);
                    pane.getChildren().add(sumLabel);
                }

                if (startVals[r][c] != 0) {
                    tf.setText(String.valueOf(startVals[r][c]));
                    tf.setEditable(false);
                    tf.getStyleClass().add("sudoku-cell-initial");
                } else {
                    tf.getStyleClass().add("sudoku-cell-user");
                    addInputValidation(tf, r, c);
                }

                pane.getChildren().add(tf);
                cells[r][c] = tf;
                cellPanes[r][c] = pane;
                gridPane.add(pane, c, r);
            }
        }
    }

    private void applyKillerBorders(StackPane pane, int r, int c, int[][] cages) {
        int cageId = cages[r][c];
        String tStyle = (r % 3 == 0) ? "solid" : (r > 0 && cages[r-1][c] != cageId) ? "dashed" : "solid";
        String rStyle = (c == 8 || (c+1) % 3 == 0) ? "solid" : (c < 8 && cages[r][c+1] != cageId) ? "dashed" : "solid";
        String bStyle = (r == 8 || (r+1) % 3 == 0) ? "solid" : (r < 8 && cages[r+1][c] != cageId) ? "dashed" : "solid";
        String lStyle = (c % 3 == 0) ? "solid" : (c > 0 && cages[r][c-1] != cageId) ? "dashed" : "solid";

        String tWidth = (r % 3 == 0) ? "2px" : (tStyle.equals("dashed")) ? "1.5px" : "0.2px";
        String rWidth = (c == 8 || (c+1) % 3 == 0) ? "2px" : (rStyle.equals("dashed")) ? "1.5px" : "0.2px";
        String bWidth = (r == 8 || (r+1) % 3 == 0) ? "2px" : (bStyle.equals("dashed")) ? "1.5px" : "0.2px";
        String lWidth = (c % 3 == 0) ? "2px" : (lStyle.equals("dashed")) ? "1.5px" : "0.2px";

        String tColor = tWidth.equals("0.2px") ? "#e0e0e0" : "#2c3e50";
        String rColor = rWidth.equals("0.2px") ? "#e0e0e0" : "#2c3e50";
        String bColor = bWidth.equals("0.2px") ? "#e0e0e0" : "#2c3e50";
        String lColor = lWidth.equals("0.2px") ? "#e0e0e0" : "#2c3e50";

        pane.setStyle("-fx-border-style: " + tStyle + " " + rStyle + " " + bStyle + " " + lStyle + "; " +
                "-fx-border-width: " + tWidth + " " + rWidth + " " + bWidth + " " + lWidth + "; " +
                "-fx-border-color: " + tColor + " " + rColor + " " + bColor + " " + lColor + ";");
    }

    private boolean isTopLeftOfCage(int r, int c, int[][] cages) {
        int id = cages[r][c];
        int minR = 9, minC = 9;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cages[i][j] == id) {
                    if (i < minR) { minR = i; minC = j; }
                    else if (i == minR && j < minC) { minC = j; }
                }
            }
        }
        return (r == minR && c == minC);
    }

    //algo and validation below

    private void addInputValidation(TextField tf, int r, int c) {
        tf.textProperty().addListener((obs, oldV, newV) -> {
            tf.getStyleClass().remove("sudoku-cell-error");
            if (isKillerMode) cellPanes[r][c].getStyleClass().remove("sudoku-cell-error");

            if (newV.isEmpty()) return;
            if (!newV.matches("[1-9]")) { Platform.runLater(() -> tf.setText("")); return; }

            int val = Integer.parseInt(newV);
            if (!isSafe(getBoardSnapshot(), r, c, val)) {
                tf.getStyleClass().add("sudoku-cell-error");
                if (isKillerMode) cellPanes[r][c].getStyleClass().add("sudoku-cell-error");
            }
        });
    }

    @FXML
    public void handleAutoSolve() {
        if (isSolving) return;
        isSolving = true;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int startVal = isKillerMode ? currentKillerBoard.initialNumbers[r][c] : initialBoard[r][c];
                if (startVal == 0) {
                    cells[r][c].setText("");
                    cells[r][c].getStyleClass().removeAll("sudoku-cell-error", "sudoku-cell-solved");
                    if (isKillerMode) cellPanes[r][c].getStyleClass().removeAll("sudoku-cell-error");
                }
            }
        }

        new Thread(() -> {
            int[][] boardToSolve = copyBoard(isKillerMode ? currentKillerBoard.initialNumbers : initialBoard);
            solveCSP(boardToSolve);
            isSolving = false;
        }).start();
    }

    private boolean solveCSP(int[][] board) {
        int[] bestCell = findMRVCell(board);

        if (bestCell == null) return true; //Win: Board is full and valid
        if (bestCell[0] == -1) return false; //Dead End: force backtrack

        int r = bestCell[0], c = bestCell[1];

        for (int val : getCandidates(board, r, c)) {
            board[r][c] = val;
            updateUI(r, c, val);

            if (solveCSP(board)) return true;

            board[r][c] = 0;
            updateUI(r, c, 0);
        }
        return false;
    }

    private int[] findMRVCell(int[][] board) {
        int minCandidates = 10;
        int[] bestCell = null;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] == 0) {
                    int numCandidates = getCandidates(board, r, c).size();

                    if (numCandidates == 0) return new int[]{-1, -1}; //trigger dead end

                    if (numCandidates < minCandidates) {
                        minCandidates = numCandidates;
                        bestCell = new int[]{r, c};
                    }
                }
            }
        }
        return bestCell; //returns null if no empty cells are found
    }

    private List<Integer> getCandidates(int[][] b, int r, int c) {
        List<Integer> list = new ArrayList<>();
        for (int n = 1; n <= 9; n++) if (isSafe(b, r, c, n)) list.add(n);
        return list;
    }

    private boolean isSafe(int[][] b, int r, int c, int n) {
        //normal sudok Row/Col/Box Rules
        for (int i = 0; i < 9; i++) if (b[r][i] == n || b[i][c] == n) return false;
        int rs = r - r % 3, cs = c - c % 3;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) if (b[rs+i][cs+j] == n) return false;

        //killersudoku cage rules
        if (isKillerMode) {
            int cageId = currentKillerBoard.cageMap[r][c];
            int targetSum = currentKillerBoard.cageSums.get(cageId);
            int currentSum = n;
            int emptyCells = 0;

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (currentKillerBoard.cageMap[i][j] == cageId && !(i == r && j == c)) {
                        if (b[i][j] == n) return false; //duplicate in cage
                        if (b[i][j] != 0) currentSum += b[i][j];
                        else emptyCells++;
                    }
                }
            }

            if (currentSum > targetSum) return false; //over sum
            if (emptyCells == 0 && currentSum != targetSum) return false; //full but wrong sum

            //checks if the remaining empty cells mathematically reach the target
            int maxPossibleRemaining = 0;
            for (int i = 0; i < emptyCells; i++) maxPossibleRemaining += (9 - i);
            if (currentSum + maxPossibleRemaining < targetSum) return false;

            int minPossibleRemaining = 0;
            for (int i = 0; i < emptyCells; i++) minPossibleRemaining += (1 + i);
            if (currentSum + minPossibleRemaining > targetSum) return false;
        }
        return true;
    }

    private void updateUI(int r, int c, int val) {
        Platform.runLater(() -> {
            cells[r][c].setText(val == 0 ? "" : String.valueOf(val));
            if (val != 0) cells[r][c].getStyleClass().add("sudoku-cell-solved");
        });
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int[][] getBoardSnapshot() {
        int[][] b = new int[9][9];
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
            String s = cells[r][c].getText();
            b[r][c] = s.isEmpty() ? 0 : Integer.parseInt(s);
        }
        return b;
    }

    private int[][] copyBoard(int[][] s) {
        int[][] d = new int[9][9];
        for (int i = 0; i < 9; i++) System.arraycopy(s[i], 0, d[i], 0, 9);
        return d;
    }

    static class KillerBoard {
        int[][] initialNumbers;
        int[][] cageMap;
        Map<Integer, Integer> cageSums;

        public KillerBoard(int[][] initialNumbers, int[][] cageMap, Map<Integer, Integer> cageSums) {
            this.initialNumbers = initialNumbers;
            this.cageMap = cageMap;
            this.cageSums = cageSums;
        }
    }

    private void loadStandardLibrary() {
        boardLibrary.add(new int[9][9]);
        String[] boards = {
                "030490010740018000196700024000501762003027059000040300078900000429000073000370098",
                "530070000600195000098000060800060003400803001700020006060000280000419005000080079",
                "000260701680070090190004500820100040004602900050003028009300074040050036703018000",
                "100489006730000040000001295007120600500703008006095700914600000020000037800512004",
                "020608000580009700000040000370000500600000004008000013000020000009800036000306090",
                "000600400700003600000091080000000000050180003000306045040200060903000000020000100",
                "200300000804062003013800540000020390507000621032006000020009140601250809000001002",
                "000004090802970000901200300000049157013050920579120000007002603000038205020500000",
                "000000000000003085001020000000507000004000100090000000500000073002010000000040009",
                "005300000800000020070010500400005300010070006003200080060500009004000030000009700",
                "000000002000000940003000005092305074840000000067098000000706000000500003400120600",
                "039000120000907000800401006042000730000000000091000540500109002000502000083000610",
                "070000050000010000002000300000000000500040006000000000003000700000080000010000090",
                "000700000100000000000430200000000006000509000000000418000081000002000050040000300",
                "020000000000600003074080000000003002080040010600500000000010780500009000000000040",
                "100007090030020008009600500005300900010080002600004000300000010040000007007000300",
                "800000000003600000070090200050007000000045700000100030001000068008500010090000400",
                "000000900000000002000000000000000000000000000000000000000000000000000000000000000",
                "123456789000000000000000000000000000000000000000000000000000000000000000000000000"
        };

        for (String b : boards) {
            int[][] board = new int[9][9];
            for (int i = 0; i < 81; i++) {
                board[i / 9][i % 9] = Character.getNumericValue(b.charAt(i));
            }
            boardLibrary.add(board);
        }
    }

    private void loadKillerLibrary() {
        int[][] map1 = {
                {1, 1, 2, 2, 3, 3, 3, 4, 4}, {5, 1, 6, 2, 7, 7, 8, 8, 4}, {5, 6, 6, 9, 9, 7, 10,10,10},
                {11,11,12,12,13,13,14,14,14},{15,11,16,16,13,17,17,18,18},{15,15,19,16,20,20,17,21,18},
                {22,22,19,19,23,20,24,21,21},{25,22,26,26,23,23,24,24,27},{25,25,28,28,28,29,29,27,27}
        };
        Map<Integer, Integer> s1 = new HashMap<>();
        s1.put(1,15); s1.put(2,10); s1.put(3,16); s1.put(4,12); s1.put(5,9);  s1.put(6,17); s1.put(7,14); s1.put(8,7);
        s1.put(9,11); s1.put(10,21); s1.put(11,16); s1.put(12,13); s1.put(13,15); s1.put(14,19); s1.put(15,18); s1.put(16,22);
        s1.put(17,12); s1.put(18,15); s1.put(19,14); s1.put(20,11); s1.put(21,18); s1.put(22,13); s1.put(23,17); s1.put(24,16);
        s1.put(25,12); s1.put(26,9);  s1.put(27,15); s1.put(28,20); s1.put(29,8);
        killerLibrary.add(new KillerBoard(new int[9][9], map1, s1));


        int[][] map2 = {
                {1, 1, 2, 3, 3, 4, 5, 5, 6}, {1, 1, 2, 3, 3, 4, 5, 5, 6}, {7, 7, 2, 8, 8, 4, 9, 9, 6},
                {10,10,11,11,12,13,13,14,14},{10,10,15,12,12,12,16,14,14},{17,17,15,18,18,19,16,20,20},
                {21,22,22,23,24,24,25,26,26},{21,22,22,23,27,27,25,26,26},{28,28,29,29,30,30,31,31,32}
        };
        Map<Integer, Integer> s2 = new HashMap<>();
        s2.put(1,14); s2.put(2,11); s2.put(3,10); s2.put(4,20); s2.put(5,13); s2.put(6,16); s2.put(7,9); s2.put(8,12);
        s2.put(9,8); s2.put(10,21); s2.put(11,7); s2.put(12,19); s2.put(13,14); s2.put(14,24); s2.put(15,15); s2.put(16,11);
        s2.put(17,10); s2.put(18,18); s2.put(19,5); s2.put(20,13); s2.put(21,12); s2.put(22,20); s2.put(23,8); s2.put(24,14);
        s2.put(25,17); s2.put(26,22); s2.put(27,11); s2.put(28,9); s2.put(29,15); s2.put(30,12); s2.put(31,7); s2.put(32,4);
        killerLibrary.add(new KillerBoard(new int[9][9], map2, s2));

        int[][] map3 = {
                {1, 1, 1, 2, 2, 2, 3, 3, 3}, {4, 4, 5, 5, 6, 6, 7, 7, 8}, {4, 4, 5, 5, 6, 6, 7, 7, 8},
                {9, 10,10,11,11,12,12,13,13},{9, 14,14,15,15,16,16,17,17},{9, 18,18,19,19,20,20,21,21},
                {22,22,23,23,24,24,25,25,26},{27,27,28,28,29,29,30,30,26},{31,31,31,32,32,32,33,33,33}
        };
        Map<Integer, Integer> s3 = new HashMap<>();
        s3.put(1,18); s3.put(2,12); s3.put(3,15); s3.put(4,22); s3.put(5,14); s3.put(6,16); s3.put(7,10); s3.put(8,9);
        s3.put(9,14); s3.put(10,11); s3.put(11,7); s3.put(12,13); s3.put(13,15); s3.put(14,9); s3.put(15,12); s3.put(16,8);
        s3.put(17,11); s3.put(18,17); s3.put(19,10); s3.put(20,14); s3.put(21,9); s3.put(22,12); s3.put(23,16); s3.put(24,11);
        s3.put(25,13); s3.put(26,17); s3.put(27,15); s3.put(28,8); s3.put(29,10); s3.put(30,12); s3.put(31,19); s3.put(32,14); s3.put(33,12);
        killerLibrary.add(new KillerBoard(new int[9][9], map3, s3));
        //the 2 below are copies cause I wanted to have 5 baords instead of 3
        killerLibrary.add(new KillerBoard(new int[9][9], map1, s1));
        killerLibrary.add(new KillerBoard(new int[9][9], map2, s2));
    }
}