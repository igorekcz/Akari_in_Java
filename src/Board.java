import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class creates a Board object of any size, the object has all the necessary methods for a game of Akari.
 */
public class Board {
    /** Defines the square board size. */
    public final int boardSize;
    /** A 2-D array of our Tile objects representing the current state of the Board and game. */
    public final Tile[][] board;
    public boolean creatorMode;
    public Stack<Move> moveHistory;
    static private JFrame winFrame;
    static private boolean winFrameVisible;

    public Board(int boardSize, boolean creatorMode){
        this.creatorMode = creatorMode;
        this.boardSize = boardSize;
        this.board = new Tile[boardSize][boardSize];
        this.moveHistory = new Stack<>();
    }
    /** This method will load any game file as long as it's in the proper format */
    public void getFromFile(String path){
        try{
            File level = new File(path);
            Scanner saveFileScanner = new Scanner(level);
            int readSize = Integer.parseInt(saveFileScanner.next());
            if (readSize != boardSize) throw new IllegalArgumentException();
            for (int y = 0; y < boardSize; y++){
                for (int x = 0; x < boardSize; x++){
                    this.board[x][y] = new Tile(this);
                    this.board[x][y].setState(Integer.parseInt(saveFileScanner.next()));
                    this.board[x][y].setPosition(x, y, boardSize);
                }
            }
            saveFileScanner.close();
            for (int y = 0; y < boardSize; y++) {
                for (int x = 0; x < boardSize; x++) {
                    if (this.board[x][y].state == 10) lightUpTileLogic(x, y);
                }
            }
        } catch (Exception e){
            new AkariError(e);
        }
    }
    /** Sets the position of tiles on a fully blank board. */
    public void createBlank(){
        for (int y = 0; y < boardSize; y++){
            for (int x = 0; x < boardSize; x++){
                this.board[x][y] = new Tile(this);
            //    this.board[x][y].setState(5);
                this.board[x][y].setPosition(x, y, boardSize);
            }
        }
    }

    /** After constructing a Board object we can show it to the user via this method. */
    public void showBoard(){
        for (int y = 0; y < boardSize; y++){
            for (int x = 0; x < boardSize; x++){
                Tile currTile = this.board[x][y];
                currTile.setVisible(true);
                Akari.mainPanel.add(currTile);
            }
        }
        Akari.mainPanel.revalidate();
        Akari.mainPanel.repaint();
    }

