import ch.astorm.jchess.core.Board;
import ch.astorm.jchess.core.Color;
import ch.astorm.jchess.core.Position;
import ch.astorm.jchess.core.rules.RuleManager;

import java.io.Serializable;

public class SerializablePosition implements Serializable {
    private static final long serialVersionUID = 1L;

    // Add fields for the necessary information from the Position object
    // For example, if Position has a field for the row and column:
    private Board board;
    private Color color;
    private RuleManager ruleManager;

    // Add a constructor that takes a Position object and extracts the necessary information
    public SerializablePosition(Position position, RuleManager ruleManager) {
        this.board = position.getBoard();
        this.ruleManager = ruleManager;
        this.color = position.getColorOnMove();
    }


    // Add a method to convert back to a Position object
    public Position toPosition() {
        return new Position(board, ruleManager, color);
    }
}