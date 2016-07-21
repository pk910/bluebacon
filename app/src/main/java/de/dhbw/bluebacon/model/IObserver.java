package de.dhbw.bluebacon.model;

/**
 * Interface for observing classes
 */
public interface IObserver {

    /**
     * notify observing object about changes in observed object
     * @param observable Observed object
     */
    void notify(IObservable observable);

}
