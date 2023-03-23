package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    //check if it has to hover
    private boolean hovering;

    //for hovering the center
    private boolean centreBlock = false;

    //The images for the center and the texture for the block
    private Image republicLogo = new Image(getClass().getResource("/images/republicLogo.png").toExternalForm());
    private Image texture = new Image (getClass().getResource("/images/texture.png").toExternalForm());

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);

    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
        //If the block is centred, a circle is displayed
        if (centreBlock) paintCentre();

        //if you are on the block paint center
        if (hovering) paintCentre();
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();
        //Put the image

        //Clear
        gc.clearRect(0,0,width,height);

        //Draw the rectangle with the image
        gc.setFill(Color.rgb(1,2,0,0.5));
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        //To not change the entire code i just put here white
        colour = Color.color(0.965,0.961,0.952,0.9);
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        gc.setFill(Color.color(1, 1, 1, 0.5));

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);
        gc.drawImage(texture,0,0,width,height);

        //Border
        gc.setStroke(Color.color(0.1, 0.1, 0.1, 0.4));
        gc.strokeRect(0, 0, width, height);

    }

    /**
     * Set Center
     * @param centreBlock
     */
    public void setCentreBlock(boolean centreBlock) {
        this.centreBlock = centreBlock;
        paint();
    }

    //Paint center with the rebels logo
    public void paintCentre() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.fillOval(width / 4, height / 4, width / 2, height / 2);
        gc.drawImage(republicLogo,width / 4, height / 4, width / 2, height / 2);
    }

    /**
     * Do the animation for the line clearing
     */
    public void fadeOut(){
        logger.info("Fading Out");
        paintEmpty();
        paintColor(Color.BLACK);

        AnimationTimer timer = new AnimationTimer() {
            double opacity = 0.9;
            @Override
            public void handle(long l) {
                paintEmpty();
                opacity = opacity -  0.02;
                if (opacity <= 0) {
                    stop();
                } else {
                    GraphicsContext gc = getGraphicsContext2D();
                    gc.setFill(Color.color(1, 1, 1, opacity));
                    gc.fillRect(0, 0, width, height);
                }
            }
        };
        timer.start();
    }

    /**
     * Put the logo where the mouse is on the board
     * @param hovering
     */
    public void setHovering(boolean hovering) {
        this.hovering = hovering;
        paint();
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

}
