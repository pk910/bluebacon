package de.dhbw.bluebacon.model;

/**
 * Interface for observable classes
 */
public interface IObservable {

    /**
     * subscribe observing object for changes of this object
     * @param observer Observing object
     */
    void subscribe(IObserver observer);

    /**
     * unsubscribe observing object for changes of this object
     * @param observer Observing object
     */
    void unsubscribe(IObserver observer);

    /**
     * Checks if the observer is already subscribed
     * @param observer
     * @return True if observer is already subscribed
     */
    Boolean isSubscribed(IObserver observer);

}
