package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
//import jdk.internal.foreign.abi.CallingSequence;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.multimedia.Multimedia;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    // The piece which has to be placed
    protected GamePiece currentPiece ;

    //The next piece which is following to come
    protected GamePiece followingPiece;

    //For keeping track of what lines needs to be deleted
    protected final ArrayList<Integer> horizontalLinesToBeDeleted = new ArrayList<>();
    protected final ArrayList<Integer> verticalLinesToBeDeleted = new ArrayList<>();

    //Listener is called when the next piece is generated
    private NextPieceListener nextPieceListener;

    //Listener which is triggered when the line is cleared
    private LineClearedListener lineClearedListener;

    //Listener which is triggered when the game has to loop
    private GameLoopListener gameLoopListener;

    //Listens when the game is ending
    private GameOverListener gameOverListener;

    //The random generator
    private final Random random = new Random();

    //All the game values, they are bind with the UI
    protected final SimpleIntegerProperty score = new SimpleIntegerProperty(0);
    protected final SimpleIntegerProperty level = new SimpleIntegerProperty(0);
    protected final SimpleIntegerProperty lives = new SimpleIntegerProperty(3);
    protected final SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    protected ScheduledExecutorService executor;

    protected ScheduledFuture nextRound;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    //HelpDesk told me how to do this method
    //Continue the game when it has to restart
    public void restartGameLoop() {
        //Stop running the current next Round so it wont exit
        nextRound.cancel(false);
        //start the next round
        nextRound = executor.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        gameLoopListener.setOneRoundLength(getTimerDelay());
    }

    /**
     * Stop the executor
     */
    public void stop() {
        executor.shutdownNow();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        nextRound = executor.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        gameLoopListener.setOneRoundLength(getTimerDelay());
        //Creating the second piece
        followingPiece = spawnPiece();
        //Give the value of second piece to the first piece and create a new second piece
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     * @return if the peace can be placed
     */
    public boolean blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        //If the piece can't be played get out of the method
        if(!grid.canPlayPiece(currentPiece,x,y)){
            return false;
        }
        //If it passed the verification place the piece
        grid.playPiece(currentPiece, x, y);
        //Generate the next piece after the previoues one was placed
        nextPiece();
        //Check for any lines that need to be cleared
        afterPiece();
        //Clear if the case
        clear();
        return true;
    }

    //Set next piece
    public void nextPiece() {
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        nextPieceListener.nextPiece(currentPiece, followingPiece);
    }

