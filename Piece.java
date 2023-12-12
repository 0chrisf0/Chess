import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Piece extends JButton {
    /**
     * Color = 1 means black
     * Color = -1 means white
     * Color = 0 means empty
     */
    private int color;

    /**
     * Piece type. Valid options are:
     * "k","q","p","b","r","n","Empty"
     * or their uppercase versions (excluding "Empty")
     */
    private String type;

    /**
     * Correct icon image.
     */
    private java.awt.Image iconImage;

    /**
     * Whether this piece has moved
     */
    private Boolean moved;

    /**
     * The original background color of this square.
     */
    private Color background;


    /**
     * The direction the piece is pinned against. If the set is empty, there
     */
    private HashSet<Board.dir> pinned = new HashSet<>();

    /**
     * Adds a pin direction to the set of pin directions
     */
    public void addPin(Board.dir direction) {
        pinned.add(direction);
    }

    /**
     * Resets the Piece's pin direction set.
     */
    public void resetPin() {pinned = new HashSet<>();}

    /**
     * Returns the set of pin directions.
     */
    public HashSet<Board.dir> getPinned() {
        return pinned;
    }
    /**
     * Sets the background color to the original one
     * @return
     */
    public void originalBackground() {
        setBackground(background);
    }
        /**
     * Returns the color of this piece as an int.
     */
    public int getColor() {
        return color;
    }

    /**
     * Sets the color of the piece. Only used to create dummy instances for scanAdjust.
     */
    public void setColor(int num) {color = num;}

    /**
     * Returns whether this piece has moved.
     */
    public boolean getHasMoved() {
        return moved;
    }

    /**
     * Returns the type of this piece.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the moved status of this piece.
     */
    public void setMoved(boolean val) {
        moved = val;
    }
    /**
     * Sets the type of this piece.
     */
    public void setType(String given) {
        type = given;
    }

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
            this.color = 0;
        } else if (type.equals(type.toUpperCase())) { // White Piece
            this.color = -1;
            iconImage = (Toolkit.getDefaultToolkit().getImage(getClass().getResource
                    ("icons/white" + filenames.get(type.toLowerCase()) + ".png")));
//            iconFile = "icons/white" + filenames.get(type.toLowerCase()) + ".png";
        } else {
            this.color = 1; // Black Piece
            iconImage = (Toolkit.getDefaultToolkit().getImage(getClass().getResource
                    ("icons/black" + filenames.get(type) + ".png")));
//            iconFile = "icons/black" + filenames.get(type) + ".png";
        }
    }
    public void reinitialize(Piece piece) {
        color = piece.color;
        type = piece.type;
        iconImage = piece.iconImage;
        moved = piece.moved;
        background = getBackground();
        pinned = piece.pinned;
        if (iconImage != null) {
            setIcon(new ImageIcon(piece.iconImage));
        } else {
            ImageIcon icon = new ImageIcon(
                    new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB));
            setIcon(icon);
        }
    }

}
