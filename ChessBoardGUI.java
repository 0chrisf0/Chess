import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
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
    private final int[] brownColor = {145, 98, 94};
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
        BLACK_SELECT
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
    int[] lastclick = new int[2];

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

        gui.add(new JLabel("?"), BorderLayout.LINE_START);

        chessBoard = new JPanel(new GridLayout(0, 9));
        chessBoard.setBorder(new LineBorder(Color.BLACK));
        gui.add(chessBoard);

        // create the chess board squares
        Insets buttonMargin = new Insets(0,0,0,0);
        for (int ii = 0; ii < chessBoardSquares.length; ii++) {
            for (int jj = 0; jj < chessBoardSquares[ii].length; jj++) {
                Piece b = new Piece("Empty",false);
                b.setMargin(buttonMargin);

                // our chess pieces are 60x60 px in size, so we'll
                // 'fill this in' using a transparent icon..
                ImageIcon icon = new ImageIcon(
                        new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB));
                b.setIcon(icon);
                if ((jj % 2 == 1 && ii % 2 == 1)
                        //) {
                        || (jj % 2 == 0 && ii % 2 == 0)) {
                    b.setBackground(Color.WHITE);
                } else {
                    b.setBackground(new Color(brownColor[0], brownColor[1], brownColor[2]));
                }
                chessBoardSquares[ii][jj] = b;
                final int row = ii;
                final int column = jj;
                b.addActionListener(e -> buttonPress(row, column));
            }
        }

        //fill the chess board
        chessBoard.add(new JLabel(""));
        // fill the top row
        for (int ii = 0; ii < 8; ii++) {
            chessBoard.add(
                    new JLabel(COLS.substring(ii, ii + 1),
                            SwingConstants.CENTER));
        }
        // fill the rest of the chessBoard
        for (int ii = 0; ii < 8; ii++) {
            for (int jj = 0; jj < 8; jj++) {
                switch (jj) {
                    case 0:
                        chessBoard.add(new JLabel("" + (8- ii),
                                SwingConstants.CENTER));
                    default:
                        chessBoard.add(chessBoardSquares[ii][jj]);
                }
            }
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
        // First Field: pieces and their positions
        for (int row = 0; row < 8; row++) {
            int column = 0;
            for (int j = 0; j < ranks[row].length(); j++ ) { //j = current index along rank entry
                try {
                    int empties = Integer.parseInt(ranks[row].substring(j,j+1));
                    for (int k = 0; k < empties; k++) {
                        Piece piece = new Piece("Empty", false);
                        // Why is this column then rank?
                        chessBoardSquares[row][column].reinitialize(piece);
                        column++;
                    }
                } catch (NumberFormatException e) {
                    Piece piece = new Piece(ranks[row].substring(j,j+1), false);
                    chessBoardSquares[row][column].reinitialize(piece);
                    column++;
                }
            }
        }
    }

    public void startGame() {
        if (board == null) {
            return;
        }
        if (board.getTurn().equals("w")) {
            currentGamestate = gamestate.WHITE;
        } else {
            currentGamestate = gamestate.BLACK;
        }

    }

    public void buttonPress(int row, int column) {
        switch (currentGamestate) {
            case WHITE:
                if (chessBoardSquares[row][column].getColor() == 0) {
                    currentGamestate = gamestate.WHITE_SELECT;
                    chessBoardSquares[row][column].setBackground(Color.green);
                }
                break;
            case WHITE_SELECT:
                // Allow user to deselect the piece they chose
                if (row == lastclick[0] && column == lastclick[1]) {
                    currentGamestate = gamestate.WHITE;
                    chessBoardSquares[row][column].originalBackground();
                    break;
                }
                // These are the things that need to happen in this case statement (and BLACK_SELECT):
                // 1. Check if the move is legal
                // 2. IF the move is legal, make the move and update data accordingly (DETECT CHECKS)
                // otherwise set gamestate back to WHITE or BLACK.
                // 3. Check for checks on the opposing King
                // 4. If there is a check, should automatically check for checkmate as well.
                if (board.checkMove(lastclick[0], lastclick[1], row, column, chessBoardSquares)) {

                } else {
                    currentGamestate = gamestate.WHITE;
                }
                break;
            case BLACK:
                if (chessBoardSquares[row][column].getColor() == 1) {
                    currentGamestate = gamestate.BLACK_SELECT;
                }
                break;
            case BLACK_SELECT:
                break;
            default:
                return;
        }
        lastclick[0] = row;
        lastclick[1] = column;
    }
}
