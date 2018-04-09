package Graphics;


/**
 * Keeps the neccesarry functions for all Window classes
 */
public interface Windowable {
    /**
     * Prepares the window for viewing
     */
    void redraw();
    /**
     * Redraws the window after an update has happened
     */
    void showUI();
}
