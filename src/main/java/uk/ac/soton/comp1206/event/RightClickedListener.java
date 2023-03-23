package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

@FunctionalInterface
public interface RightClickedListener {

    /**
     * Pass what to do when rightClicked
     */
    void setOnRightClicked();

}
