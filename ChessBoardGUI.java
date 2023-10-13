import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.border.*;

/**
 * GUI inspired by code posted at:
 * https://stackoverflow.com/questions/21077322/create-a-chess-board-with-jpanel
 */
public class ChessBoardGUI {

    /**
     * chessBoardSquares is a 2D array representing all the squares of the chessboard.
     */
    private Piece[][] chessBoardSquares = new Piece[8][8];
    /**
     * brownColor is the RGB value corresponding to the color of the brown chessboard squares.
     */
    private final int[] lightBrownColor = {210, 180, 140};

    /**
     * offBrownColor is the RGB value corresponding to the color fo the offBrown chessboard squares.
     */
    private final int[] darkBrownColor = {128, 70, 27};
    /**
     * JPanel that the holds all the chess gui.
     */
    private final JPanel gui = new JPanel(new BorderLayout(3, 3));
    /**
     * JPanel that holds all the chessboard gui.
     */
    private JPanel chessBoard;
    /**
     * Title of game window.
     */
    private final JLabel message = new JLabel(
            "Chess");
    /**
     * Used to label the rows of the chessboard
     */
    private static final String COLS = "ABCDEFGH";

    /**
     * The current gamestate, a value from enum gamestate.
     */
    gamestate currentGamestate = gamestate.INACTIVE;
    enum gamestate {
        INACTIVE,
        WHITE,
        WHITE_SELECT,
        BLACK,
        BLACK_SELECT,
        CHECKMATE
    }


    /**
     * Returns the current GUI.
     */
    public final JComponent getGui() {
        return gui;
    }
    /**
     * Represents the Board corresponding to the current GUI.
     */
    private Board board;

    /**
     * Represents the position of the last click on the chessBoard. Index 0 is the row and index 1
     * is the column.
     */
    private int[] lastclick = new int[2];

    /**
     * Reprsents the current set of legalMoves for the selected piece.
     */
    private HashSet<String> currentLegalMoves = new HashSet<>();


    /**
     * Constructor for ChessBoardGUI. Makes use of initializeGui().
     */
    ChessBoardGUI() {
        initializeGui();
    }

