import ch.astorm.jchess.JChessGame;
import ch.astorm.jchess.core.*;
import ch.astorm.jchess.core.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.astorm.jchess.JChessGame.Status.WIN_BLACK;
import static ch.astorm.jchess.JChessGame.Status.WIN_WHITE;

public class AI {
    public JChessGame game;
    public Color color;
    public boolean isMaximizer;
    public int depth;
    public Map<Class<? extends Moveable>, Integer> pieceValues = new HashMap<>();

    public AI(JChessGame game, int depth) {
        // Create a new game with the same position as the given game
        this.game = game;
        this.color = game.getColorOnMove();
        this.isMaximizer = color == Color.WHITE;
        this.depth = depth;
        this.pieceValues.put(Pawn.class, 1);
        this.pieceValues.put(Knight.class, 3);
        this.pieceValues.put(Bishop.class, 3);
        this.pieceValues.put(Rook.class, 5);
        this.pieceValues.put(Queen.class, 9);
        this.pieceValues.put(King.class, 1000);
    }

    public Move makeMove(JChessGame game) {
        this.game = game;
        this.color = game.getColorOnMove();
        this.isMaximizer = color == Color.WHITE;

        // Generate all possible moves for the current position
        List<Move> legalMoves = this.game.getAvailableMoves();
        Move bestMove = legalMoves.getFirst();
        if (this.isMaximizer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                this.game.play(move);
                int val = this.minimax(this.depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                if (val > maxEval) {
                    maxEval = val;
                    bestMove = move;
                }
                this.game.back();
            }
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                this.game.play(move);
                int val = this.minimax(this.depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
                if (val < minEval) {
                    minEval = val;
                    bestMove = move;
                }
                this.game.back();
            }
        }
        System.out.println("Best move: " + bestMove);
        return bestMove;

    }

    public int evaluate() {
        if (this.game.getStatus().isFinished()) {
            if (this.game.getStatus() == WIN_WHITE) {
                return Integer.MAX_VALUE;
            } else if (this.game.getStatus() == WIN_BLACK) {
                return Integer.MIN_VALUE;
            } else {
                return 0;
            }
        }
        Position position = this.game.getPosition();

        // Get all the white and black entities in the position
        List<Moveable> whiteMoveables = position.getMoveables(Color.WHITE);
        List<Moveable> blackMoveables = position.getMoveables(Color.BLACK);

        // Initialize the evaluation value
        int evaluation = 0;

        // Evaluate all the white and black entities
        evaluation += evaluateMoveables(whiteMoveables, 1);
        evaluation -= evaluateMoveables(blackMoveables, -1);

        // Return the evaluation value
        return evaluation;
    }

    private int evaluateMoveables(List<Moveable> moveables, int factor) {
        int evaluation = 0;

        // Loop through all the entities
        for (Moveable moveable : moveables) {
            // Get the value of the entity
            int value = this.pieceValues.get(moveable.getClass());

            // Get the number of moves the entity can make
            int moves = game.getAvailableMoves(moveable).size();

            // Add the value of the entity to the evaluation value
            evaluation += factor * (value + moves);
        }

        return evaluation;
    }

    public int minimax(int depth, int alpha, int beta, boolean isMaximizer) {
        if (depth == 0) {
            return quiescenceSearch(alpha, beta, isMaximizer, 5);
        }

        List<Move> legalMoves = game.getAvailableMoves();
        if (isMaximizer) {
            for (Move move : legalMoves) {
                game.play(move);
                int eval = minimax(depth - 1, alpha, beta, false);
                game.back();
                if (eval >= beta) {
                    return beta;
                }
                if (eval > alpha) {
                    alpha = eval;
                }
            }
            return alpha;
        } else {
            for (Move move : legalMoves) {
                game.play(move);
                int eval = minimax(depth - 1, alpha, beta, true);
                game.back();
                if (eval <= alpha) {
                    return alpha;
                }
                if (eval < beta) {
                    beta = eval;
                }
            }
            return beta;
        }
    }

    public int quiescenceSearch(int alpha, int beta, boolean isMaximizer, int depth) {
        int standPat = evaluate();
        if (isMaximizer) {
            if (standPat >= beta) {
                return beta;
            }
            if (alpha < standPat) {
                alpha = standPat;
            }
        } else {
            if (standPat <= alpha) {
                return alpha;
            }
            if (beta > standPat) {
                beta = standPat;
            }
        }

        if (depth == 0) {
            return standPat;
        }

        List<Move> captures = new ArrayList<>();
        List<Move> moves = game.getAvailableMoves();

        for (Move move : moves) {
            if (move.getCapturedEntity() != null) {
                captures.add(move);
            }
        }

        if (isMaximizer) {
            for (Move move : captures) {
                game.play(move);
                int score = quiescenceSearch(alpha, beta, false, depth-1);
                game.back();
                if (score >= beta) {
                    return beta;
                }
                if (score > alpha) {
                    alpha = score;
                }
            }
        } else {
            for (Move move : captures) {
                game.play(move);
                int score = quiescenceSearch(alpha, beta, true, depth-1);
                game.back();
                if (score <= alpha) {
                    return alpha;
                }
                if (score < beta) {
                    beta = score;
                }
            }
        }
        return isMaximizer ? alpha : beta;
    }
}
