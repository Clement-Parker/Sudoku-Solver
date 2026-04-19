module com.mausbyte.sudoku {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.gwctest.sudoku to javafx.fxml;

    exports com.gwctest.sudoku;
}
