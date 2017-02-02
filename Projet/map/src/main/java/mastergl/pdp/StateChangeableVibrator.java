package mastergl.pdp;

/**
 * Interface which offers the only methods a StateDirectionsHandler must use, which are vibrations methods
 * and changing state method.
 */
public interface StateChangeableVibrator {

    /**
     * Change the state of the changeable class.
     * @param state the state to set.
     * @return the new state of the changeable.
     * @see StateDirectionsHandler
     */
    StateDirectionsHandler changeState(StateDirectionsHandler state);

    /**
     * Vibrate when the user must turn to left on the next move.
     */
    void vibrateLeft();

    /**
     * Vibrate when the user must turn to right on the next move.
     */
    void vibrateRight();

    /**
     * Vibrate when the user is in the wrong directions
     */
    void vibrateWrongDirections();
}
