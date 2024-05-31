import ch.astorm.jchess.JChessGame;
import ch.astorm.jchess.core.Move;
import ch.astorm.jchess.util.UnicodePositionRenderer;


import java.util.List;


public class Main {
    public static void main(String[] args) {
        // Create a new game
        JChessGame game1 = JChessGame.newGame();
        JChessGame game = JChessGame.newGame();

        // Launch the GUI in a new thread
        new Thread(() -> ChessGUI.launch(ChessGUI.class)).start();

        // Wait for the GUI to be initialized
        while (ChessGUI.getInstance() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ChessGUI gui = ChessGUI.getInstance();

        AI ai = new AI(game, 3);
        while (!game.getStatus().isFinished()) {

            // Generate all possible moves for the current position
            List<Move> legalMoves = game.getAvailableMoves();
            // Make a move
            if (!legalMoves.isEmpty()) {
                Move move = ai.makeMove(game1);
                game.play(move);
                game1.play(move);

                System.out.println("Im here");

                UnicodePositionRenderer.render(System.out, game.getPosition());

                gui.updateGame(game);
            }
        }
        System.out.println(game.getStatus());
    }
}