/**
    Return a piece randomly
    @return the piece
*/
    public GamePiece spawnPiece() {
        logger.info("Generate a random piece");
        return GamePiece.createPiece(random.nextInt(15));
    }

    //Check if any line has to be cleared
    //Copied from Olis tutorial
    public void afterPiece() {
        //Here we are going to check to see if we need to clear any lines
        for(var x = 0; x < cols; x++) {
            var counterVertical = 0;
            for (var y = 0; y < rows; y++) {
                //Vertical lines are cleaned
                if (grid.get(x,y) == 0) break;
                counterVertical++;
            }
            if (counterVertical == rows) {
                //Have to clear this line
                verticalLinesToBeDeleted.add(x);
            }
        }
        for(var y = 0; y < rows; y++) {
            var counterHorizontal = 0;
            for (var x = 0; x < cols; x++) {
                //Horizontal lines are cleaned
                if (grid.get(x,y) == 0) break;
                counterHorizontal++;
            }
            if (counterHorizontal == cols) {
                //Have to clear this line
                horizontalLinesToBeDeleted.add(y);
            }
        }
    }

    //Update the score
    public void updateScore(){
        //case when you have either vertical or horizontal lines to delete
        if(horizontalLinesToBeDeleted.size() == 0 && verticalLinesToBeDeleted.size() != 0) {
            score(verticalLinesToBeDeleted.size(),verticalLinesToBeDeleted.size() * rows);
        } else if((horizontalLinesToBeDeleted.size() != 0 && verticalLinesToBeDeleted.size() == 0)){
            score(horizontalLinesToBeDeleted.size(),horizontalLinesToBeDeleted.size() * cols);
        } else {
            //case when both vertical and horizontal lines are deleted
            //logger.info("number of lines {}" + (horizontalLinesToBeDeleted.size() + verticalLinesToBeDeleted.size()));
            //logger.info("number of blocks {}" +((verticalLinesToBeDeleted.size()* rows) + (horizontalLinesToBeDeleted.size() * cols) - (horizontalLinesToBeDeleted.size() + verticalLinesToBeDeleted.size())));
            score(horizontalLinesToBeDeleted.size() + verticalLinesToBeDeleted.size(),(verticalLinesToBeDeleted.size()* rows) + (horizontalLinesToBeDeleted.size() * cols) - (horizontalLinesToBeDeleted.size() * verticalLinesToBeDeleted.size()));
        }
        increaseLevel();
    }

    /**
     * Count the score
     * @param numberOfLines
     * @param numberOfBlocks
     */
    public void score(int numberOfLines, int numberOfBlocks) {
        if(numberOfLines != 0){
            score.set(score.intValue() + (numberOfLines * numberOfBlocks * 10 * multiplier.intValue()));
        }
    }

    public void increaseLevel() {
        //increase the level
        logger.info("Set level : " + level.get());
        levelProperty().setValue(score.getValue() / 1000);
    }

    /**
     * Clear column
     * @param x
     */
    public void clearColumn(int x) {
        //Set all values in that in column to 0 (empty)
        for (int y = 0; y < rows; y++){
            sendBlockToFadeOut(x,y);
            grid.set(x,y,0);
        }
    }

    /**
     * Clear row
     * @param y
     */
    public void clearRow(int y) {

        for (int x = 0; x < cols; x++) {
            sendBlockToFadeOut(x,y);
            grid.set(x,y,0);
        }
    }

    //Passes to the clearRow and cleaColumn methid the lines that need to be cleared
    public void clear(){
        logger.info("Clear the lines");
        var iteratorHor = horizontalLinesToBeDeleted.iterator();
        var iteratorVer = verticalLinesToBeDeleted.iterator();

        //Update score
        logger.info("Score is updated");
        updateScore();

        //increase the multiplier if there is a streak
        if(horizontalLinesToBeDeleted.size() != 0 || verticalLinesToBeDeleted.size() != 0){
            multiplier.set(multiplier.intValue() + 1);
        } else {
            multiplier.set(1);
        }

        //Iterate vertical cleaning
        while(iteratorVer.hasNext()){
            int columnToBeCleared = iteratorVer.next();
            clearColumn(columnToBeCleared);
            //Call this lineCLeared mothod for fading out in its implementation
            //lineClearedListener.fadeLinesFromListener(columnToBeCleared,-1);
            iteratorVer.remove();
        }
        //Clear the list
        //verticalLinesToBeDeleted.clear();

        //Iterate horizontal cleaning
        while(iteratorHor.hasNext()){
            int rowToBeCleared = iteratorHor.next();
            clearRow(rowToBeCleared);
            //Call this method for fading out in implementation
            //lineClearedListener.fadeLinesFromListener(-1,rowToBeCleared);
            iteratorHor.remove();
        }
        //Clear the list
        //horizontalLinesToBeDeleted.clear();
       // Multimedia.playAudio("clear.wav");
    }

    /**
     *
     * @return
     */
    public GamePiece getSecondPiece() {
        return followingPiece;
    }

    //Methods called in other classes
    public void rotateCurrentPiece(int rotate) {
        currentPiece.rotate(rotate);
    }

    public void swapCurrentPiece() {
        GamePiece holdingPiece = currentPiece;
        currentPiece = followingPiece;
        followingPiece = holdingPiece;
    }

    //Making the gameLoop and the timer

    /**
     * One round timer
     * @return
     */

    public int getTimerDelay() {
        return Math.max(2500, 12000 - 500 * level.get());
    }

    //Make the loop
    private void gameLoop() {
        multiplier.set(1);
        if (lives.get() > 0) {
            //logger.info("Looping the game now");
            lives.set(lives.get() - 1);
            Multimedia.playAudio("oh-no.mp3");
            nextPiece();
            gameLoopListener.setOneRoundLength(getTimerDelay());
            nextRound = executor.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        } else Platform.runLater(gameOverListener::gameOver);
    }

    //Setting Listeners
    public void setNextPieceListener(NextPieceListener nextPieceListener){
        this.nextPieceListener = nextPieceListener;
    }

    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    public void setOnGameOver(GameOverListener gameOverListener) {
        this.gameOverListener = gameOverListener;
    }

    public void setLineClearedListener(LineClearedListener lineClearedListener){
        this.lineClearedListener = lineClearedListener;
    }

    //all getters for the properties
    public void sendBlockToFadeOut(int x, int y) {
        lineClearedListener.fadeLine(x,y);
    }

    public IntegerProperty scoreProperty() { return score; }

    public IntegerProperty livesProperty() { return lives; }

    public IntegerProperty levelProperty() { return level; }

    public IntegerProperty multiplierProperty() { return multiplier; }

    public int getScore() { return  this.score.get(); }

    public int getLevel() { return this.level.get(); }

    public int getLives() { return this.lives.get(); }

    public int getMultiplier() {
        return this.multiplier.get();
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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

    //Returning methods
    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    public VBox getPlayersVBox() {
        return null;
    }
}
