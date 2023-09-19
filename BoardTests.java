import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BoardTests {

    String[] fields = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1".split(" ");
    Board board = new Board(fields);
    String[] ranks = fields[0].split("/");
    Piece[][] chessBoardSquares = new Piece[8][8];

    public void moveGenerator(int originRow, int originCol,
            int destRow, int destCol) {
        // First Field: pieces and their positions
        for (int row = 0; row < 8; row++) {
            int column = 0;
            for (int j = 0; j < ranks[row].length(); j++) { //j = current index along rank entry
                try {
                    int empties = Integer.parseInt(ranks[row].substring(j, j + 1));
                    for (int k = 0; k < empties; k++) {
                        Piece piece = new Piece("Empty", false);
                        // Why is this column then rank?
                        chessBoardSquares[row][column] = piece;
                        column++;
                    }
                } catch (NumberFormatException e) {
                    Piece piece = new Piece(ranks[row].substring(j, j + 1), false);
                    chessBoardSquares[row][column] = piece;
                    column++;
                }
            }
        }

    }

    @Test
    public void moveCheck() {
        moveGenerator(0, 0, 1, 0);
    }
}