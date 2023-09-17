import java.util.HashMap;
import java.util.HashSet;

/**
 * The Board class contains all the chess game logic. This is where legal moves are calculated given
 * the current state of the chessboard.
 */
public class Board {

    /**
     * Whose turn it is. "w" or "b"
     */
    private String turn;

    /**
     * Who can castle, stored as set of strings corresponding to FEN notation for castling.
     */
    private HashSet<String> canCastle = new HashSet<>();

    /**
     * En passantable pawn positions.
     */ //TODO storing as a set for now until implemented
    private HashSet<String> passant = new HashSet<>();

    /**
     * Halfmove clock... if this is ever 100 the game ends in a draw
     */
    private int halfmoves;

    /**
     * Fullmove clock... number of moves made by black
     */
    private int fullmoves;

    /**
     * Returns whose turn it is
     */
    public String getTurn() {
        return turn;
    }
    /**
     * Construct a board object given a starting position.
     * Precondition: startingPosition must be a legal FEN.
     *
     * I.e. rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
     *
     * Lowercase letters denote black pieces.
     */
    public Board(String[] fields) {
        // Second field: turn
        turn = fields[1];
        // Third field: castling
        if (fields[2].charAt(0) != '-') {
            for (int i = 0; i < fields[2].length();i++) {
                canCastle.add(fields[2].substring(i, i + 1));
            }
        }
        // Fourth field: en passantables
        if (fields[3].charAt(0) != '-') { //TODO does this work?
            for (int i = 0; i < fields[3].length(); i = i + 2) {
                passant.add(fields[3].substring(i, i + 2));
            }
        }
        // Fifth field: halfmoves
        halfmoves = Integer.parseInt(fields[4]);
        // Sixth field: fullmoves
        fullmoves = Integer.parseInt(fields[5]);
    }

    public boolean checkMove(int originRow, int originColumn,
            int destinationRow, int destinationColumn, Piece[][] boardstate) {
        HashSet<String> legalMoves = new HashSet<>();
        String piece = boardstate[originRow][originColumn].getType();
        if (piece.equals("P")) {
            legalMoves = pawnLogic(true);
        } else if (piece.equals("p")) {
            legalMoves = pawnLogic(false);
        }
        if (piece.equals("R")) {
            legalMoves = rookLogic(true);
        } else if (piece.equals("r")) {
            legalMoves = rookLogic(false);
        }
        if (piece.equals("Q")) {
            legalMoves = queenLogic(true);
        } else if (piece.equals("q")) {
            legalMoves = queenLogic(false);
        }
        if (piece.equals("K")) {
            legalMoves = kingLogic(true);
        } else if (piece.equals("k")) {
            legalMoves = kingLogic(false);
        }
        if (piece.equals("B")) {
            legalMoves = bishopLogic(true);
        } else if (piece.equals("b")) {
            legalMoves = bishopLogic(false);
        }
        if (piece.equals("N")) {
            legalMoves = knightLogic(true);
        } else if (piece.equals("n")) {
            legalMoves = knightLogic(false);
        }

        if (legalMoves.contains(positionOfCoord(destinationRow, destinationColumn))) {
            return true;
        }

        return true; // uncomment
        // return false;
    }

    public HashSet<String> pawnLogic(boolean white) {

    }
    public HashSet<String> rookLogic(boolean white) {

    }
    public HashSet<String> bishopLogic(boolean white) {

    }
    public HashSet<String> knightLogic(boolean white) {

    }
    public HashSet<String> queenLogic(boolean white) {

    }
    public HashSet<String> kingLogic(boolean white) {

    }

    public String positionOfCoord(int row, int column) {
        return row + Integer.toString(column);
    }
}