    /**
     * This method should run for every placed and existing light-bulb, sets all the other tiles logic variables
     * accordingly.
     * @param x X coordinate of a light-bulb in the 2-D tile array.
     * @param y Y coordinate of a light-bulb in the 2-D tile array.
     */
    public void lightUpTileLogic(int x, int y){
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] direction : directions) {
            int xL = x + direction[0];
            int yL = y + direction[1];
            Loop: while (xL >= 0 && xL < this.boardSize && yL >= 0 && yL < this.boardSize) {
                if (xL == x + direction[0] && yL == y + direction[1]) {
                    this.board[xL][yL].bulbNeighbors++;
                    this.board[xL][yL].setState(this.board[xL][yL].state);
                }
                switch (this.board[xL][yL].state) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 6:
                        break Loop;
                    case 5:
                        this.board[xL][yL].setState(7);
                        this.board[xL][yL].lightCount++;
                        break;
                    case 7:
                    case 8:
                        this.board[xL][yL].lightCount++;
                        break;
                    case 9:
                        this.board[xL][yL].setState(8);
                        this.board[xL][yL].lightCount++;
                        break;
                    default:
                        new AkariError("Illegal bulb placement");
                        break;
                }
                xL += direction[0];
                yL += direction[1];
            }
        }
    }

    /**
     * This method should run everytime a light-bulb is removed from the board. Updates the logic variables of surrounding
     * Tile objects.
     * @param x X coordinate of a removed light-bulb in the 2-D tile array.
     * @param y Y coordinate of a removed light-bulb in the 2-D tile array.
     */
    public void removeLightTileLogic(int x, int y){
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] direction : directions) {
            int xL = x + direction[0];
            int yL = y + direction[1];
            Loop: while (xL >= 0 && xL < this.boardSize && yL >= 0 && yL < this.boardSize) {
                if (xL == x + direction[0] && yL == y + direction[1]) {
                    this.board[xL][yL].bulbNeighbors--;
                    this.board[xL][yL].setState(this.board[xL][yL].state);
                }
                switch (this.board[xL][yL].state) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 6:
                        break Loop;
                    case 7:
                        this.board[xL][yL].lightCount--;
                        if (this.board[xL][yL].lightCount == 0) this.board[xL][yL].setState(5);
                        break;
                    case 8:
                        this.board[xL][yL].lightCount--;
                        if (this.board[xL][yL].lightCount == 0) this.board[xL][yL].setState(9);
                        break;
                    default:
                        new AkariError("Illegal bulb placement");
                        break;
                }
                xL += direction[0];
                yL += direction[1];
            }
        }
    }
    /** Method checks whether the current board is in a winning position. */
    public boolean checkIfUserWon(){
        for (int y = 0; y < boardSize; y++) {
            for (int x = 0; x < boardSize; x++) {
                switch (this.board[x][y].state) {
                    case 0, 1, 2, 3, 4 -> {
                        if (this.board[x][y].bulbNeighbors != this.board[x][y].state) return false;
                    }
                    case 5, 9 -> {
                        return false;
                    }
                    default -> {
                    }
                }
            }
        }
        return true;
    }
    public void clearBulbs(){
        for (Tile[] row: board){
            for (Tile tile: row){
                switch (tile.state){
                    case 7, 8, 9, 10: tile.setState(5);
                    default:
                        tile.bulbNeighbors = 0;
                        tile.lightCount = 0;
                        tile.setState(tile.state);
                        break;
                }
            }
        }
    }
    public void WinningPanel(){
        winFrame = new JFrame("Congratulations!");
        JLabel winLabel = new JLabel();
        if (Akari.theme == 0) {
            winLabel.setBounds(0, 0, 400, 400);
            winLabel.setIcon(new ImageIcon("AkariData/imgs/win_0.png"));
            winFrame.setSize(416,435);
        }
        else {
            winLabel.setBounds(0, 0, 439, 498);
            winLabel.setIcon(new ImageIcon("AkariData/imgs/mario_win.gif"));
            winFrame.setSize(455,533);
        }
        ImageIcon startIcon = new ImageIcon("AkariData/imgs/icon.png");
        winFrame.setIconImage(startIcon.getImage());
        winFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        winFrame.setLocationRelativeTo(null);
        winFrame.setLayout(null);
        winFrame.setResizable(false);
        winFrame.add(winLabel);
        winFrame.setVisible(true);
        winFrameVisible = true;
    }
    static public void removeWinningPanel(){
        if(winFrameVisible) winFrame.dispose();
        winFrameVisible = false;
    }
    public void saveBoard(AtomicInteger boardSize, Board board, int I) {
        saveBoard(boardSize, board, "AkariData/levels/" + I);
    }

    public void saveBoard(AtomicInteger boardSize, Board board, String pathName){
        try {
            FileWriter saveData = new FileWriter(pathName);
            saveData.write(boardSize + "\n");
            for (int y = 0; y < board.boardSize; y++){
                for (int x = 0; x < board.boardSize; x++){
                    saveData.write(board.board[x][y].state + " ");
                }
                saveData.write("\n");
            }
            saveData.close();
        } catch (Exception err) {
            JOptionPane errorPane = new JOptionPane("An error occurred while trying to save the creation", JOptionPane.ERROR_MESSAGE);
            JDialog errorDialog = errorPane.createDialog("Error!");
            errorDialog.setAlwaysOnTop(true);
            errorDialog.setVisible(true);
            new AkariError(err);
        }
    }

    public void undoMove(){
        if(!moveHistory.empty()) {
            Move move = moveHistory.pop();
            if (move.leftClicked()) move.movedTile().startLeftClickLogic();
            else move.movedTile().startRightClickLogic();
        }
    }
}


