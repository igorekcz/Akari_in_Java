import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.filechooser.FileNameExtensionFilter;


/*
 *  State:
 *  0-4 - Zero-Cztery Blok
 *  5 - Puste
 *  6 - Pełne
 *  7 - Oświetlone
 *  8 - Oświetlone krzyżyk
 *  9 - Krzyżyk nieoświetlony
 *  10 - Żarówka
 */


/**
 * The Akari class consists of the entire game functionality and GUI, creating a new Akari object opens a new
 * instance of the game.
 */
public class Akari {
   /** User point of view volume as a number ranging from 0-100, later gets converted to realVolume */
   public static int volume;
   /** Loaded from settings, allows the correct images to be loaded for the chosen theme */
   public static int theme;
   /** This is the game's soundtrack */
   public static Clip audioClip;
   /** The board tile size we will be using in pixels. */
   public static int tileSize;
   /** Calculated from volume becomes a value in dB from -40 to 0 later added to soundtrack base audio level */
   public static FloatControl realVolume;
   /** false only when volume == 0 */
   public static boolean soundtrackPlaying;
   /** Our main panel where we do all GUI interactions */
   public static JPanel mainPanel;
   private static boolean startedPlaying = false;

   public Akari(){
      // Below we try to open the settings.txt file and check if the values are what they're supposed to be
      // If unsuccessful we go to the AkariError class which displays the error notification.
      try {
         File settings = new File("AkariData/settings.txt");
         Scanner s_reader = new Scanner(settings);
         volume = Integer.parseInt(s_reader.next());
         if (volume < 0 || volume > 100) throw new Exception();
         theme = Integer.parseInt(s_reader.next());
         if (theme != 0 && theme != 1) throw new Exception();
         s_reader.close();
      } catch (Exception e){
         new AkariError(e);
         return;
      }

      // Main window settings
      String iconFileName = "AkariData/imgs/icon.png";
      JFrame mainWindow = new JFrame("Akari");
      ImageIcon startIcon = new ImageIcon(iconFileName);
      mainWindow.setIconImage(startIcon.getImage());
      mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      mainWindow.setSize(816,835);
      mainWindow.setLocationRelativeTo(null);
      mainWindow.setLayout(null);
      mainWindow.setResizable(false);

      // mainPanel
      mainPanel = new JPanel();
      JLabel satLabel = new JLabel();
      mainPanel.setBounds(0,0,800,800);
      mainPanel.setLayout(null);
      JLabel background = new JLabel();
      background.setBounds(0,0,800,800);
      ImageIcon backgroundIcon = new ImageIcon("AkariData/imgs/background_" + theme + ".png");
      background.setIcon(backgroundIcon);
      mainWindow.add(mainPanel);

      // Panel with levels
      JPanel levelPanel = new JPanel();

      // Labels
      JLabel titleIcon = new JLabel();

      // Buttons
      JButton playButton = new JButton();
      JButton print = new JButton();
      JButton settingsButton = new JButton();
      JButton creditsButton = new JButton();
      JButton rulesButton = new JButton();
      JButton quitButton = new JButton();
      JButton levelButton = new JButton();
      JButton lightThemeButton = new JButton();
      JButton darkThemeButton = new JButton();
      JButton loadFromFile = new JButton();
      JButton playLevelsButton = new JButton();
      JButton createOwnLevelButton = new JButton();
      JButton gridSize7Button = new JButton("7x7");
      JButton gridSize10Button = new JButton("10x10");
      JButton gridSize14Button = new JButton("14x14");
      JButton undoMoveButton = new JButton("Undo");


      JButton loadFileButton = new JButton();
      // Buttons to go back
      JButton goBackButton = new JButton();
      JButton goBackButton_StartGame = new JButton();
      JButton goBackButton_Levels = new JButton();
      JButton goBackButton_Editor = new JButton();

      // Sliders
      JSlider volumeSlider = new JSlider();
      volumeSlider.setOpaque(false);

      // Slider to change volume of music
      JLabel volumeLabel = new JLabel("Volume: " + volumeSlider.getValue());
      volumeLabel.setBounds(200, 350, 100, 100);

      volumeSlider.addChangeListener(e -> {
         volumeLabel.setText("Volume:" + volumeSlider.getValue());
         volume = volumeSlider.getValue();
         if (volume != 0) {
            if (!soundtrackPlaying) audioClip.loop(Clip.LOOP_CONTINUOUSLY);
            realVolume.setValue((-1) * (40 - (0.4f * volume)));
         } else {
            audioClip.stop();
            soundtrackPlaying = false;
         }
         saveSettings(volume, theme);
      });

      volumeSlider.setBounds(300, 350, 400, 100);

      if (theme == 0) {
         Font labelFont = new Font("Play", Font.PLAIN, 12); // for Cyberpunk theme
         volumeLabel.setFont(labelFont);
         volumeLabel.setForeground(Color.YELLOW);
         volumeSlider.setUI(new CyberpunkSliderUI(volumeSlider));
      } else if (theme == 1) {
         Font labelFont = new Font("chinese.sthupo.ttf", Font.PLAIN, 12); // for Mario theme
         volumeLabel.setFont(labelFont);
         volumeLabel.setForeground(Color.WHITE);
         volumeSlider.setUI(new MarioSliderUI(volumeSlider));
      }

      volumeSlider.setPaintLabels(true);
      volumeSlider.setPaintTicks(true);
      volumeSlider.setMinorTickSpacing(2);
      volumeSlider.setPaintTrack(true);
      volumeSlider.setMajorTickSpacing(10);

      mainPanel.setOpaque(false);



      //Back to main screen
      goBackButton.setBounds(-5,700,100,100);
      goBackButton.setContentAreaFilled(false);
      goBackButton.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
      goBackButton.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            goBackButton.setIcon(new ImageIcon("AkariData/imgs/back_clicked_"+theme+".png"));
         }
         @Override
         public void mouseReleased(MouseEvent e) {
            goBackButton.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
         }
         @Override
         public void mouseEntered(MouseEvent e) {
            goBackButton.setIcon(new ImageIcon("AkariData/imgs/back_touched_"+theme+".png"));
         }
         @Override
         public void mouseExited(MouseEvent e) {
            goBackButton.setIcon(new ImageIcon("AkariData/imgs/Back_default_"+theme+".png"));
         }
      });
      goBackButton.addActionListener(e -> {
         mainPanel.removeAll();
         mainPanel.add(titleIcon);
         mainPanel.add(playButton);
         mainPanel.add(settingsButton);
         mainPanel.add(creditsButton);
         mainPanel.add(rulesButton);
         mainPanel.add(quitButton);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });

      // Go back from levels to Start Game
      goBackButton_StartGame.setBounds(-5,700,100,100);
      goBackButton_StartGame.setContentAreaFilled(false);
      goBackButton_StartGame.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
      goBackButton_StartGame.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            goBackButton_StartGame.setIcon(new ImageIcon("AkariData/imgs/back_clicked_"+theme+".png"));
         }
         @Override
         public void mouseReleased(MouseEvent e) {
            goBackButton_StartGame.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
         }
         @Override
         public void mouseEntered(MouseEvent e) {
            goBackButton_StartGame.setIcon(new ImageIcon("AkariData/imgs/back_touched_"+theme+".png"));
         }
         @Override
         public void mouseExited(MouseEvent e) {
            goBackButton_StartGame.setIcon(new ImageIcon("AkariData/imgs/Back_default_"+theme+".png"));
         }
      });
      goBackButton_StartGame.addActionListener(e -> {
         mainPanel.removeAll();
         mainPanel.add(createOwnLevelButton);
         mainPanel.add(playLevelsButton);
         mainPanel.add(goBackButton);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });

      // Go back from level to Levels
      goBackButton_Levels.setBounds(-5,700,100,100);
      goBackButton_Levels.setContentAreaFilled(false);
      goBackButton_Levels.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
      goBackButton_Levels.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            goBackButton_Levels.setIcon(new ImageIcon("AkariData/imgs/back_clicked_"+theme+".png"));
         }
         @Override
         public void mouseReleased(MouseEvent e) {
            goBackButton_Levels.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
         }
         @Override
         public void mouseEntered(MouseEvent e) {
            goBackButton_Levels.setIcon(new ImageIcon("AkariData/imgs/back_touched_"+theme+".png"));
         }
         @Override
         public void mouseExited(MouseEvent e) {
            goBackButton_Levels.setIcon(new ImageIcon("AkariData/imgs/Back_default_"+theme+".png"));
         }
      });
      goBackButton_Levels.addActionListener(e -> {
         Board.removeWinningPanel();
         mainPanel.removeAll();
         mainPanel.add(goBackButton_StartGame);
         mainPanel.add(levelPanel);
         mainPanel.add(loadFromFile);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });

      // Go back to editor
      goBackButton_Editor.setBounds(-5,700,100,100);
      goBackButton_Editor.setContentAreaFilled(false);
      goBackButton_Editor.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
      goBackButton_Editor.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            goBackButton_Editor.setIcon(new ImageIcon("AkariData/imgs/back_clicked_"+theme+".png"));
         }
         @Override
         public void mouseReleased(MouseEvent e) {
            goBackButton_Editor.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
         }
         @Override
         public void mouseEntered(MouseEvent e) {
            goBackButton_Editor.setIcon(new ImageIcon("AkariData/imgs/back_touched_"+theme+".png"));
         }
         @Override
         public void mouseExited(MouseEvent e) {
            goBackButton_Editor.setIcon(new ImageIcon("AkariData/imgs/Back_default_"+theme+".png"));
         }
      });
      goBackButton_Editor.addActionListener(e -> {
         if (!Editor.isSaved){
            int saveOption = JOptionPane.showOptionDialog(
              mainPanel,
              "Are you sure you want to exit without saving creation?",
              "Exit confirmation",
              JOptionPane.DEFAULT_OPTION,
              JOptionPane.WARNING_MESSAGE,
              null,
              new Object[]{"Cancel", "Exit"},
              "Cancel"
            );
            if (saveOption == 0){
               return;
            }
         }
         mainPanel.removeAll();
         mainPanel.add(goBackButton_StartGame);
         mainPanel.add(gridSize7Button);
         mainPanel.add(gridSize10Button);
         mainPanel.add(gridSize14Button);
         mainPanel.add(loadFileButton);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });
      // Print button
      print.setBounds(500,640,200,100);
      print.setContentAreaFilled(false);
      print.setIcon(new ImageIcon("AkariData/imgs/Print_"+theme+".png"));
      print.addActionListener(e -> {
         JPanel contentPane = mainPanel;
         BufferedImage image = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
         Graphics graphics = image.getGraphics();
         contentPane.paint(graphics);
         graphics.dispose();
         JFileChooser creationSaveDirChooser = new JFileChooser();
         creationSaveDirChooser.setCurrentDirectory(new File("AkariData/userSaves"));
         creationSaveDirChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.png", "png"));
         creationSaveDirChooser.setDialogTitle("Save your board");
         int chooserResult = creationSaveDirChooser.showSaveDialog(mainPanel);
         if (chooserResult == JFileChooser.APPROVE_OPTION) {
            try {
               //File output = new File("screenshot.png");
               File output = creationSaveDirChooser.getSelectedFile();
               String test = output.getPath();
               if (!test.endsWith(".png")) test = test + ".png";
               ImageIO.write(image, "png", new File(test));
            } catch (Exception err) {
               JOptionPane errorPane = new JOptionPane("An error occurred while trying to save the board", JOptionPane.ERROR_MESSAGE);
               JDialog errorDialog = errorPane.createDialog("Error!");
               errorDialog.setAlwaysOnTop(true);
               errorDialog.setVisible(true);
               new AkariError(err);
            }
         }
      });

      // Label z tytułem
      titleIcon.setIcon(new ImageIcon("AkariData/imgs/Akari_logoM_" + theme + ".png"));
      titleIcon.setBounds(100,100,600,200);
      mainPanel.add(titleIcon);

      // Przycisk graj
      playButton.setIcon(new ImageIcon("AkariData/imgs/start_game_" + theme + ".png"));
      playButton.setBounds(250,400,300,100);
      playButton.setContentAreaFilled(false);
      mainPanel.add(playButton);
      playButton.addActionListener(e -> {
         mainPanel.removeAll();
         mainPanel.add(createOwnLevelButton);
         mainPanel.add(playLevelsButton);
         mainPanel.add(goBackButton);
         mainPanel.add(background);
         mainPanel.add(levelButton);
         mainPanel.revalidate();
         mainPanel.repaint();
      });
      // LoadFromFileButton
      loadFromFile.setBounds(120,600,200,100);
      loadFromFile.setContentAreaFilled(false);
      loadFromFile.setIcon(new ImageIcon("AkariData/imgs/Load_"+theme+".png"));
      tileSize = 86;
      final AtomicInteger[] bSize = {new AtomicInteger(7)};
      JButton save = new JButton("Save");
      save.setIcon(new ImageIcon("AkariData/imgs/Save_"+theme+".png"));
      final File[] lFile = new File[1];
      save.setBounds(618,250,180,100);
      final Board[] lff = new Board[1];
      loadFromFile.addActionListener(e -> {
         JFileChooser loadFile = new JFileChooser();
         loadFile.setCurrentDirectory(new File ("AkariData/userSaves"));
         loadFile.setDialogTitle("Load your creation");
         int chooserResult = loadFile.showOpenDialog(mainPanel);
         if (chooserResult == JFileChooser.APPROVE_OPTION) {
            mainPanel.removeAll();
            JButton Reset = new JButton("Reset");
            Reset.setContentAreaFilled(false);
            Reset.setIcon(new ImageIcon("AkariData/imgs/Reset_"+theme+".png"));
            Reset.setBounds(200,640,250,100);
            lFile[0] = loadFile.getSelectedFile();
            try {
               Scanner sizeScan = new Scanner(lFile[0]);
               bSize[0].set(sizeScan.nextInt());
               sizeScan.close();
               switch (bSize[0].get()){
                  case 7 -> tileSize = 86;
                  case 10 -> tileSize = 60;
                  case 14 -> tileSize = 43;
                  default -> {}
               }
               lff[0] = new Board(bSize[0].get(), false);
               lff[0].getFromFile(lFile[0].getPath());
               lff[0].showBoard();
               undoMoveButton.addActionListener(e3 -> lff[0].undoMove());
               mainPanel.add(goBackButton_Levels);
               mainPanel.add(save);
//               save.addActionListener(e2 -> lff[0].saveBoard(boardSize, lff[0],loadedFile.getPath()));
               mainPanel.add(Reset);
               Reset.addActionListener(e1 -> lff[0].clearBulbs());
               mainPanel.add(print);
               mainPanel.add(undoMoveButton);
               mainPanel.add(background);
               mainPanel.revalidate();
               mainPanel.repaint();
            } catch (Exception err){
               new AkariError(err);
            }
         }
      });

      // Save button
      save.setContentAreaFilled(false);
      save.addActionListener(e -> {
         lff[0].saveBoard(bSize[0], lff[0], lFile[0].getPath());
         JFrame saveFrame = new JFrame("You saved me :)");
         JLabel saveLabel = new JLabel();
         saveLabel.setBounds(0,0,300,200);
         saveLabel.setIcon(new ImageIcon("AkariData/imgs/icon.png"));
         saveFrame.setIconImage(startIcon.getImage());
         saveFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         saveFrame.setSize(300,200);
         saveFrame.setLocationRelativeTo(null);
         saveFrame.setLayout(null);
         saveFrame.setResizable(false);
         saveFrame.add(saveLabel);
         saveFrame.setVisible(true);
      });


      // Przycisk ustawienia
      levelButton.setBounds(650,50,10,10);
      settingsButton.setIcon(new ImageIcon("AkariData/imgs/settings_default_"+theme+".png"));
      settingsButton.setContentAreaFilled(false);
      levelButton.setContentAreaFilled(false);
      satLabel.setBounds(0,0,800,800);
      settingsButton.setBounds(40,600,100,100);
      settingsButton.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            settingsButton.setIcon(new ImageIcon("AkariData/imgs/settings_clicked_"+theme+".png"));
         }
         @Override
         public void mouseReleased(MouseEvent e) {
            settingsButton.setIcon(new ImageIcon("AkariData/imgs/settings_default_"+theme+".png"));
         }
         @Override
         public void mouseEntered(MouseEvent e) {
            settingsButton.setIcon(new ImageIcon("AkariData/imgs/settings_touched_"+theme+".png"));
         }
         @Override
         public void mouseExited(MouseEvent e) {
            settingsButton.setIcon(new ImageIcon("AkariData/imgs/settings_default_"+theme+".png"));
         }
      });
      levelButton.addActionListener(e -> {
         mainPanel.removeAll();
         mainPanel.add(satLabel);
         mainPanel.revalidate();
         mainPanel.repaint();
      });
      ImageIcon Ikona = new ImageIcon("AkariData/imgs/obraz.png");
      satLabel.setIcon(Ikona);
      settingsButton.addActionListener(e -> {
         settingsButton.setIcon(new ImageIcon("AkariData/imgs/settings_default_" + theme + ".png"));
         mainPanel.removeAll();
         mainPanel.add(goBackButton);
         mainPanel.add(volumeSlider);
         volumeSlider.setValue(volume);
         mainPanel.add(volumeLabel);
         mainPanel.add(lightThemeButton);
         mainPanel.add(darkThemeButton);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });
      mainPanel.add(settingsButton);

      // Przycisk credits
      creditsButton.setIcon(new ImageIcon("AkariData/imgs/credits2_" + theme + ".png"));
      creditsButton.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseEntered(MouseEvent e) {
            creditsButton.setIcon(new ImageIcon("AkariData/imgs/CreditsM_" + theme + ".png"));
         }

         @Override
         public void mouseExited(MouseEvent e) {
            creditsButton.setIcon(new ImageIcon("AkariData/imgs/credits2_" + theme + ".png"));
         }
      });
      creditsButton.setBounds(180,600,200,100);
      creditsButton.setContentAreaFilled(false);
      creditsButton.setContentAreaFilled(false);
      JLabel credit_label = new JLabel();
      credit_label.setBounds(0,0,800,800);
      credit_label.setIcon(new ImageIcon("AkariData/imgs/credits_image_"+theme+".png"));
      creditsButton.addActionListener(e -> {
         creditsButton.setIcon(new ImageIcon("AkariData/imgs/credits2_" + theme + ".png"));
         mainPanel.removeAll();
         mainPanel.add(goBackButton);
         mainPanel.add(credit_label);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });
      mainPanel.add(creditsButton);

      // Change theme to light
      lightThemeButton.setBounds(150, 100, 230, 230);
      lightThemeButton.setIcon(new ImageIcon("AkariData/imgs/Theme_ logo_1.png"));
      lightThemeButton.setContentAreaFilled(false);
      lightThemeButton.addActionListener(e -> {
         if (theme != 1){
         saveSettings(volume, 1);
         mainWindow.dispose();
         new Akari();
      }});

      // Change theme to dark
      darkThemeButton.setBounds(450, 100, 230, 230);
      darkThemeButton.setIcon(new ImageIcon("AkariData/imgs/Theme_ logo_0.png"));
      darkThemeButton.setContentAreaFilled(false);
      darkThemeButton.addActionListener(e ->{
         if (theme != 0){
         saveSettings(volume, 0);
         mainWindow.dispose();
         new Akari();

      }});

      gridSize7Button.setBounds(50, 250, 200, 200);
      gridSize7Button.setContentAreaFilled(false);
      gridSize7Button.setIcon(new ImageIcon("AkariData/imgs/7x7_"+theme+".png"));
      gridSize7Button.addActionListener(e -> {
         mainPanel.removeAll();
         tileSize = 86;
         new Editor(7, true);
         mainPanel.add(goBackButton_Editor);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });
      gridSize10Button.setBounds(300, 250, 200, 200);
      gridSize10Button.setContentAreaFilled(false);
      gridSize10Button.setIcon(new ImageIcon("AkariData/imgs/10x10_"+theme+".png"));
      gridSize10Button.addActionListener(e -> {
         mainPanel.removeAll();
         tileSize = 60;
         new Editor(10, true);
         mainPanel.add(goBackButton_Editor);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });
      gridSize14Button.setBounds(550, 250, 200, 200);
      gridSize14Button.setContentAreaFilled(false);
      gridSize14Button.setIcon(new ImageIcon("AkariData/imgs/14x14_"+theme+".png"));
      gridSize14Button.addActionListener(e -> {
         mainPanel.removeAll();
         tileSize = 43;
         new Editor(14, true);
         mainPanel.add(goBackButton_Editor);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });

      loadFileButton.setBounds(100, 500, 600, 150);
      loadFileButton.setIcon(new ImageIcon("AkariData/imgs/Read_from_file_"+theme+".png"));
      loadFileButton.setContentAreaFilled(false);
      tileSize = 86;
      loadFileButton.addActionListener(e -> {
         JFileChooser loadFile = new JFileChooser();
         loadFile.setCurrentDirectory(new File ("AkariData/userSaves"));
         loadFile.setDialogTitle("Load your creation");
         int chooserResult = loadFile.showOpenDialog(mainPanel);
         if (chooserResult == JFileChooser.APPROVE_OPTION) {
            mainPanel.removeAll();
            File loadedFile = loadFile.getSelectedFile();
            try {
               Scanner sizeScan = new Scanner(loadedFile);
               int boardSize = sizeScan.nextInt();
               sizeScan.close();
               switch (boardSize){
                  case 7 -> tileSize = 86;
                  case 10 -> tileSize = 60;
                  case 14 -> tileSize = 43;
                  default -> {}
               }
               Editor fromFileEditor = new Editor(boardSize, false);
               fromFileEditor.creatorSpace.getFromFile(loadedFile.getPath());
               fromFileEditor.creatorSpace.showBoard();
               mainPanel.add(goBackButton_Editor);
               mainPanel.add(background);
               mainPanel.revalidate();
               mainPanel.repaint();
            } catch (Exception err){
               new AkariError(err);
            }
         }
      });

      // Button to editor
      createOwnLevelButton.setIcon(new ImageIcon("AkariData/imgs/Open_editor_" + theme + ".png"));
      createOwnLevelButton.setBounds(100, 550, 600, 150);
      createOwnLevelButton.setContentAreaFilled(false);
      createOwnLevelButton.addActionListener(e -> {
         mainPanel.removeAll();
         mainPanel.add(goBackButton_StartGame);
         mainPanel.add(gridSize7Button);
         mainPanel.add(gridSize10Button);
         mainPanel.add(gridSize14Button);
         mainPanel.add(loadFileButton);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });

      undoMoveButton.setBounds(625, 450, 150, 100);
      undoMoveButton.setContentAreaFilled(false);
      undoMoveButton.setIcon(new ImageIcon("AkariData/imgs/undo_"+theme+".png"));


      // Level panel
      levelPanel.setBounds(120,75,600,500);
      levelPanel.setOpaque(false);
      levelPanel.setLayout(new GridLayout(3,5,10,10));
      for (int i = 1; i <= 15 ; i++){
         JButton temp_button = new JButton();
         //temp_button.setIcon(new ImageIcon("path"));
         temp_button.setContentAreaFilled(false);
         temp_button.setIcon(new ImageIcon("AkariData/imgs/level_"+i+"_"+theme+".png"));
         JButton reset = new JButton("Reset");
         reset.setIcon(new ImageIcon("AkariData/imgs/Reset_"+theme+".png"));
         reset.setContentAreaFilled(false);
         JButton goBackButton_Levelsave = new JButton();
         goBackButton_Levelsave.setContentAreaFilled(false);
         goBackButton_Levelsave.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
         goBackButton_Levelsave.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               goBackButton_Levelsave.setIcon(new ImageIcon("AkariData/imgs/back_clicked_"+theme+".png"));
            }
            @Override
            public void mouseReleased(MouseEvent e) {
               goBackButton_Levelsave.setIcon(new ImageIcon("AkariData/imgs/back_default_"+theme+".png"));
            }
            @Override
            public void mouseEntered(MouseEvent e) {
               goBackButton_Levelsave.setIcon(new ImageIcon("AkariData/imgs/back_touched_"+theme+".png"));
            }
            @Override
            public void mouseExited(MouseEvent e) {
               goBackButton_Levelsave.setIcon(new ImageIcon("AkariData/imgs/Back_default_"+theme+".png"));
            }
         });
         reset.setBounds(200,640,250,100);
         int I = i;
         AtomicInteger boardSize = new AtomicInteger(7);
         final Board[] level = new Board[1];
         tileSize = 86;
         if (I >10){
            tileSize = 43;
            boardSize.set(14);
         } else if (I>=6) {
            tileSize = 60;
            boardSize.set(10);
         }
         level[0] = new Board(boardSize.get(), false);
         level[0].getFromFile("AkariData/levels/"+I);
         if (level[0].checkIfUserWon()) temp_button.setIcon(new ImageIcon("AkariData/imgs/won_level_"+i+"_"+theme+".png"));
         temp_button.addActionListener(e -> {
            mainPanel.removeAll();

            undoMoveButton.addActionListener(e1 -> level[0].undoMove());

            tileSize = 86;
            if (I >10){
               tileSize = 43;
            } else if (I>=6) {
               tileSize = 60;
            }
            level[0].showBoard();
            reset.addActionListener(e1 -> {
               level[0].clearBulbs();
               level[0].moveHistory.removeAllElements();
            });
            mainPanel.add(goBackButton_Levelsave);
            mainPanel.add(reset);
            mainPanel.add(print);
            mainPanel.add(undoMoveButton);
            mainPanel.add(background);
            mainPanel.revalidate();
            mainPanel.repaint();
         });
         levelPanel.add(temp_button);
         goBackButton_Levelsave.setBounds(-5,700,100,100);

         goBackButton_Levelsave.addActionListener(e -> {
            Board.removeWinningPanel();
            level[0].moveHistory.removeAllElements();
            level[0].saveBoard(boardSize,level[0],I);
            if (level[0].checkIfUserWon()) temp_button.setIcon(new ImageIcon("AkariData/imgs/won_level_"+I+"_"+theme+".png"));
            else if (!level[0].checkIfUserWon()) temp_button.setIcon(new ImageIcon("AkariData/imgs/level_"+I+"_"+theme+".png"));
            mainPanel.removeAll();
            mainPanel.add(goBackButton_StartGame);
            mainPanel.add(levelPanel);
            mainPanel.add(loadFromFile);
            mainPanel.add(background);
            mainPanel.revalidate();
            mainPanel.repaint();
         });
      }
      playLevelsButton.setIcon(new ImageIcon("AkariData/imgs/Play_" + theme + ".png"));
      playLevelsButton.setBounds(100, 300, 600, 150);
      playLevelsButton.setContentAreaFilled(false);
      playLevelsButton.addActionListener(e -> {
         mainPanel.removeAll();
         mainPanel.add(goBackButton_StartGame);
         mainPanel.add(levelPanel);
         mainPanel.add(loadFromFile);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });

      // Przycisk rules
      rulesButton.setIcon(new ImageIcon("AkariData/imgs/rules_" + theme + ".png"));
      rulesButton.setBounds(420, 600, 200, 100);
      rulesButton.setContentAreaFilled(false);
      JLabel rule_label = new JLabel();
      rule_label.setBounds(0, 0, 800, 800);
      String imagePath = "AkariData/imgs/rules_image_" + theme + ".png";
      ImageIcon imageIcon = new ImageIcon(imagePath);
      Image image = imageIcon.getImage();
      Image scaledImage = image.getScaledInstance(800, 800, Image.SCALE_SMOOTH);
      imageIcon = new ImageIcon(scaledImage);
      rule_label.setIcon(imageIcon);

      rulesButton.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseEntered(MouseEvent e) {
            rulesButton.setIcon(new ImageIcon("AkariData/imgs/rulesM_" + theme + ".png"));
         }

         @Override
         public void mouseExited(MouseEvent e) {
            rulesButton.setIcon(new ImageIcon("AkariData/imgs/rules_" + theme + ".png"));
         }
      });
      rulesButton.addActionListener(e -> {
         rulesButton.setIcon(new ImageIcon("AkariData/imgs/rules_" + theme + ".png"));
         mainPanel.removeAll();
         mainPanel.add(goBackButton);
         mainPanel.add(rule_label);
         mainPanel.add(background);
         mainPanel.revalidate();
         mainPanel.repaint();
      });
      mainPanel.add(rulesButton);



      // Przycisk quit
      quitButton.setIcon(new ImageIcon("AkariData/imgs/exit_default_"+theme+".png"));
      quitButton.setContentAreaFilled(false);
      quitButton.setBounds(660,600,100,100);
      quitButton.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            quitButton.setIcon(new ImageIcon("AkariData/imgs/exit_clicked_"+theme+".png"));
         }
         @Override
         public void mouseReleased(MouseEvent e) {
            quitButton.setIcon(new ImageIcon("AkariData/imgs/exit_default_"+theme+".png"));
         }
         @Override
         public void mouseEntered(MouseEvent e) {
            quitButton.setIcon(new ImageIcon("AkariData/imgs/exit_touched_"+theme+".png"));
         }
         @Override
         public void mouseExited(MouseEvent e) {
            quitButton.setIcon(new ImageIcon("AkariData/imgs/exit_default_"+theme+".png"));
         }
      });
      quitButton.addActionListener(e -> mainWindow.dispose());
      mainPanel.add(quitButton);


      // Below we try to open our soundtrack audio which will play at a volume between -40dB and 0dB (plus the audio's level)
      if (!startedPlaying){
      try {
         File akariSoundtrack = new File("AkariData/AkariSoundtrack.wav");
         AudioInputStream audioPlayer = AudioSystem.getAudioInputStream(akariSoundtrack);
         audioClip = AudioSystem.getClip();
         audioClip.open(audioPlayer);
         realVolume = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
         if (volume != 0){
            realVolume.setValue((-1) * (40 - (0.4f * volume)));
            audioClip.loop(Clip.LOOP_CONTINUOUSLY);
         }
         startedPlaying = true;
      } catch (Exception e) {
         new AkariError(e);
         return;
      }}

      // Ważne aby to była taka kolejność plus aby to było na końcu tworzenia GUI
      mainPanel.add(background);
      mainWindow.setVisible(true);


   }
   /** Saves the provided values to the settings.txt file */
   public void saveSettings(int volume, int theme){
      try{
         FileWriter settingsWriter = new FileWriter("AkariData/settings.txt");
         settingsWriter.write(volume + "\n");
         settingsWriter.write(theme + "\n");
         settingsWriter.close();
      } catch (Exception e){ new AkariError(e); }
   }
   public static void main(String[] args) {
      new Akari();
   }
}

