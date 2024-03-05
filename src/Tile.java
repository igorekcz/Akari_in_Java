import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class creates Tile objects with a provided state, if no state is provided the default is state 5 (Empty square)
 */
public class Tile extends JButton {
    /** Describes the current state of the Tile, there are 11 possible states */
    public int state;
    /** lightCount should never have a value outside the range of 0-2 */
    public int lightCount;
    /** Should always be in range 0-4 used to determine whether number should be normal, green or red. */
    public int bulbNeighbors;
    public boolean leftClickable;
    public boolean rightClickable;
    private int x;
    private int y;
    private boolean mouseOver;
    private final Board assignedBoard;

    public Tile(int state, Board assignedBoard){
        this.assignedBoard = assignedBoard;
        this.lightCount = 0;
        this.bulbNeighbors = 0;
        this.setState(state);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e){ mouseOver = true; }
            @Override
            public void mouseExited(MouseEvent e){ mouseOver = false; }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && (leftClickable || assignedBoard.creatorMode) && mouseOver){
                    startLeftClickLogic();
                    assignedBoard.moveHistory.push(new Move((Tile) e.getSource(), true));
                }
                else if (e.getButton() == MouseEvent.BUTTON3 && (rightClickable || assignedBoard.creatorMode) && mouseOver){
                    startRightClickLogic();
                    assignedBoard.moveHistory.push(new Move((Tile) e.getSource(), false));
                }
            }
        });
    }
    public Tile(Board assignedBoard) { this(5, assignedBoard); }

    /** Sets a new state for a Tile and updates the Icon displayed on the Tile accordingly, we can also give the current state
     * in order to refresh the icon for states that have more than one icon to choose from. */
    public void setState(int state){
        this.state = state;
        switch (state) {
            case 0, 1, 2, 3, 4 -> {
                if (this.bulbNeighbors == state)
                    this.setIcon(new ImageIcon("AkariData/imgs/" + Akari.tileSize + "_" + state + "_" + Akari.theme + "_green.png"));
                else if (this.bulbNeighbors > state)
                    this.setIcon(new ImageIcon("AkariData/imgs/" + Akari.tileSize + "_" + state + "_" + Akari.theme + "_red.png"));
                else
                    this.setIcon(new ImageIcon("AkariData/imgs/" + Akari.tileSize + "_" + state + "_" + Akari.theme + ".png"));
                leftClickable = false;
                rightClickable = false;
            }
            case 5 -> {
                this.setIcon(new ImageIcon("AkariData/imgs/" + Akari.tileSize + "_" + state + "_" + Akari.theme + ".png"));
                this.leftClickable = true;
                this.rightClickable = true;
            }
            case 6 -> {
                this.setIcon(new ImageIcon("AkariData/imgs/" + Akari.tileSize + "_" + state + "_" + Akari.theme + ".png"));
                this.leftClickable = false;
                this.rightClickable = false;
            }
            case 10 -> {
                this.setIcon(new ImageIcon("AkariData/imgs/" + Akari.tileSize + "_" + state + "_" + Akari.theme + ".png"));
                this.leftClickable = true;
                this.rightClickable = false;
            }
            case 7, 8, 9 -> {
                this.setIcon(new ImageIcon("AkariData/imgs/" + Akari.tileSize + "_" + state + "_" + Akari.theme + ".png"));
                this.leftClickable = false;
                this.rightClickable = true;
            }
            default -> new AkariError("Unsupported game block ID in save file.");
        }
        }

    public void setPosition(int x, int y, int boardSize){
        int tileSize = 1;
        this.x = x;
        this.y = y;
        switch (boardSize) { // Można to lepiej zrobić poprostu dzieląc i zaokrąglając
            case 7 -> tileSize = 86;
            case 10 -> tileSize = 60;
            case 14 -> tileSize = 43;
            default -> new AkariError("Invalid boardSize provided to setPosition method");
        }
        this.setBounds(x * tileSize, y * tileSize, tileSize, tileSize);
    }

    public void startLeftClickLogic(){
        if (!assignedBoard.creatorMode) {
            switch (this.state) {
                case 5 -> {
                    this.setState(10);
                    assignedBoard.lightUpTileLogic(x, y);
                    if (assignedBoard.checkIfUserWon()) assignedBoard.WinningPanel();
                }
                case 10 -> {
                    this.setState(5);
                    assignedBoard.removeLightTileLogic(x, y);
                }
                default -> new AkariError("Unexpected LeftClickTileID");
            }
        }
        else{
            if (this.state != Editor.selectedTile) this.setState(Editor.selectedTile);
            else this.setState(5);
            Editor.isSaved = false;
        }
    }

    public void startRightClickLogic(){
        if (!assignedBoard.creatorMode){
        switch (this.state) {
            case 5 -> this.setState(9);
            case 7 -> this.setState(8);
            case 8 -> this.setState(7);
            case 9 -> this.setState(5);
            default -> new AkariError("Unexpected RightClickTileID");
        }
    }}
}

record Move(Tile movedTile, boolean leftClicked) {
}

