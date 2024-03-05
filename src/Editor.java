import javax.swing.*;
import java.io.File;
import java.io.FileWriter;

/**
 * Class responsible for creating an Editor GUI, with save and load to file functionality
 */
public class Editor {
    public static int selectedTile;
    public final Board creatorSpace;
    public static boolean isSaved = true;

    public Editor(int gridSize, boolean blank){
        int I = 0;
        for (int i = 0; i <= 10 ; i++){
            if (i != 5 && i != 7 && i != 8 && i != 9 && i != 10){
            JButton temp_button = new JButton();
            temp_button.setBounds(250 + 86 * I, 650, 86, 86);
            temp_button.setIcon(new ImageIcon("AkariData/imgs/86_" + i + "_" + Akari.theme + ".png"));
            temp_button.setContentAreaFilled(false);
            I = I + 1;
            int T = i;
            temp_button.addActionListener(e -> setSelectedTile(T));
            Akari.mainPanel.add(temp_button);
        }}
        creatorSpace = new Board(gridSize, true);
        if (blank) {
            creatorSpace.createBlank();
            creatorSpace.showBoard();
        }
        JButton testCreationButton = new JButton();
        testCreationButton.setBounds(618, 100, 180, 100);
        testCreationButton.setIcon(new ImageIcon("AkariData/imgs/Test_"+Akari.theme+".png"));
        testCreationButton.setContentAreaFilled(false);
        testCreationButton.addActionListener(e -> {
            if (creatorSpace.creatorMode) {
                creatorSpace.creatorMode = false;
                testCreationButton.setIcon(new ImageIcon("AkariData/imgs/Stop_"+Akari.theme+".png"));
            }
            else {
                creatorSpace.creatorMode = true;
                creatorSpace.clearBulbs();
                testCreationButton.setIcon(new ImageIcon("AkariData/imgs/Test_"+Akari.theme+".png"));
            }
        });
        Akari.mainPanel.add(testCreationButton);

        JButton saveCreationButton = new JButton();
        saveCreationButton.setBounds(618, 250, 180, 100);
        saveCreationButton.setIcon(new ImageIcon("AkariData/imgs/Save_"+ Akari.theme+".png"));
        saveCreationButton.setContentAreaFilled(false);
        saveCreationButton.addActionListener(e -> {
            JFileChooser creationSaveDirChooser = new JFileChooser();
            creationSaveDirChooser.setCurrentDirectory(new File("AkariData/userSaves"));
            creationSaveDirChooser.setDialogTitle("Save your creation");
            int chooserResult = creationSaveDirChooser.showSaveDialog(Akari.mainPanel);
            if (chooserResult == JFileChooser.APPROVE_OPTION){
                try {
                    File saveFile = creationSaveDirChooser.getSelectedFile();
                    FileWriter saveData = new FileWriter(saveFile);
                    saveData.write(gridSize + "\n");
                    creatorSpace.clearBulbs();
                    for (int y = 0; y < creatorSpace.boardSize; y++){
                        for (int x = 0; x < creatorSpace.boardSize; x++){
                            saveData.write(creatorSpace.board[x][y].state + " ");
                        }
                        saveData.write("\n");
                    }
                    saveData.close();
                    isSaved = true;
                } catch (Exception err) {
                    JOptionPane errorPane = new JOptionPane("An error occurred while trying to save the creation", JOptionPane.ERROR_MESSAGE);
                    JDialog errorDialog = errorPane.createDialog("Error!");
                    errorDialog.setAlwaysOnTop(true);
                    errorDialog.setVisible(true);
                    new AkariError(err);
                }
            }
        });
        Akari.mainPanel.add(saveCreationButton);
    }

    public void setSelectedTile(int selectedTile) {
        Editor.selectedTile = selectedTile;
    }
}
