import java.util.HashMap;
import java.util.HashSet;

/**
 * The Board class contains all the chess game logic. This is where legal moves are calculated given
 * the current state of the chessboard.
 */
public class Board {
    /**
     * Current boardstate, contains all pieces present.
     */
    private Piece[][] boardstate = new Piece[8][8];

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
     * Return boardstate.
     */
    public Piece[][] getBoardstate() {
        return boardstate;
    }

    /**
     * Update the boardstate to be in sync with the chessBoardSquares
     */
    public void updateBoardState(Piece[][] chessBoardSquares) {
        boardstate = chessBoardSquares;

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

}