    private void initializeGui() {
        // set up the main GUI
        gui.setBorder(new EmptyBorder(5, 5, 5, 5));
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
        gui.add(tools, BorderLayout.PAGE_START);

        JButton start = new JButton("Start");
        tools.add(start);
        start.addActionListener(e -> startGame());



        JButton newgame = new JButton("New");
        tools.add(newgame);
        newgame.addActionListener(e -> setupBoard());

        tools.add(new JButton("Save")); // TODO - add functionality!
        tools.add(new JButton("Restore")); // TODO - add functionality!
        tools.addSeparator();
        tools.add(new JButton("Resign")); // TODO - add functionality!
        tools.addSeparator();
        tools.add(message);

        gui.add(new JLabel(""), BorderLayout.LINE_START);

        chessBoard = new JPanel(new GridLayout(0, 10));
        chessBoard.setBorder(new LineBorder(Color.BLACK));
        gui.add(chessBoard);

        // create the chess board squares
        Insets buttonMargin = new Insets(0,0,0,0);
        for (int ii = 0; ii < chessBoardSquares.length; ii++) {
            for (int jj = 0; jj < chessBoardSquares[ii].length; jj++) {
                Piece b = new Piece("Empty",false);
                b.setMargin(buttonMargin);
                b.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

                // our chess pieces are 60x60 px in size, so we'll
                // 'fill this in' using a transparent icon..
                ImageIcon icon = new ImageIcon(
                        new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB));
                b.setIcon(icon);
                if ((jj % 2 == 1 && ii % 2 == 1)
                        //) {
                        || (jj % 2 == 0 && ii % 2 == 0)) {
                    b.setBackground(new Color(lightBrownColor[0],lightBrownColor[1],lightBrownColor[2]));
                } else {
                    b.setBackground(new Color(darkBrownColor[0], darkBrownColor[1], darkBrownColor[2]));
                }
                chessBoardSquares[ii][jj] = b;
                final int row = ii;
                final int column = jj;
                b.addActionListener(e -> buttonPress(row, column));
            }
        }
        //fill the chess board
        JLabel label1 = new JLabel("");
        label1.setOpaque(true);
        label1.setBackground(Color.gray);
        chessBoard.add(label1);
        // fill the top row
        for (int ii = 0; ii < 8; ii++) {
            JLabel label = new JLabel(COLS.substring(ii, ii + 1),
                    SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(Color.gray);
            chessBoard.add(label);
        }
        // top right corner
        label1 = new JLabel("");
        label1.setOpaque(true);
        label1.setBackground(Color.gray);
        chessBoard.add(label1);
        // fill the rest of the chessBoard
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 9; column++) {
                switch (column) {
                    case 8:
                        JLabel label2 = new JLabel("");
                        label2.setOpaque(true);
                        label2.setBackground(Color.gray);
                        chessBoard.add(label2);
                        break;
                    case 0:
                        JLabel label = new JLabel("" + (8 - row),
                                SwingConstants.CENTER);
                        label.setOpaque(true);
                        label.setBackground(Color.gray);
                        chessBoard.add(label);
                    default:
                        chessBoard.add(chessBoardSquares[row][column]);
                }
            }
        }
        // fill the bottom row of the chessBoard
        for (int ii = 0; ii < 10; ii++) {
            JLabel label = new JLabel("");
            label.setOpaque(true);
            label.setBackground(Color.gray);
            chessBoard.add(label);
        }
    }

    /**
     * Setup board with pieces given a starting position (FEN).
     * I.e: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
     */
    public void setupBoard() {
        String userInput = JOptionPane.showInputDialog(
                null, "Input a valid FEN", "Input", JOptionPane.QUESTION_MESSAGE);
        if (userInput == null) {
            return;
        }
        // Initialize the board with the given FEN
        String[] fields = userInput.split(" ");
        board = new Board(fields);
        String[] ranks = fields[0].split("/");
        // First Field: pieces and their positions "3R"
        for (int row = 0; row < 8; row++) {
            int column = 0;
            for (int j = 0; j < ranks[row].length(); j++ ) { //j = current index along rank entry
                try {
                    int empties = Integer.parseInt(ranks[row].substring(j,j+1));
                    for (int k = 0; k < empties; k++) {
                        Piece piece = new Piece("Empty", false);
                        chessBoardSquares[row][column].reinitialize(piece);
                        column++;
                    }
                } catch (NumberFormatException e) {
                    Piece piece = new Piece(ranks[row].substring(j,j+1), false);
                    chessBoardSquares[row][column].reinitialize(piece);
                    column++;
                }
            } // I don't know if this is needed for valid FENs, but it is convenient
            if (column != 7) {
                while (column <= 7) {
                    Piece piece = new Piece("Empty", false);
                    chessBoardSquares[row][column].reinitialize(piece);
                    column++;
                }
            }
        }
    }

    /**
     * Starts the game, initializing the gamestate to the correct turn
     */
    public void startGame() {
        if (board == null) {
            return;
        }
        // Check if any checks or pins exist
        board.updateXray(chessBoardSquares);

        if (board.getTurn().equals("w")) {
            currentGamestate = gamestate.WHITE;
        } else {
            currentGamestate = gamestate.BLACK;
        }

    }

    /**
     * Handles the movement of the pieces on the GUI. Ensures that moves are legal.
     */
    public void buttonPress(int row, int column) {
        System.out.println("On Press: " + currentGamestate);
        switch (currentGamestate) {
            case WHITE,BLACK:
                if (currentGamestate == gamestate.WHITE && chessBoardSquares[row][column].getColor() == -1) {
                    currentGamestate = gamestate.WHITE_SELECT;
                } else if (currentGamestate == gamestate.BLACK && chessBoardSquares[row][column].getColor() == 1) {
                    currentGamestate = gamestate.BLACK_SELECT;
                }
                currentLegalMoves = board.legalMoves(row, column, chessBoardSquares);
                // Color current piece and legalMoves
                chessBoardSquares[row][column].setBackground(Color.green);
                for (String position : currentLegalMoves) {
                    int[] coords = board.coordOfPosition(position);
                    chessBoardSquares[coords[0]][coords[1]].setBackground(Color.green);
                }
                break;
            case WHITE_SELECT, BLACK_SELECT:
                // Allow user to deselect the piece they chose
                if (row == lastclick[0] && column == lastclick[1]) {
                    if (currentGamestate == gamestate.WHITE_SELECT) {
                        currentGamestate = gamestate.WHITE;
                    } else {
                        currentGamestate = gamestate.BLACK;
                    }
                    uncolor();
                    break;
                }
                // These are the things that need to happen in this case statement (and BLACK_SELECT):
                // 1. Check if the move is legal
                // 2. IF the move is legal, make the move and update data accordingly (DETECT CHECKS)
                // otherwise set gamestate back to WHITE or BLACK.
                // 3. Check for promotion, then check for checks on the opposing King
                // 4. If there is a check, should automatically check for checkmate as well.
                if (currentLegalMoves.contains(board.positionOfCoord(row, column))) {

                    // MAKE MOVE (make a helper function)

                    // Uncolor Selection and Check for Checks and update XRAY status
                    boardUpdate();
                    // TODO make one function that loops over the entire board once and does this
                    uncolor();
                    if (currentGamestate == gamestate.CHECKMATE) {
                        // END GAME
                    }
                    else if (currentGamestate == gamestate.WHITE_SELECT) {
                        currentGamestate = gamestate.BLACK;
                    } else {
                        currentGamestate = gamestate.WHITE;

                    }
                } else { // Illegal move selected, deselect piece
                    if (currentGamestate == gamestate.WHITE_SELECT) {
                        currentGamestate = gamestate.WHITE;
                    } else {
                        currentGamestate = gamestate.BLACK;
                    }
                    // UNCOLOR SELECTION
                    uncolor();
                }
                break;
            default:
                return;
        }
        lastclick[0] = row;
        lastclick[1] = column;
        System.out.println("On Exit: " + currentGamestate);
    }

    /**
     * Sets all the squares on the chessboard Piece's backgrounds to what they were originally.
     */
    public void uncolor() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessBoardSquares[i][j].originalBackground();
            }
        }
    }

    /**
     * Does the various checks that need to be made after each move is made, such as:
     * -Update each piece's Xray status
     * -Detect check and checkmate
     */
    public void boardUpdate() {
        // Refresh all pieces' pin status
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessBoardSquares[i][j].resetPin();
            }
        }
        // Detect check
            if (currentGamestate == gamestate.WHITE_SELECT) {
                if (board.detectChecks(-1, chessBoardSquares) > 0) {
                    if (board.detectCheckmate(-1,chessBoardSquares)) {
                        currentGamestate = gamestate.CHECKMATE;
                    }
                }
            } else {
                if (board.detectChecks(1, chessBoardSquares) > 0) {
                    if (board.detectCheckmate(1,chessBoardSquares)) {
                        currentGamestate = gamestate.CHECKMATE;
                    }
                }
            }
        }
}
