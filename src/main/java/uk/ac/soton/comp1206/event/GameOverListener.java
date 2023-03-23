package uk.ac.soton.comp1206.event;

/**
 * listen when the game is finished
 */

@FunctionalInterface
public interface GameOverListener {

    /**
     * Pass what to do when the game is over
     */
    void gameOver();

}
