/**
 * A box that holds an object.
 * @author Marc Fernandes 101288346
 * @version 2025-01-23
 */
public class Box {
    // the object to hold and whether the box is empty
    private Object o;
    private boolean empty = true;

    /**
     * Returns the object if the box is not empty, and
     * sets the box to empty.
     * @return The object that was held
     */
    public synchronized Object get() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        empty = true;
        notify();
        return o;
    }

    /**
     * Sets the box to the object given.
     * @param o The object for the box to take
     */
    public synchronized void put(Object o) {
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        this.o = o;
        empty = false;
        notifyAll();
    }

    /**
     * Returns the object if the box is not empty,
     * however does not remove the object from the box.
     * @return The object that was held
     */
    public synchronized Object peek() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        return o;
    }
}