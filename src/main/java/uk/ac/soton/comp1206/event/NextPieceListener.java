package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

@FunctionalInterface
public interface NextPieceListener {

    /**
     * Pass the next piece
     * @param currentPiece
     * @param nextPiece
     */
    void nextPiece(GamePiece currentPiece, GamePiece nextPiece);
}
