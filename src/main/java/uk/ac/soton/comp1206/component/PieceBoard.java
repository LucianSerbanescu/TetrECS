package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

public class PieceBoard extends GameBoard{

    int cols;
    int rows;

    /**
     * Construct a piece board
     * @param cols
     * @param rows
     * @param width
     * @param height
     */
    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height, true);
        this.cols = cols;
        this.rows = rows;
    }

    //Setting a piece to display

    /**
     * Set piece in the board
     * @param gamePiece
     */
    public void setPiece(GamePiece gamePiece) {
        for (int x = 0; x < cols; x++){
            for (int y = 0; y < rows; y++){
                grid.set(x, y, 0);
            }
        }
        grid.playPiece(gamePiece, 1, 1);
    }

    /**
     * Show senter
     */
    public void showCentre() {
        // If the grid is not 5 by 5 you need to change this
        blocks[1][1].setCentreBlock(true);
    }
}
