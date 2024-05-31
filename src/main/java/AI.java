import ch.astorm.jchess.JChessGame;
import ch.astorm.jchess.core.*;
import ch.astorm.jchess.core.entities.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static ch.astorm.jchess.JChessGame.Status.WIN_BLACK;
import static ch.astorm.jchess.JChessGame.Status.WIN_WHITE;

public class AI {
    private JChessGame game;
    private Color color;
    private boolean isMaximizer;
    private int depth;
    private Map<Class<? extends Moveable>, Integer> pieceValues = new HashMap<>();
    private TranspositionTable transpositionTable = new TranspositionTable();
    private Move bestMoveFound;

    public AI(JChessGame game, int depth) {
        this.game = game;
        this.color = game.getColorOnMove();
        this.isMaximizer = color == Color.WHITE;
        this.depth = depth;
        this.pieceValues.put(Pawn.class, 10);
        this.pieceValues.put(Knight.class, 33);
        this.pieceValues.put(Bishop.class, 30);
        this.pieceValues.put(Rook.class, 50);
        this.pieceValues.put(Queen.class, 90);
        this.pieceValues.put(King.class, 1000);
        loadTranspositionTable("transpositionTable.ser");
    }

    public void saveTranspositionTable(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(transpositionTable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTranspositionTable(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            transpositionTable = (TranspositionTable) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            transpositionTable = new TranspositionTable();
        }
    }

    public List<Move> orderMoves(List<Move> moves) {
        List<Move> modifiableMoves = new ArrayList<>(moves);
        modifiableMoves.sort((move1, move2) -> {
            game.play(move1);
            int score1 = 0;
            int score2 = 0;
            try {
                score1 = evaluate();
            } finally {
                game.back();
            }
            game.play(move2);
            try {
                score2 = evaluate();
            } finally {
                game.back();
            }
            return score2 - score1;
        });
        return modifiableMoves;
    }

    public Move iterativeDeepening(JChessGame game) {
        this.game = game;
        this.color = game.getColorOnMove();
        this.isMaximizer = color == Color.WHITE;


        ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask<Move> futureTask = new FutureTask<>(() -> {

            // Generate all possible moves for the current position
            List<Move> legalMoves = this.game.getAvailableMoves();
            legalMoves = orderMoves(legalMoves);

            Move bestMove = null;
            int maxDepth = this.depth;
            for (int depth = 1; depth <= maxDepth; depth++) {
                this.depth = depth;
                bestMove = makeMove(legalMoves);
                this.bestMoveFound = bestMove;
            }
            this.depth = maxDepth;
            return bestMove;
        });

        executor.execute(futureTask);
        try {
            // Get the result of the FutureTask, but only wait for 5 seconds
            saveTranspositionTable("transpositionTable.ser");
            return futureTask.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
        } catch (TimeoutException e) {
            // The iterativeDeepening method didn't finish within 5 seconds
            // The best move found so far will be returned
        }

        // Shut down the executor
        executor.shutdown();

        saveTranspositionTable("transpositionTable.ser");
        return bestMoveFound;
    }


    public Move makeMove(List<Move> legalMoves) {
        Move bestMove = legalMoves.getFirst();
        if (this.isMaximizer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                this.game.play(move);
                try {
                    int val = this.minimax(this.depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                    if (val > maxEval) {
                        maxEval = val;
                        bestMove = move;
                    }
                } finally {
                    this.game.back();
                }
            }
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                this.game.play(move);
                try {
                    int val = this.minimax(this.depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
                    if (val < minEval) {
                        minEval = val;
                        bestMove = move;
                    }
                } finally {
                    this.game.back();
                }
            }
        }
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

        return evaluation + new Random().nextInt(2) - 1;
    }

    public String toFen(Position pos) {
        StringBuilder fen = new StringBuilder();

        Map<Moveable, String> pieceMap = new HashMap<>();
        pieceMap.put(new Pawn(Color.WHITE), "P");
        pieceMap.put(new Pawn(Color.BLACK), "p");
        pieceMap.put(new Knight(Color.WHITE), "N");
        pieceMap.put(new Knight(Color.BLACK), "n");
        pieceMap.put(new Bishop(Color.WHITE), "B");
        pieceMap.put(new Bishop(Color.BLACK), "b");
        pieceMap.put(new Rook(Color.WHITE), "R");
        pieceMap.put(new Rook(Color.BLACK), "r");
        pieceMap.put(new Queen(Color.WHITE), "Q");
        pieceMap.put(new Queen(Color.BLACK), "q");
        pieceMap.put(new King(Color.WHITE), "K");
        pieceMap.put(new King(Color.BLACK), "k");

        Map<Coordinate, Moveable> pieces = pos.getMoveables();
        int empty = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Coordinate coord = new Coordinate(i, j);
                Moveable piece = pieces.get(coord);
                if (piece == null) {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    fen.append(pieceMap.get(piece));
                }
            }
        }


        return fen.toString();

    }

    public int minimax(int depth, int alpha, int beta, boolean isMaximizer) {

        List<Integer> previousScore = transpositionTable.get(toFen(game.getPosition()));
        if (previousScore != null && previousScore.get(1) >= depth) {
            return previousScore.getFirst();
        }

        if (depth == 0) {
            return quiescenceSearch(alpha, beta, isMaximizer, 5);
        }

        List<Move> legalMoves = game.getAvailableMoves();

        legalMoves = orderMoves(legalMoves);
        if (isMaximizer) {
            for (Move move : legalMoves) {
                game.play(move);
                try {
                    int eval = minimax(depth - 1, alpha, beta, false);
                    if (eval > alpha) {
                        alpha = eval;
                    }
                    if (alpha >= beta) {
                        break;  // Beta cut-off
                    }
                } finally {
                    game.back();
                }
            }
            transpositionTable.put(toFen(game.getPosition()), alpha, this.depth - (this.depth - depth));
            return alpha;
        } else {
            for (Move move : legalMoves) {
                game.play(move);
                try {

                    int eval = minimax(depth - 1, alpha, beta, true);
                    if (eval < beta) {
                        beta = eval;
                    }
                    if (beta <= alpha) {
                        break;  // Alpha cut-off
                    }
                } finally {
                    game.back();
                }
            }
            transpositionTable.put(toFen(game.getPosition()), beta, this.depth - (this.depth - depth));
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
                try {
                    int score = quiescenceSearch(alpha, beta, false, depth - 1);
                    if (score >= beta) {
                        return beta;
                    }
                    if (score > alpha) {
                        alpha = score;
                    }
                } finally {

                    game.back();
                }
            }
        } else {
            for (Move move : captures) {
                game.play(move);
                try {

                    int score = quiescenceSearch(alpha, beta, true, depth - 1);
                    if (score <= alpha) {
                        return alpha;
                    }
                    if (score < beta) {
                        beta = score;
                    }
                } finally {
                    game.back();
                }
            }
        }
        return isMaximizer ? alpha : beta;
    }
}
