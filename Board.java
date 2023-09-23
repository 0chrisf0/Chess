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
     * color. Directions are from white's POV.
     */
    enum dir{
        UP,
        DOWN,
        LEFT,
        RIGHT,
        UP_LEFT,
        UP_RIGHT,
        DOWN_LEFT,
        DOWN_RIGHT
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
     * E.g. rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
     * E.g. rnbqkbnr/pppppppp/8/8/3R/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
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

    /**
     * Checks whether a given move is legal. In doing so, generates a set of all the legal moves.
     * This will make additional features in the future easier.
     */
    public HashSet<String> legalMoves(int originRow, int originColumn, Piece[][] boardstate) {
        HashSet<String> legalMoves = new HashSet<>();
        String piece = boardstate[originRow][originColumn].getType();
        if (piece.equals("P")) {
            legalMoves = pawnLogic(true);
        } else if (piece.equals("p")) {
            legalMoves = pawnLogic(false);
        }
        if (piece.equalsIgnoreCase("R")) {
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
        if (piece.equalsIgnoreCase("B")) {
            legalMoves = bishopLogic(originRow, originColumn, boardstate);
        }
        if (piece.equals("N")) {
            legalMoves = knightLogic(true);
        } else if (piece.equals("n")) {
            legalMoves = knightLogic(false);
        }

       // TODO check for check
        return legalMoves;
    }

    private HashSet<String> pawnLogic(boolean white) {

        return new HashSet<>();
    }

    /**
     * Returns the legalMoves for a rook given the current boardstate.
     */
    private HashSet<String> rookLogic(int originRow, int originColumn, Piece[][] boardstate) {
        HashSet<String> legalMoves = new HashSet<>();
        int up = scanPerpNoBounds(dir.UP, originRow, originColumn, boardstate);
        int down = scanPerpNoBounds(dir.DOWN, originRow, originColumn, boardstate);
        int right = scanPerpNoBounds(dir.RIGHT, originRow, originColumn, boardstate);
        int left = scanPerpNoBounds(dir.LEFT, originRow, originColumn, boardstate);
        // Forwards and backwards moves
        for (int i = originRow; i < down; i++) {
            legalMoves.add(positionOfCoord(i, originColumn));
        }
        for (int i = originRow; i > up; i--) {
            legalMoves.add(positionOfCoord(i, originColumn));
        }
        // Right and left moves
        for (int i = originColumn; i < right; i++) {
            legalMoves.add(positionOfCoord(originRow, i));
        }
        for (int i = originColumn; i > left; i--) {
            legalMoves.add(positionOfCoord(originRow, i));
        }
        return legalMoves;
    }
    private HashSet<String> bishopLogic(int originRow, int originColumn, Piece[][] boardstate) {
        HashSet<String> legalMoves = new HashSet<>();
        int upleft = scanDiagpNoBounds(dir.UP_LEFT,originRow,originColumn,boardstate);
        int upright = scanDiagpNoBounds(dir.UP_RIGHT,originRow,originColumn,boardstate);
        int downleft = scanDiagpNoBounds(dir.DOWN_LEFT,originRow,originColumn,boardstate);
        int downright = scanDiagpNoBounds(dir.DOWN_RIGHT,originRow,originColumn,boardstate);
        // Up-left diagonal moves
        int currentCol = originColumn;
        for (int i = originRow; i > upleft; i--) {
            legalMoves.add(positionOfCoord(i,currentCol));
            currentCol--;
        }
        // Up-right diagonal moves
        currentCol = originColumn;
        for(int i = originRow; i > upright; i--) {
            legalMoves.add(positionOfCoord(i,currentCol));
            currentCol++;
        }
        // Down-left diagonal moves
        currentCol = originColumn;
        for(int i = originRow; i < downleft; i++) {
            legalMoves.add(positionOfCoord(i,currentCol));
            currentCol--;
        }
        // Down-right diagonal moves
        currentCol = originColumn;
        for(int i = originRow; i < downright; i++) {
            legalMoves.add(positionOfCoord(i,currentCol));
            currentCol++;
        }
        return legalMoves;
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
    private int scanPerpNoBounds(dir direction, int row, int column, Piece[][] boardstate) {
        int current = 0;
        int color = boardstate[row][column].getColor();
        switch (direction) {
            case DOWN:
                current = row+1;
                while (current <= 7) {
                    Piece piece = boardstate[current][column];
                    if (piece.getType().equals("Empty")) {
                        current++;
                    } else if (piece.getColor() == -color) {
                        current++;
                        break;
                    } else {
                        break;
                    }
                }

                break;
            case UP:
                current = row-1;
                while (current >= 0) {
                    Piece piece = boardstate[current][column];
                    if (piece.getType().equals("Empty")) {
                        current--;
                    } else if (piece.getColor() == -color) {
                        current--;
                        break;
                    } else {
                        break;
                    }
                }
                break;
            case RIGHT:
                current = column+1;
                while (current <= 7) {
                    Piece piece = boardstate[row][current];
                    if (piece.getType().equals("Empty")) {
                        current++;
                    } else if (piece.getColor() == -color) {
                        System.out.println("The color is" + -color);
                        current++;
                        break;
                    } else {
                        break;
                    }
                }
                break;
            case LEFT:
                current = column-1;
                while (current >= 0) {
                    Piece piece = boardstate[row][current];
                    if (piece.getType().equals("Empty")) {
                        current--;
                    } else if (piece.getColor() == -color) {
                        current--;
                        break;
                    } else {
                        break;
                    }
                }
                break;
        }
        return current;
    }
    /**
     * Returns the row number of the first square that the piece CANNOT move to. A piece
     * can not move past a piece that is blocking its path.
     */
    private int scanDiagpNoBounds(dir direction, int row, int column, Piece[][] boardstate) {
        int currentRow = 0;
        int currentCol = 0;
        int color = boardstate[row][column].getColor();
        switch(direction) {
            case UP_LEFT:
                currentRow = row - 1;
                currentCol = column - 1;
                while (currentRow >= 0 && currentCol >= 0) {
                    Piece piece = boardstate[currentRow][currentCol];
                    if (piece.getType().equals("Empty")) {
                        currentRow--;
                        currentCol--;
                    } else if (piece.getColor() == -color) {
                        currentRow--;
                        break;
                    } else {
                        break;
                    }
                }
                break;
            case UP_RIGHT:
                currentRow = row - 1;
                currentCol = column + 1;
                while (currentRow >= 0 && currentCol <= 7) {
                    Piece piece = boardstate[currentRow][currentCol];
                    if (piece.getType().equals("Empty")) {
                        currentRow--;
                        currentCol++;
                    } else if (piece.getColor() == -color) {
                        currentRow--;
                        break;
                    } else {
                        break;
                    }
                }
                break;
            case DOWN_LEFT:
                currentRow = row + 1;
                currentCol = column - 1;
                while (currentRow <= 7 && currentCol >= 0) {
                    Piece piece = boardstate[currentRow][currentCol];
                    if (piece.getType().equals("Empty")) {
                        currentRow++;
                        currentCol--;
                    } else if (piece.getColor() == -color) {
                        currentRow++;
                        break;
                    } else {
                        break;
                    }
                }
                break;
            case DOWN_RIGHT:
                currentRow = row + 1;
                currentCol = column + 1;
                while (currentRow <= 7 && currentCol <= 7) {
                    Piece piece = boardstate[currentRow][currentCol];
                    if (piece.getType().equals("Empty")) {
                        currentRow++;
                        currentCol++;
                    } else if (piece.getColor() == -color) {
                        currentRow++;
                        break;
                    } else {
                        break;
                    }
                }
                break;
        }
        return currentRow;
    }
    /**
     * Converts a row and column to a String position.
     */
    public String positionOfCoord(int row, int column) {
        return row + Integer.toString(column);
    }

    /**
     * Converts a String position to an array containing a row and column number.
     */
   public int[] coordOfPosition(String position) {
        return new int[]{Integer.parseInt(position.substring(0,1)),
                Integer.parseInt(position.substring(1,2))};
    }
}
