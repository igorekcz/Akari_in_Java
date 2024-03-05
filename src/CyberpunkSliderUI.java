import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

class CyberpunkSliderUI extends BasicSliderUI {
    private Font font;


    public CyberpunkSliderUI(JSlider slider) {
        super(slider);
        try {
            File fontFile = new File("AkariData/fonts/Play-Bold.ttf");
            font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(12f);
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
        g.setColor(Color.YELLOW);

        int min = slider.getMinimum();
        int max = slider.getMaximum();
        int majorTickSpacing = slider.getMajorTickSpacing();

        for (int i = min; i <= max; i += majorTickSpacing) {
            if (i % 10 == 0) {  // Show only every 10th label
                String label = Integer.toString(i);
                int labelWidth = fontMetrics.stringWidth(label);
                int labelX = xPositionForValue(i) - labelWidth / 2;
                int labelY = trackRect.y + trackRect.height + fontMetrics.getAscent() + 5;
                g.setFont(font);
                g.drawString(label, labelX, labelY);
            }
        }

    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(trackRect.x, trackRect.y + trackRect.height / 3, trackRect.width, trackRect.height / 3);

        g2d.setColor(Color.CYAN);
        g2d.fillRect(trackRect.x, trackRect.y + trackRect.height / 3, thumbRect.x - trackRect.x, trackRect.height / 3);
    }

    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(255, 229, 0));
        g2d.fillRect(thumbRect.x, thumbRect.y + thumbRect.height / 3, thumbRect.width, thumbRect.height / 3);

    }

}
