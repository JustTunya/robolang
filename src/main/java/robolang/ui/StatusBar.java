package robolang.ui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** The bottom bar: current message on the left, best stars of the level on the right (SPEC §7.2). */
@SuppressWarnings("serial")
public final class StatusBar extends JPanel {
    private final JLabel message = new JLabel(" ");
    private final JLabel stars = new JLabel();

    /** Creates a status bar with no message and zero stars. */
    public StatusBar() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        add(message, BorderLayout.CENTER);
        add(stars, BorderLayout.EAST);
        setStars(0);
    }

    /**
     * Sets the message.
     *
     * @param text the text to show
     */
    public void setMessage(String text) {
        message.setText(text);
    }

    /**
     * Returns the message.
     *
     * @return the shown text
     */
    public String message() {
        return message.getText();
    }

    /**
     * Shows the best result of the level.
     *
     * @param count stars from 0 to 3
     * @throws IllegalArgumentException if outside 0 to 3
     */
    public void setStars(int count) {
        if (count < 0 || count > 3) {
            throw new IllegalArgumentException("stars must be 0..3: " + count);
        }
        stars.setText("Best: " + count + "/3 stars");
    }

    /**
     * Returns the stars text.
     *
     * @return e.g. {@code Best: 2/3 stars}
     */
    public String starsText() {
        return stars.getText();
    }
}
