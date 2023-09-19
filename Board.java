import java.awt.Color;
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
     * Enum representing directions on the chessboard. Used to generate moves depending on piece
     * color.
     */
    enum dir{
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

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
            legalMoves = rookLogic(originRow, originColumn, boardstate);
        } else if (piece.equals("r")) {
            legalMoves = rookLogic(originRow, originColumn, boardstate);
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

        return true;
        // uncomment
        // return false;
    }

    private HashSet<String> pawnLogic(boolean white) {

        return new HashSet<>();
    }

    /**
     * Returns the legalMoves for a rook given the current boardstate.
     */
    private HashSet<String> rookLogic(int originRow, int originColumn, Piece[][] boardstate) {
        HashSet<String> legalMoves = new HashSet<>();
        int up = scan(dir.UP, originRow, originColumn, boardstate);
        int down = scan(dir.DOWN, originRow, originColumn, boardstate);
        int right = scan(dir.RIGHT, originRow, originColumn, boardstate);
        int left = scan(dir.LEFT, originRow, originColumn, boardstate);
        // Forwards and backwards moves
        for (int i = originRow; i < up; i++) {
            legalMoves.add(positionOfCoord(i, originColumn));
        }
        for (int i = originRow; i > down; i--) {
            legalMoves.add(positionOfCoord(i, originColumn));
        }
        // Right and left moves
        for (int i = originRow; i < right; i++) {
            legalMoves.add(positionOfCoord(originRow, i));
        }
        for (int i = originRow; i > left; i--) {
            legalMoves.add(positionOfCoord(originRow, i));
        }
        return legalMoves;
    }
    private HashSet<String> bishopLogic(boolean white) {
        return new HashSet<>();
    }
    private HashSet<String> knightLogic(boolean white) {
        return new HashSet<>();
    }
    private HashSet<String> queenLogic(boolean white) {
        return new HashSet<>();
    }
    private HashSet<String> kingLogic(boolean white) {
        return new HashSet<>();
    }

    /**
     * Returns the column or row number of the first square that the piece CANNOT move to. A piece
     * can not move past a piece that is blocking its path.
     */
    private int scan(dir direction, int row, int column, Piece[][] boardstate) {
        int current = 0;
        int color = boardstate[row][column].getColor();
        // TODO prevent index out of bounds errors and treat pieces of different colors differently
        // TODO i.e. capturing is allowed only of opposite color piece... capture should be handled
        // TODO somewher else
        System.out.println(positionOfCoord(row,column));
        switch (direction) {
            case UP:
                current = row+1;
                while (current <= 7) {
                    if (boardstate[current][column].getType().equals("Empty")) {
                        current++;
                    } else {
                        break;
                    }
                }
                break;
            case DOWN:
                current = row-1;
                while (current >= 0) {
                    if (boardstate[current][column].getType().equals("Empty")) {
                        current--;
                    } else {
                        break;
                    }
                }
                break;
            case RIGHT:
                current = column+1;
                while (current <= 7) {
                    if (boardstate[row][current].getType().equals("Empty")) {
                        current++;
                    } else {
                        break;
                    }
                }
                break;
            case LEFT:
                current = column-1;
                while (current >= 0) {
                    if (boardstate[row][current].getType().equals("Empty")) {
                        current--;
                    } else {
                        break;
                    }
                }
                break;
        }
        System.out.println(current);
        return current;
    }

    private String positionOfCoord(int row, int column) {
        return row + Integer.toString(column);
    }

    private int[] coordOfPosition(String position) {
        return new int[]{Integer.parseInt(position.substring(0,1)),
                Integer.parseInt(position.substring(1,2))};
    }
}
