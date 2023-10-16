import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GameMain {
    public static void main(String[] args) {
        // Creation of window must occur on Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        // Create frame.
        JFrame frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ChessBoardGUI cb = new ChessBoardGUI();
        frame.add(cb.getGui());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);

        // Compute ideal window size and show window.
        frame.setVisible(true);


    }

}
