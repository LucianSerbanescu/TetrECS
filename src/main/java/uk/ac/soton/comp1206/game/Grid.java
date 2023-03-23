package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    //create the logger
    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Check if the piece may be placed
     * @param piece
     * @param placeX
     * @param placeY
     * @return
     */
    public boolean canPlayPiece (GamePiece piece, int placeX, int placeY) {
        logger.info("Run canPlayPiece method");

        //to not start from the middle and start from the corner
        placeX = placeX - 1;
        placeY = placeY - 1;
        int [][] blocks = piece.getBlocks(); //Our 3x3 array of int blocks

        //loops through every part of the game piece
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                int value = blocks[x][y];
                if (value == 0) continue; //If there isn't a block we ignore it
                int gridValue = get(x + placeX, y + placeY);
                if(gridValue > 0 || gridValue == -1) {
                    //There is something in the way!
                    return false;
                }
            }
        }
        //There is nothing on the way!
        return true;

    }

    //Add the piece on the Game Grid

    /**
     * Put the piece in the grid
     * @param piece
     * @param placeX
     * @param placeY
     */
    public void playPiece(GamePiece piece, int placeX, int placeY) {
        logger.info("Run playPiece method");

        if(!canPlayPiece(piece, placeX,placeY)) return;

        placeX = placeX - 1;
        placeY = placeY - 1;
        int [][] blocks = piece.getBlocks(); //Our 3x3 array of int blocks

        //loops through every part of the game piece
        for (int x = 0; x < blocks.length; x++){
            for (int y = 0; y < blocks[x].length; y++){
                int value = blocks[x][y];
                if(value == 0) continue; //If there isn't a block we ignore it

                //updated the grid of the game board with that piece
                set(x + placeX, y + placeY, value);
            }
        }
    }
}
