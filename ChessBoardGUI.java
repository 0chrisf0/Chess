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

        tools.add(new JButton("Start")); // TODO - add functionality


        JButton newgame = new JButton("New");
        tools.add(newgame); // TODO - add functionality
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
                chessBoardSquares[jj][ii] = b;
                final int row = jj;
                final int column = ii;
                //b.addActionListener(e -> activeButton(row + Integer.toString(column)));
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
        // fill the black non-pawn piece row
        for (int ii = 0; ii < 8; ii++) {
            for (int jj = 0; jj < 8; jj++) {
                switch (jj) {
                    case 0:
                        chessBoard.add(new JLabel("" + (ii + 1),
                                SwingConstants.CENTER));
                    default:
                        chessBoard.add(chessBoardSquares[jj][ii]);
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
        board = new Board(userInput);
        HashMap<String, Piece> pieces = board.getBoardstate();
        for (String key : pieces.keySet()) {
            int column = Integer.parseInt(key.substring(0,1));
            int row = Integer.parseInt(key.substring(1,2));
            chessBoardSquares[column][row].reinitialize(pieces.get(key));
        }
    }

}
