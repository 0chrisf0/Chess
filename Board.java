import java.awt.Color;
import java.beans.IndexedPropertyDescriptor;
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
     */
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
    public enum dir{
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
     * The location of the last piece detected to be currently attacking a king.
     */
    private String attackerPos = "";

    private dir attackerDir;

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
        // TODO delete this after implementing en passant
        for(String e : passant) {
            System.out.println(e);
        }
        HashSet<String> legalMoves = new HashSet<>();
        Piece currentPiece = boardstate[originRow][originColumn];
        String piece = currentPiece.getType();
        int color = currentPiece.getColor();
        if (piece.equalsIgnoreCase("P")) {
            legalMoves = pawnLogic(originRow, originColumn, boardstate);
        }
        if (piece.equalsIgnoreCase("R")) {
            legalMoves = rookLogic(originRow, originColumn, boardstate);
        }
        if (piece.equalsIgnoreCase("Q")) {
            legalMoves = queenLogic(originRow, originColumn, boardstate);
        }
        if (piece.equalsIgnoreCase("K")) {
            legalMoves = kingLogic(originRow, originColumn, boardstate);
        }
        if (piece.equalsIgnoreCase("B")) {
            legalMoves = bishopLogic(originRow, originColumn, boardstate);
        }
        if (piece.equalsIgnoreCase("N")) {
            legalMoves = knightLogic(originRow, originColumn, boardstate);
        }

       if (detectChecks(color, boardstate) > 0) {
           removeIllegalMoves(currentPiece, color, legalMoves, boardstate);
       }
        return legalMoves;
    }

    /**
     * Called when the king is in check to remove the moves that do not remove the king from check.
     */
    private void removeIllegalMoves(Piece currentPiece, int color, HashSet<String> currentMoves,
            Piece[][] boardstate) {
        // Need to make a copy so we can remove things while iterating without errors.
        HashSet<String> iterationSet= new HashSet<>(currentMoves);
        for (String move : iterationSet) {
            if (simulateCheckTest(currentPiece, color, move, boardstate)) {
                currentMoves.remove(move);
            }
        }
    }

    /**
     * Simulates the given move and returns a boolean representing whether that move results in
     * the king STILL BEING IN CHECK (true).
     */
    private boolean simulateCheckTest(Piece currentPiece, int color, String currentMove, Piece[][] boardstate) {
        boolean condition = false;
        int[] currentMoveCoords = coordOfPosition(currentMove);
        Piece target = boardstate[currentMoveCoords[0]][currentMoveCoords[1]];
        // Store original info so we can revert simulation
        String originalTargetType = target.getType();
        int originalTargetColor = target.getColor();
        String originalPieceType = currentPiece.getType();
        int originalPieceColor = currentPiece.getColor();
        // Simulate
        target.setType(originalPieceType);
        target.setColor(originalPieceColor);
        currentPiece.setType("Empty");
        currentPiece.setColor(0);
        if(detectChecks(color, boardstate) > 0) {
            condition = true;
        }
        // Revert simulation
        target.setType(originalTargetType);
        target.setColor(originalTargetColor);
        currentPiece.setType(originalPieceType);
        currentPiece.setColor(originalPieceColor);
        return condition;

    }

    /**
     * Returns the legalMoves for a given pawn given the current boardstate.
     * FEN TESTS:
     *
     * Pinned, no legal moves: k7/8/8/8/4b/8/6P1/7K w KQkq - 0 1
     * Not Pinned, extra white blocker: k7/8/8/3b/4P/8/6P1/7K w KQkq - 0 1
     * Not Pinned, extra black blocker: k7/8/8/3b/4p/8/6P1/7K w KQkq - 0 1
     * Pinned, can push:
     * Pinned, can capture: k7/8/8/8/8/5b/6P1/7K w KQkq - 0 1
     */
    private HashSet<String> pawnLogic(int originRow, int originColumn, Piece[][] boardstate) {
        HashSet<String> legalMoves = new HashSet<>();
        boolean captureLeft = true;
        boolean captureRight = true;
        boolean pushing = true;
        int color = boardstate[originRow][originColumn].getColor();
        int maxRange = pawnScan(originRow, originColumn, boardstate);
        Piece currentPiece = boardstate[originRow][originColumn];
        // Prevent moving out of absolute pins:
        if (currentPiece.getPinned().contains(dir.UP) ||
                currentPiece.getPinned().contains(dir.DOWN)) {
            captureLeft = false;
            captureRight = false;
        }
        if (currentPiece.getPinned().contains(dir.LEFT) ||
                currentPiece.getPinned().contains(dir.RIGHT)) {
            captureLeft = false;
            captureRight = false;
            pushing = false;
        }
        if (currentPiece.getPinned().contains(dir.UP_LEFT) ||
                currentPiece.getPinned().contains(dir.DOWN_RIGHT)) {
            captureRight = false;
            pushing = false;
        }
        if (currentPiece.getPinned().contains(dir.UP_RIGHT) ||
                currentPiece.getPinned().contains(dir.DOWN_LEFT)) {
            captureLeft = false;
            pushing = false;
        }

        if (color == -1) { // White Piece
            if(pushing) {
                for (int i = originRow; i > maxRange; i--) {
                    legalMoves.add(positionOfCoord(i, originColumn));
                }
            }
            // Capturing Rules
            if(captureLeft) {
                try {
                    if (boardstate[originRow - 1][originColumn - 1].getColor() == 1 ||
                            passant.contains(positionOfCoord(originRow - 1, originColumn - 1))) {
                        legalMoves.add(positionOfCoord(originRow - 1, originColumn - 1));
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
            if (captureRight) {
                try {
                    if (boardstate[originRow - 1][originColumn + 1].getColor() == 1 ||
                            passant.contains(positionOfCoord(originRow - 1, originColumn + 1))) {
                        legalMoves.add(positionOfCoord(originRow - 1, originColumn + 1));
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }

        } else { // Black piece
            if(pushing) {
                for (int i = originRow; i < maxRange; i++) {
                    legalMoves.add(positionOfCoord(i, originColumn));
                }
            }
            // Capturing Rules
            if(captureRight) {
                try {
                    if (boardstate[originRow + 1][originColumn - 1].getColor() == -1 ||
                            passant.contains(positionOfCoord(originRow + 1, originColumn - 1))) {
                        legalMoves.add(positionOfCoord(originRow + 1, originColumn - 1));
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
            if (captureLeft) {
                try {
                    if (boardstate[originRow + 1][originColumn + 1].getColor() == -1 ||
                            passant.contains(positionOfCoord(originRow + 1, originColumn + 1))) {
                        legalMoves.add(positionOfCoord(originRow + 1, originColumn + 1));
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
        // TODO: (delete?) System.out.println("Left: " + captureLeft + " Right: " + captureRight + " Pushing: " +  pushing);
        return legalMoves;
    }

    /**
     * Returns the legalMoves for a rook given the current boardstate.
     * Test FENs on doc.
     */
    private HashSet<String> rookLogic(int originRow, int originColumn, Piece[][] boardstate) {
        boolean lateralMoves = true;
        boolean verticalMoves = true;
        Piece currentPiece = boardstate[originRow][originColumn];
        if (currentPiece.getPinned().contains(dir.LEFT) || currentPiece.getPinned().contains(dir.RIGHT) ) {
            verticalMoves = false;
        }
        if (currentPiece.getPinned().contains(dir.DOWN) || currentPiece.getPinned().contains(dir.UP) ) {
            lateralMoves = false;
        }
        if (currentPiece.getPinned().contains(dir.UP_LEFT) ||currentPiece.getPinned().contains(dir.UP_RIGHT)
                || currentPiece.getPinned().contains(dir.DOWN_LEFT) ||currentPiece.getPinned().contains(dir.DOWN_RIGHT)) {
            verticalMoves = false;
            lateralMoves = false;
        }
                HashSet<String> legalMoves = new HashSet<>();
        int up = scanPerpNoBounds(dir.UP, originRow, originColumn, boardstate);
        int down = scanPerpNoBounds(dir.DOWN, originRow, originColumn, boardstate);
        int right = scanPerpNoBounds(dir.RIGHT, originRow, originColumn, boardstate);
        int left = scanPerpNoBounds(dir.LEFT, originRow, originColumn, boardstate);
        if (verticalMoves) {
            // Up and down moves
            for (int i = originRow; i < down; i++) {
                legalMoves.add(positionOfCoord(i, originColumn));
            }
            for (int i = originRow; i > up; i--) {
                legalMoves.add(positionOfCoord(i, originColumn));
            }
        }
        if (lateralMoves) {
            // Right and left moves
            for (int i = originColumn; i < right; i++) {
                legalMoves.add(positionOfCoord(originRow, i));
            }
            for (int i = originColumn; i > left; i--) {
                legalMoves.add(positionOfCoord(originRow, i));
            }
        }
        return legalMoves;
    }
    /**
     * Returns the legalMoves for a given rook given the current boardstate.
     * Test FENs on doc.
     */
    private HashSet<String> bishopLogic(int originRow, int originColumn, Piece[][] boardstate) {
        boolean posSlopeDiag = true;
        boolean negSlopeDiag = true;
        Piece currentPiece = boardstate[originRow][originColumn];
        if (currentPiece.getPinned().contains(dir.UP) || currentPiece.getPinned().contains(dir.RIGHT)
                || currentPiece.getPinned().contains(dir.DOWN) || currentPiece.getPinned().contains(dir.LEFT)) {
            posSlopeDiag = false;
            negSlopeDiag = false;
        }
        if (currentPiece.getPinned().contains(dir.UP_LEFT) || currentPiece.getPinned().contains(dir.DOWN_RIGHT)) {
            posSlopeDiag = false;
        }
        if (currentPiece.getPinned().contains(dir.UP_RIGHT) || currentPiece.getPinned().contains(dir.DOWN_LEFT)) {
            negSlopeDiag = false;
        }
        HashSet<String> legalMoves = new HashSet<>();
        int upleft = scanDiagNoBounds(dir.UP_LEFT,originRow,originColumn,boardstate);
        int upright = scanDiagNoBounds(dir.UP_RIGHT,originRow,originColumn,boardstate);
        int downleft = scanDiagNoBounds(dir.DOWN_LEFT,originRow,originColumn,boardstate);
        int downright = scanDiagNoBounds(dir.DOWN_RIGHT,originRow,originColumn,boardstate);
        int currentCol;
        if (negSlopeDiag) {
            // Up-left diagonal moves
            currentCol = originColumn;
            for (int i = originRow; i > upleft; i--) {
                legalMoves.add(positionOfCoord(i, currentCol));
                currentCol--;
            }
            // Down-right diagonal moves
            currentCol = originColumn;
            for (int i = originRow; i < downright; i++) {
                legalMoves.add(positionOfCoord(i, currentCol));
                currentCol++;
            }
        }
        if (posSlopeDiag) {
            // Up-right diagonal moves
            currentCol = originColumn;
            for (int i = originRow; i > upright; i--) {
                legalMoves.add(positionOfCoord(i, currentCol));
                currentCol++;
            }
            // Down-left diagonal moves
            currentCol = originColumn;
            for (int i = originRow; i < downleft; i++) {
                legalMoves.add(positionOfCoord(i, currentCol));
                currentCol--;
            }
        }
        return legalMoves;
    }

    /**
     * Returns the set of legalMoves that the given knight can make given the current boardstate.
     * For Testing: 8/8/2n/8/4N/8/8/8 w KQkq - 0 1
     *
     */
    private HashSet<String> knightLogic(int originRow, int originColumn, Piece[][] boardstate) {
        // At most 8 possible moves
        HashSet<String> legalMoves = new HashSet<>();
        if (!boardstate[originRow][originColumn].getPinned().isEmpty()) {
            return legalMoves;
        }
        int currentColor = boardstate[originRow][originColumn].getColor();
        try {
            Piece currentLoc = boardstate[originRow - 2][originColumn + 1];
            if (currentLoc.getColor() == 0 ||
                    currentLoc.getColor() == -currentColor) {
                legalMoves.add(positionOfCoord(originRow - 2, originColumn + 1));
            }
        } catch (IndexOutOfBoundsException e) {

        }
        try {
            Piece currentLoc = boardstate[originRow - 2][originColumn - 1];
            if (currentLoc.getColor() == 0 ||
                    currentLoc.getColor() == -currentColor) {
                legalMoves.add(positionOfCoord(originRow - 2, originColumn - 1));
            }
        } catch (IndexOutOfBoundsException e) {

        }
        try {
            Piece currentLoc = boardstate[originRow - 1][originColumn - 2];
            if (currentLoc.getColor() == 0 ||
                    currentLoc.getColor() == -currentColor) {
                legalMoves.add(positionOfCoord(originRow - 1, originColumn - 2));
            }
        } catch (IndexOutOfBoundsException e) {}

        try {
            Piece currentLoc = boardstate[originRow - 1][originColumn + 2];
            if (currentLoc.getColor() == 0 ||
                    currentLoc.getColor() == -currentColor) {
                legalMoves.add(positionOfCoord(originRow - 1, originColumn + 2));
            }
        } catch (IndexOutOfBoundsException e) {}
        try {
            Piece currentLoc = boardstate[originRow + 1][originColumn - 2];
            if (currentLoc.getColor() == 0 ||
                    currentLoc.getColor() == -currentColor) {
                legalMoves.add(positionOfCoord(originRow + 1, originColumn - 2));
            }
        } catch (IndexOutOfBoundsException e) {}
        try {
            Piece currentLoc = boardstate[originRow + 1][originColumn + 2];
            if (currentLoc.getColor() == 0 ||
                    currentLoc.getColor() == -currentColor) {
                legalMoves.add(positionOfCoord(originRow + 1, originColumn + 2));
            }
        } catch (IndexOutOfBoundsException e) {}
        try {
            Piece currentLoc = boardstate[originRow + 2][originColumn - 1];
            if (currentLoc.getColor() == 0 ||
                    currentLoc.getColor() == -currentColor) {
                legalMoves.add(positionOfCoord(originRow + 2, originColumn - 1));
            }
        }
        catch (IndexOutOfBoundsException e) {}
        try {
            Piece currentLoc = boardstate[originRow + 2][originColumn + 1];
            if (currentLoc.getColor() == 0 ||
                    currentLoc.getColor() == -currentColor) {
                legalMoves.add(positionOfCoord(originRow + 2, originColumn + 1));
            }
        } catch (IndexOutOfBoundsException e) {}


        return legalMoves;
    }

    /**
     * Returns the set of legalMoves that the given queen can make given the current boardstate.
     *
     */
    private HashSet<String> queenLogic(int originRow, int originColumn, Piece[][] boardstate) {
        Piece currentPiece = boardstate[originRow][originColumn];
        for(dir direction : currentPiece.getPinned()) {
            System.out.println(direction);
        }
        HashSet<String> rookLegalMoves = rookLogic(originRow, originColumn, boardstate);
        HashSet<String> bishopLegalMoves = bishopLogic(originRow,originColumn,boardstate);
        rookLegalMoves.addAll(bishopLegalMoves);
        return rookLegalMoves;
    }

    /**
     * Returns the set of legalMoves that the given king can make given the current boardstate.
     *
     */
    private HashSet<String> kingLogic(int originRow, int originColumn, Piece[][] boardstate) {
        HashSet<String> legalMoves = new HashSet<>();
        HashSet<Integer[]> spots = new HashSet<>();
        spots.add(new Integer[]{originRow+1,originColumn});
        spots.add(new Integer[]{originRow+1,originColumn+1});
        spots.add(new Integer[]{originRow+1,originColumn-1});
        spots.add(new Integer[]{originRow-1,originColumn});
        spots.add(new Integer[]{originRow-1,originColumn+1});
        spots.add(new Integer[]{originRow-1,originColumn-1});
        spots.add(new Integer[]{originRow,originColumn+1});
        spots.add(new Integer[]{originRow,originColumn-1});
        Piece currentPiece = boardstate[originRow][originColumn];
        int kingColor = currentPiece.getColor();
        for (Integer[] spot : spots) {
            Piece piece;
            try {
                piece = boardstate[spot[0]][spot[1]];
            } catch (IndexOutOfBoundsException e) {
                continue;
            }
            if (piece.getColor() == kingColor) {
                // TODO this is where I will check for castling
            } else {
                // Simulate the position if the king made the given move, then reset to original
                String originalSpotType = piece.getType();
                int originalSpotColor = piece.getColor();
                String originalKingType = currentPiece.getType();
                piece.setType(currentPiece.getType());
                piece.setColor(currentPiece.getColor());
                currentPiece.setType("Empty");
                currentPiece.setColor(0);
                if(detectChecks(kingColor, boardstate) == 0) {
                    legalMoves.add(positionOfCoord(spot[0],spot[1]));
                }
                piece.setType(originalSpotType);
                piece.setColor(originalSpotColor);
                currentPiece.setType(originalKingType);
                currentPiece.setColor(kingColor);
            }
        }

        return legalMoves;
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
    private int scanDiagNoBounds(dir direction, int row, int column, Piece[][] boardstate) {
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
     * Scans forwards two squares, for pawns. Returns one beyond the furthest spot the pawn can move.
     * Doesn't consider capturing.
     */
    private int pawnScan(int row, int column, Piece[][] boardstate) {
        int current;
        int maxRange;
        if (boardstate[row][column].getColor() == -1) { // WHITE PIECE
            if (boardstate[row][column].getHasMoved()) {
                maxRange = row - 2;
            } else {
                maxRange = row - 3;
            }
            current = row - 1;
            while (current >= 0) {
                Piece piece = boardstate[current][column];
                if (piece.getType().equals("Empty")) {
                    current--;
                } else {
                    break;
                }
                if (current == maxRange) { // Ensures pawns can only move the number of places it should
                    break;
                }
            }
        } else { //BLACK PIECE
            if (boardstate[row][column].getHasMoved()) {
                maxRange = row + 2;
            } else {
                maxRange = row + 3;
            }
            current = row + 1;
            while (current <= 7) {
                Piece piece = boardstate[current][column];
                if (piece.getType().equals("Empty")) {
                    current++;
                } else {
                    break;
                }
                if (current == maxRange) { // Ensures pawns can only move the number of places it should
                    break;
                }
            }
        }
        return current;
    }

    /**
     * Finds where the kings are on the board.
     */
    private String findKings(int color, Piece[][] boardstate) {
        for (int row = 0; row < 8; row++) { //TODO more efficient way of finding the king?
            for (int column = 0; column < 8; column++) {
                if (boardstate[row][column].getType().equals("K") && color == -1) {
                    return positionOfCoord(row, column);
                } else if (boardstate[row][column].getType().equals("k") && color == 1) {
                    return positionOfCoord(row,column);
                }
            }
        }
        return null;
    }


    /**
     * This function detects check. Additionally, it updates the pin status of any pieces that are
     * pinned. This works because check detection will happen at the end of every turn anyways. This
     * function should be called at the beginning of the game and then after each turn.
     *
     * Test FENs: (must always have two kings)
     * UP: 2k/4r/8/8/8/8/4R/4K w KQkq - 0 1
     * UP: 2k/4r/8/8/8/4R/8/4K w KQkq - 0 1
     * UP: k5K/8/8/8/8/8/8/8 b KQkq - 0 1
     * UP CHECK: 2k/4r/8/8/8/8/8/4K w KQkq - 0 1
     *
     * DOWN: 4K/4R/8/8/8/8/4r/2k w KQkq - 0 1
     *
     * RIGHT: K1R3r/8/8/8/8/8/8/k w KQkq - 0 1
     *
     * LEFT: r3R1K1/8/8/8/8/8/8/k b KQkq - 0 1
     *
     * UPLEFT: b/8/2P/8/4K/8/8/k w KQkq - 0 1
     * UPLEFT: b/8/8/8/4K/8/8/k w KQkq - 0 1
     *
     * UPRIGHT: 7b/8/5P/8/3K/8/8/k w KQkq - 0 1
     *
     * DOWNRIGHT: k/8/8/3K/8/5P/8/7b w KQkq - 0 1
     *
     * DOWNLEFT: k/8/8/4K/8/2P/8/b w KQkq - 0 1
     */
    public void updateXray(Piece[][] boardstate) {
        detectChecks(-1,boardstate);
        detectChecks(1,boardstate);
    }

    /**
     * Returns the number of checks on the king of the specified color
     */
    public int detectChecks (int color, Piece[][] boardstate) {
        // TODO make a helper for this function, the 8 directions are very repetitive
        HashSet<String> perpThreats = new HashSet<>();
        HashSet<String> diagThreats = new HashSet<>();
        if (color == -1) {
            perpThreats.add("r");
            perpThreats.add("q");
            diagThreats.add("q");
            diagThreats.add("b");
        } else {
            perpThreats.add("R");
            perpThreats.add("Q");
            diagThreats.add("Q");
            diagThreats.add("B");
        }

        int check = 0;
        String king = findKings(color, boardstate);
        // Two kings must be present for the game to remain active
        assert king != null;
        int row = coordOfPosition(king)[0];
        int column = coordOfPosition(king)[1];
        int up = scanAdjust(row,column,dir.UP,boardstate);
        Piece currentPiece = boardstate[up][column];
        if (perpThreats.contains(currentPiece.getType())) {
            attackerPos = positionOfCoord(up,column);
            attackerDir = dir.UP;
            check++;
        } else if (currentPiece.getColor() == color) { // Only allied pieces can be pinned
            int up_next = scanAdjust(up,column, dir.UP, boardstate);
            Piece thisPiece = boardstate[up_next][column];
            if (perpThreats.contains(thisPiece.getType())) {
                currentPiece.addPin(dir.UP);
            }
        }

        int down = scanAdjust(row,column,dir.DOWN,boardstate);
        currentPiece = boardstate[down][column];
        if (perpThreats.contains(currentPiece.getType())) {
            attackerPos = positionOfCoord(down,column);
            attackerDir = dir.DOWN;
            check++;
        } else if (currentPiece.getColor() == color) {
            int down_next = scanAdjust(down, column, dir.DOWN, boardstate);
            Piece thisPiece = boardstate[down_next][column];
            if (perpThreats.contains(thisPiece.getType())) {
                currentPiece.addPin(dir.DOWN);
            }
        }

        int right = scanAdjust(row,column,dir.RIGHT, boardstate);
        currentPiece = boardstate[row][right];
        if (perpThreats.contains(currentPiece.getType())) {
            attackerPos = positionOfCoord(row,right);
            attackerDir = dir.RIGHT;
            check++;
        } else if (currentPiece.getColor() == color) {
            int right_next = scanAdjust(row, right, dir.RIGHT, boardstate);
            Piece thisPiece = boardstate[row][right_next];

            if (perpThreats.contains(thisPiece.getType())) {
                currentPiece.addPin(dir.RIGHT);
            }
        }

        int left = scanAdjust(row,column,dir.LEFT, boardstate);
        currentPiece = boardstate[row][left];
        if (perpThreats.contains(currentPiece.getType())) {
            attackerPos = positionOfCoord(row,left);
            attackerDir = dir.LEFT;
            check++;
        } else if (currentPiece.getColor() == color) {
            int left_next = scanAdjust(row, left, dir.LEFT, boardstate);
            Piece thisPiece = boardstate[row][left_next];
            if (perpThreats.contains(thisPiece.getType())) {
                currentPiece.addPin(dir.LEFT);
            }
        }
        // For diagonals, finding which column value to plug in to get the piece will
        // be a difference of scanResult and king row position
        int upLeft = scanAdjust(row, column, dir.UP_LEFT, boardstate);
        int newColumn = column-(row - upLeft);
        currentPiece = boardstate[upLeft][newColumn];
        if (diagThreats.contains(currentPiece.getType())) {
            attackerPos = positionOfCoord(upLeft,newColumn);
            attackerDir = dir.UP_LEFT;
            check++;
        } else if (currentPiece.getColor() == color) {
            int upLeft_next = scanAdjust(upLeft, newColumn, dir.UP_LEFT, boardstate);
            Piece thisPiece = boardstate[upLeft_next][newColumn-(upLeft - upLeft_next)];
            if (diagThreats.contains(thisPiece.getType())) {
                currentPiece.addPin(dir.UP_LEFT);
            }
        }

        int upRight = scanAdjust(row, column, dir.UP_RIGHT, boardstate);
        newColumn = column+(row - upRight);
        currentPiece = boardstate[upRight][newColumn];
        if (diagThreats.contains(currentPiece.getType())) {
            attackerPos = positionOfCoord(upRight,newColumn);
            attackerDir = dir.UP_RIGHT;
            check++;
        } else if (currentPiece.getColor() == color) {
            int upRight_next = scanAdjust(upRight, newColumn, dir.UP_RIGHT, boardstate);
            Piece thisPiece = boardstate[upRight_next][newColumn+(upRight - upRight_next)];
            if (diagThreats.contains(thisPiece.getType())) {
                currentPiece.addPin(dir.UP_RIGHT);
            }
        }

        int downRight = scanAdjust(row, column, dir.DOWN_RIGHT, boardstate);
        newColumn = column+(downRight-row);
        currentPiece = boardstate[downRight][newColumn];
        if (diagThreats.contains(currentPiece.getType())) {
            attackerPos = positionOfCoord(downRight,newColumn);
            attackerDir = dir.DOWN_RIGHT;
            check++;
        } else if (currentPiece.getColor() == color) {
            int downRight_next = scanAdjust(downRight, newColumn, dir.DOWN_RIGHT, boardstate);
            Piece thisPiece = boardstate[downRight_next][newColumn+(downRight_next-downRight)];
            if (diagThreats.contains(thisPiece.getType())) {
                currentPiece.addPin(dir.DOWN_RIGHT);
            }
        }

        int downLeft = scanAdjust(row, column, dir.DOWN_LEFT, boardstate);
        newColumn = column-(downLeft-row);
        currentPiece = boardstate[downLeft][newColumn];
        if (diagThreats.contains(currentPiece.getType())) {
            attackerPos = positionOfCoord(downLeft,newColumn);
            attackerDir = dir.DOWN_LEFT;
            check++;
        } else if (currentPiece.getColor() == color) {
            int downLeft_next = scanAdjust(downLeft, newColumn, dir.DOWN_LEFT, boardstate);
            Piece thisPiece = boardstate[downLeft_next][newColumn-(downLeft_next-downLeft)];
            if (diagThreats.contains(thisPiece.getType())) {
                currentPiece.addPin(dir.DOWN_LEFT);
            }
        }

        check += knightChecks(color, boardstate, king);
        check += pawnChecks(color,boardstate,king);
        return check;
    }

    /**
     * Adjusts a scan result to match the actual location of the piece we're interested in.
     */
    private int scanAdjust(int row, int column, dir direction, Piece[][] boardstate) {
        // Create a dummy boardstate so that the scan call always originates from a black piece.
        // Necessary to get consistent outputs from this function.
        int originalColor = boardstate[row][column].getColor();
        boardstate[row][column].setColor(1);

        int scanResult = 0;
        int adjustmentRow = 0;
        int adjustmentCol = 0;
        int priorColor;

        switch (direction) {
            case UP:
                scanResult = scanPerpNoBounds(dir.UP, row, column, boardstate);
                adjustmentRow = 1;
                // This means we encountered the edge of the board
                try {
                    boardstate[scanResult][column].getColor();
                } catch (IndexOutOfBoundsException e) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered a capture
                priorColor = boardstate[scanResult+1][column].getColor();
                if (priorColor == -1) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered an allied (black) piece
                break;
            case DOWN:
                scanResult = scanPerpNoBounds(dir.DOWN, row, column, boardstate);
                adjustmentRow = -1;
                // This means we encountered the edge of the board
                try {
                    boardstate[scanResult][column].getColor();
                } catch (IndexOutOfBoundsException e) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered a capture
                priorColor = boardstate[scanResult-1][column].getColor();
                if (priorColor == -1) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered an allied (black) piece
                break;
            case LEFT:
                scanResult = scanPerpNoBounds(dir.LEFT, row, column, boardstate);
                adjustmentCol = 1;
                // This means we encountered the edge of the board
                try {
                    boardstate[row][scanResult].getColor();
                } catch (IndexOutOfBoundsException e) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentCol;
                }
                priorColor = boardstate[row][scanResult+1].getColor();
                // This means we encountered a capture
                if (priorColor == -1) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentCol;
                }
                // This means we encountered an allied (black) piece
                break;
            case RIGHT:
                scanResult = scanPerpNoBounds(dir.RIGHT, row, column, boardstate);
                adjustmentCol = -1;
                // This means we encountered the edge of the board
                try {
                    boardstate[row][scanResult].getColor();
                } catch (IndexOutOfBoundsException e) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentCol;
                }
                priorColor = boardstate[row][scanResult-1].getColor();
                // This means we encountered a capture
                if (priorColor == -1) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentCol;
                }
                // This means we encountered an allied (black) piece
                break;
            case UP_RIGHT:
                scanResult = scanDiagNoBounds(dir.UP_RIGHT, row, column, boardstate);
                adjustmentRow = 1;
                // This means we encountered the edge of the board
                try {
                    boardstate[scanResult][column+(row-scanResult)].getColor();
                } catch (IndexOutOfBoundsException e) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                priorColor = boardstate[scanResult+1][column+(row-scanResult)-1].getColor();
                // This means we encountered a capture
                if (priorColor == -1) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered an allied (black) piece
                break;
            case UP_LEFT:
                scanResult = scanDiagNoBounds(dir.UP_LEFT, row, column, boardstate);
                adjustmentRow = 1;
                // This means we encountered the edge of the board
                try {
                    boardstate[scanResult][column - (row - scanResult)].getColor();
                } catch (IndexOutOfBoundsException e) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                priorColor = boardstate[scanResult+1][column - (row - scanResult)+1].getColor();
                // This means we encountered a capture
                if (priorColor == -1) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered an allied (black) piece
                break;
            case DOWN_LEFT:
                scanResult = scanDiagNoBounds(dir.DOWN_LEFT, row, column, boardstate);
                adjustmentRow = -1;
               // This means we encountered the edge of the board
                try {
                    boardstate[scanResult][column-(scanResult-row)].getColor();
                } catch (IndexOutOfBoundsException e) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered a capture
                priorColor = boardstate[scanResult-1][column-(scanResult-row)+1].getColor();
                if (priorColor == -1) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered an allied (black) piece
                break;
            case DOWN_RIGHT:
                scanResult = scanDiagNoBounds(dir.DOWN_RIGHT, row, column, boardstate);
                adjustmentRow = -1;
                // This means we encountered the edge of the board.
                try {
                    boardstate[scanResult][column+(scanResult-row)].getColor();
                } catch (IndexOutOfBoundsException e) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered a capture
                priorColor = boardstate[scanResult-1][column+(scanResult-row)-1].getColor();
                if (priorColor == -1) {
                    boardstate[row][column].setColor(originalColor);
                    return scanResult + adjustmentRow;
                }
                // This means we encountered an allied (black) piece
                break;
        }

        boardstate[row][column].setColor(originalColor);
        return scanResult; // Return original scan if no change made
    }

    /**
     * Returns the current number of pawn given checks.
     */
    public int pawnChecks(int color, Piece[][] boardstate, String kingPos) {
        int checks = 0;
        String pawnThreat;
        if (color == -1) {
            pawnThreat = "p";
        } else {
            pawnThreat = "P";
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (boardstate[row][col].getType().equals(pawnThreat)) {
                    if(pawnLogic(row, col, boardstate).contains(kingPos)) {
                        checks++;
                    }
                }
            }
        }
        return checks;
    }

    /**
     * Returns the current number of knight given checks.
     */
    public int knightChecks(int color, Piece[][] boardstate, String kingPos ) {
        int checks = 0;
        String knightThreat;
        if (color == -1) {
            knightThreat = "n";
        } else {
            knightThreat = "N";
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (boardstate[row][col].getType().equals(knightThreat)) {
                    if(knightLogic(row, col, boardstate).contains(kingPos)) {
                        checks++;
                    }
                }
            }
        }
        return checks;
    }

    /**
     * Determined whether a checkmate has been reached for a given king.
     */
    public boolean detectCheckmate(int color, Piece[][] boardstate) {
        // What needs to happen for checkmate
        // 1. The king has no legal moves.
        boolean legalMoves = false;
        // 2. There is no way to capture the attacking piece.
        // 3. There is no way to block the attacking piece.
        // If all three are true, checkmate is achieved
        String king = findKings(color, boardstate);
        int[] kingCoords = coordOfPosition(king);
        // Condition 1:
        legalMoves = kingLogic(kingCoords[0],kingCoords[1],boardstate).isEmpty();
        // Conditions 2 & 3:
        // This detectChecks call is necessary to update incase kingLogic altered the fields
        if (detectChecks(color, boardstate) > 1) {
            // TODO is this actually true? (if there is a double check we cannot block or capture)
            return legalMoves && true;
        }
        if (legalMoves && canCaptureOrBlock(color, boardstate, kingCoords, king)) { //TODO delete
            System.out.println("CHECKMATE");
        }
        return legalMoves && canCaptureOrBlock(color, boardstate, kingCoords, king);

    }

    public boolean canCaptureOrBlock(int color, Piece[][] boardstate, int[] kingCoords, String kingPos) {

        int[] attackerCoords = coordOfPosition(attackerPos);
        Piece attacker = boardstate[attackerCoords[0]][attackerCoords[1]];
        // Knights and pawns cannot be blocked.
        if (attacker.getType().equalsIgnoreCase("N") || attacker.getType().equalsIgnoreCase("P")) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if(legalMoves(row, col, boardstate).contains(attackerPos)) {
                        return false;
                    }
                }
            }
        }
        // Rooks, bishops, and queens can be blocked
        int currentCol;
        int originColumn = kingCoords[1];
        int originRow = kingCoords[0];
        // Up-left diagonal moves
        if (attackerDir == dir.UP_LEFT) {
            currentCol = originColumn;
            for (int i = originRow; i > attackerCoords[0]-1; i--) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        // The king himself cannot block... This line is okay because if it can
                        // capture, that will be handled in kingLogic
                        if (positionOfCoord(row,col).equals(kingPos)) {
                            continue;
                        }
                        // Want to find allied piece that can block/capture
                        if (boardstate[row][col].getColor() == color &&
                                legalMoves(row, col, boardstate).contains(attackerPos)) {
                            return false;
                        }
                    }
                }
                currentCol--;
            }
        }
        // Down-right diagonal moves
        if(attackerDir == dir.DOWN_RIGHT) {
            currentCol = originColumn;
            for (int i = originRow; i < attackerCoords[0]+1; i++) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (positionOfCoord(row,col).equals(kingPos)) {
                            continue;
                        }
                        if (boardstate[row][col].getColor() == color &&
                                legalMoves(row, col, boardstate).contains(attackerPos)) {
                            return false;
                        }
                    }
                }
                currentCol++;
            }
        }
        // Up-right diagonal moves
        if(attackerDir == dir.UP_RIGHT) {
            currentCol = originColumn;
            for (int i = originRow; i > attackerCoords[0]-1; i--) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (positionOfCoord(row,col).equals(kingPos)) {
                            continue;
                        }
                        if (boardstate[row][col].getColor() == color &&
                                legalMoves(row, col, boardstate).contains(attackerPos)) {
                            return false;
                        }
                    }
                }
                currentCol++;
            }
        }
        // Down-left diagonal moves
        if (attackerDir == dir.DOWN_LEFT) {
            currentCol = originColumn;
            for (int i = originRow; i < attackerCoords[0]+1; i++) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (positionOfCoord(row,col).equals(kingPos)) {
                            continue;
                        }
                        if (boardstate[row][col].getColor() == color &&
                                legalMoves(row, col, boardstate).contains(attackerPos)) {
                            return false;
                        }
                    }
                }
                currentCol--;
            }
        }

        // Up and down moves
        if (attackerDir == dir.DOWN) {
            for (int i = originRow; i < attackerCoords[0] + 1; i++) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (positionOfCoord(row,col).equals(kingPos)) {
                            continue;
                        }
                        if (boardstate[row][col].getColor() == color &&
                                legalMoves(row, col, boardstate).contains(attackerPos)) {
                            return false;
                        }
                    }
                }
            }
        }
        if (attackerDir == dir.UP) {
            for (int i = originRow; i > attackerCoords[0] - 1; i--) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (positionOfCoord(row,col).equals(kingPos)) {
                            continue;
                        }
                        if (boardstate[row][col].getColor() == color &&
                                legalMoves(row, col, boardstate).contains(attackerPos)) {
                            return false;
                        }
                    }
                }
            }
        }
        // Right and left moves
        if (attackerDir == dir.RIGHT) {
            for (int i = originColumn; i < attackerCoords[1] + 1; i++) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (positionOfCoord(row,col).equals(kingPos)) {
                            continue;
                        }
                        if (boardstate[row][col].getColor() == color &&
                                legalMoves(row, col, boardstate).contains(attackerPos)) {
                            return false;
                        }
                    }
                }
            }
        }
        if (attackerDir == dir.LEFT) {
            for (int i = originColumn; i > attackerCoords[1] - 1; i--) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (positionOfCoord(row,col).equals(kingPos)) {
                            continue;
                        }
                        if (boardstate[row][col].getColor() == color &&
                                legalMoves(row, col, boardstate).contains(attackerPos)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
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
