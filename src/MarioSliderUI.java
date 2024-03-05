import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

class MarioSliderUI extends BasicSliderUI {
    private Font font;


    public MarioSliderUI(JSlider slider) {
        super(slider);
        try {
            File fontFile = new File("Pyton graphics/chinese.sthupo.ttf");
            font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(16f);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            font = new Font("Arial", Font.BOLD, 12);
        }
    }

    @Override
    public void paintLabels(Graphics g) {
        super.paintLabels(g);

        FontMetrics fontMetrics = g.getFontMetrics(font);

        // Set color for labels
        g.setColor(Color.white);

        int min = slider.getMinimum();
        int max = slider.getMaximum();
        int majorTickSpacing = slider.getMajorTickSpacing();

        for (int i = min; i <= max; i += majorTickSpacing) {
            if (i % 10 == 0) { // Show only every 10th label
                String label = Integer.toString(i);
                int labelWidth = fontMetrics.stringWidth(label);
                int labelX = xPositionForValue(i) - labelWidth / 2;
                int labelY = trackRect.y + trackRect.height + fontMetrics.getAscent() + 10;
                g.setFont(font);
                g.drawString(label, labelX, labelY);
            }
        }

    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(29, 105, 138));
        g2d.fillRect(trackRect.x, trackRect.y + trackRect.height / 4, trackRect.width, trackRect.height / 2);

        g2d.setColor(new Color(29, 48, 138));
        g2d.fillRect(trackRect.x, trackRect.y + trackRect.height / 4, thumbRect.x - trackRect.x, trackRect.height / 2);
    }

    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.white);
        g2d.fillRect(thumbRect.x, thumbRect.y + thumbRect.height / 4, thumbRect.width, thumbRect.height / 2);
    }
}

