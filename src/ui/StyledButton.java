package ui;

import javax.swing.*;
import java.awt.*;

// A button painted with a vertical gradient and a rounded border, as in the Figma design
public class StyledButton extends JButton {

    private static final int CORNER_RADIUS = 6;
    private static final int HEIGHT = 26;
    private static final int SIDE_PADDING = 14;

    public static final Color[] GREEN = {new Color(0x5aa65e), new Color(0x3c8d40), new Color(0x2f6b32), Color.WHITE};
    public static final Color[] GREY = {new Color(0xfbfbfb), new Color(0xdcdcdc), new Color(0x8e8e8e), new Color(0x222222)};
    public static final Color[] RED = {new Color(0xe46b6b), new Color(0xc24141), new Color(0x9b2c2c), Color.WHITE};

    private final Color top;
    private final Color bottom;
    private final Color border;

    // style = {gradient top, gradient bottom, border, text}
    public StyledButton(String text, Color[] style) {
        super(text);
        top = style[0];
        bottom = style[1];
        border = style[2];
        setForeground(style[3]);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(0, SIDE_PADDING, 0, SIDE_PADDING));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width, HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color from = getModel().isPressed() ? bottom : top;
        Color to = getModel().isPressed() ? top : bottom;
        g2.setPaint(new GradientPaint(0, 0, from, 0, getHeight(), to));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CORNER_RADIUS, CORNER_RADIUS);
        g2.setColor(border);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CORNER_RADIUS, CORNER_RADIUS);
        g2.dispose();
        super.paintComponent(g);
    }
}
