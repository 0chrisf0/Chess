import java.util.HashMap;

public class Piece {
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
    private String icon;

    /**
     * Whether this piece has moved
     */
    private Boolean moved;

    /**
     * Return the icon file name.
     */
    public String getIcon() {
        return icon;
    }

    public Piece(String type, Boolean moved) {
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
            icon = "white" + filenames.get(type.toLowerCase()) + ".png";
        } else {
            this.color = 1; // Black Piece
            icon = "black" + filenames.get(type) + ".png";
        }
    }
}
