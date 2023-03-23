package uk.ac.soton.comp1206.event;

public interface LineClearedListener {

    //This should take a set of GameBlocksCoodonates
    void fadeLine(int columnToBeFaded, int rowToBeFaded);

}
