import ch.astorm.jchess.JChessGame;
import ch.astorm.jchess.core.Color;
import ch.astorm.jchess.core.Coordinate;
import ch.astorm.jchess.core.Moveable;
import ch.astorm.jchess.core.Position;
import ch.astorm.jchess.core.entities.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.application.Platform;


import java.util.Map;

public class ChessGUI extends Application {

    private JChessGame game;
    private static ChessGUI instance;
    private GridPane gridPane;

    public ChessGUI(JChessGame game) {
        this.game = game;
        instance = this;
    }

    public void updateGame(JChessGame game) {
        this.game = game;
        instance = this;
        Platform.runLater(this::updateGameState);
    }

    public static ChessGUI getInstance() {
        return instance;
    }


    public ChessGUI() {
        this.game = JChessGame.newGame();
        instance = this;
    }


    @Override
    public void start(Stage primaryStage) {
        gridPane = new GridPane();

        // Set the preferred width and height of the cells to 300
        for (int i = 0; i < 8; i++) {
            ColumnConstraints column = new ColumnConstraints(100);
            RowConstraints row = new RowConstraints(100);
            gridPane.getColumnConstraints().add(column);
            gridPane.getRowConstraints().add(row);
        }

        Scene scene = new Scene(gridPane, 800, 800);

        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateGameState();
    }

    private void updateGameState() {
        // If gridPane is null, return without doing anything
        if (gridPane == null) {
            return;
        }

        Position position = game.getPosition();
        Map<Coordinate, Moveable> moveables = position.getMoveables();

        // Clear the grid
        gridPane.getChildren().clear();

        // Loop over the cells in the grid
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Coordinate coordinate = new Coordinate(col, row);
                Moveable moveable = moveables.get(coordinate);

                // Create a rectangle for the cell
                Rectangle rectangle = new Rectangle(100, 100);
                if ((row + col) % 2 == 0) {
                    rectangle.setFill(javafx.scene.paint.Color.WHITE);
                } else {
                    rectangle.setFill(javafx.scene.paint.Color.DARKGRAY);
                }

                // Create a stack pane and add the rectangle to it
                StackPane stackPane = new StackPane();
                stackPane.getChildren().add(rectangle);

                // If there is a piece at this position, add it to the cell
                if (moveable != null) {
                    ImageView imageView = getImageView(moveable);
                    stackPane.getChildren().add(imageView);
                }

                gridPane.add(stackPane, row, 7 - col);
            }
        }
    }

    private static ImageView getImageView(Moveable moveable) {
        String color = moveable.getColor() == Color.WHITE ? "white" : "black";
        String piece = "";

        switch (moveable) {
            case Pawn pawn -> piece = "pawn";
            case Knight knight -> piece = "knight";
            case Bishop bishop -> piece = "bishop";
            case Rook rook -> piece = "rook";
            case Queen queen -> piece = "queen";
            case King king -> piece = "king";
            default -> {
            }
        }

        String imagePath = "/img/" + color + "_" + piece + ".png";
        Image image = new Image(imagePath);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        return imageView;
    }
}