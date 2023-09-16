import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Piece extends JButton {
    /**
     * Color = 1 means black
     * Color = 0 means white
     * Color = -1 means empty
     */
    private int color;

    /**
     * Piece type. Valid options are:
     * "k","q","p","b","r","n","Empty"
     * or their uppercase versions (excluding "Empty")
     */
    private String type;

    /**
     * Correct icon file name.
     */
    private String iconFile;

    /**
     * Whether this piece has moved
     */
    private Boolean moved;

    public Piece(String type, Boolean moved) {
        super();
        HashMap<String, String> filenames = new HashMap<>();
        filenames.put("k", "king");
        filenames.put("b", "bishop");
        filenames.put("r", "rook");
        filenames.put("n", "knight");
        filenames.put("q", "queen");
        filenames.put("p", "pawn");
        this.moved = moved;
        this.type = type;
        if (type.equals("Empty")) {
            this.color = -1;
        } else if (type.equals(type.toUpperCase())) { // White Piece
            this.color = 0;
            iconFile = "icons/white" + filenames.get(type.toLowerCase()) + ".png";
        } else {
            this.color = 1; // Black Piece
            iconFile = "icons/black" + filenames.get(type) + ".png";
        }
        //TODO add action listener in the constructor
    }
    public void reinitialize(Piece piece) {
        color = piece.color;
        type = piece.type;
        iconFile = piece.iconFile;
        moved = piece.moved;
        setIcon(new ImageIcon(piece.iconFile));
    }
}
