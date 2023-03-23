package uk.ac.soton.comp1206.event;

@FunctionalInterface
public interface GameLoopListener {

    /**
     *
     * @param time
     */
    void setOneRoundLength(int time);

}
