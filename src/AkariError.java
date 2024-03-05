import javax.swing.*;

/**
 * Class for displaying an error message for whenever something goes horribly wrong.
 */
public class AkariError{
    public AkariError(Exception e) {
        JFrame akariError = new JFrame("Error");
        ImageIcon icon = new ImageIcon("AkariData/imgs/icon.png");
        akariError.setIconImage(icon.getImage());
        JLabel errorBackground = new JLabel();
        errorBackground.setIcon(new ImageIcon("AkariData/imgs/error.png"));
        akariError.add(errorBackground);
        akariError.setResizable(false);
        akariError.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        akariError.pack();
        akariError.setLocationRelativeTo(null);
        akariError.setVisible(true);
        e.printStackTrace();
    }
    public AkariError(String e){
        try{
            throw new Exception(e);
        } catch (Exception err){
            new AkariError(err);
        }
    }
}